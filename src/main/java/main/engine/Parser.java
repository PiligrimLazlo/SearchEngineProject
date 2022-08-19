package main.engine;

import lombok.extern.log4j.Log4j2;
import main.entities.Field;
import main.entities.Index;
import main.entities.Lemma;
import main.entities.Page;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;
import org.springframework.data.util.Pair;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;


/**
 * Класс для обхода всех страницы сайта. Заполняет структуры данных java,
 * которые затем отдаются наружу и используюся для формирования БД
 * необходимой для работы с веб приложением поиска.
 */
@Log4j2
public class Parser extends RecursiveAction {

    private String site;
    private String parentDomain;

    /**
     * Структуры хранения данных java. В единственном экземпляре для каждого сайта.
     * Далее {@link #indexMap} отдается наружу для заполнения БД.
     */
    private final Map<String, Page> pages;
    private final Map<String, Lemma> lemmaMap;
    private final Map<Pair<String, String>, Index> indexMap;

    private static Iterable<Field> fieldsForIndex;//передаем снаружи, здесь не работаем с БД

    private static boolean isCanceled;
    private static final Map<String, Integer> pagesCount = new ConcurrentHashMap<>();
    private static final Map<String, Integer> lemmasCount = new ConcurrentHashMap<>();

    //for main client
    //формат http://www.site.com/
    public Parser(String site) {
        this.site = site;
        this.parentDomain = site;

        pages = new ConcurrentHashMap<>();
        lemmaMap = new ConcurrentHashMap<>();
        indexMap = new ConcurrentHashMap<>();
    }

    //for subtasks
    public Parser(
            String subSite,
            String parentSite,
            Map<String, Page> pages,
            Map<String, Lemma> lemmaMap,
            Map<Pair<String, String>, Index> indexMap
    ) {
        this.site = subSite;
        this.parentDomain = parentSite;

        this.pages = pages;
        this.lemmaMap = lemmaMap;
        this.indexMap = indexMap;
    }


    /**
     * Рекурсивно обходит все страница сайта, пока не упирается в уже пройденную;
     * для каждой страницы вызывает методы
     * {@link #putPageInMap(Connection)}, {@link #putLemmasInMap(Document)}, {@link #createIndex(Page, Map)}
     */
    @Override
    protected void compute() {
        if (isCanceled) return;
        try {
            List<Parser> subParsers = new ArrayList<>();

            Connection connection = Jsoup.connect(site)
                    .userAgent("AdvancedSearchBot")
                    .referrer("http://www.google.com/");

            Page curPage = putPageInMap(connection);
            if (curPage == null) return;
            countPages();

            Document doc = connection.get();

            Map<Lemma, Integer> curLemmas = putLemmasInMap(doc);
            Set<Index> indexes = createIndex(curPage, curLemmas);
            curPage.setIndexes(indexes);
            curLemmas.forEach((lemmaEntity, amount) -> lemmaEntity.setIndexes(indexes));


            Elements elements = doc.select("a");


            for (Element e : elements) {
                String currentLink = e.absUrl("href");
                if (currentLink.contains(parentDomain)) {
                    Parser parser = new Parser(currentLink, parentDomain, pages, lemmaMap, indexMap);
                    subParsers.add(parser);
                }
            }
            ForkJoinTask.invokeAll(subParsers);

        } catch (IOException e) {
            log.error(e.getMessage() + " поймано в методе compute()");
        }
    }


    /**
     * Создает сущность Page
     * Заполняет {@link #pages} - структуру хранения всех страниц сайта
     *
     * @param connection
     * @return boolean
     */
    private Page putPageInMap(Connection connection) {
        synchronized (pages) {
            String currentShortPath = getShortPath();
            if (pages.containsKey(currentShortPath)) return null;
            Page page = new Page();
            page.setPath(currentShortPath);

            try {
                connection.ignoreHttpErrors(true);

                Connection.Response response = connection.execute();

                int statusCode = response.statusCode();
                if (statusCode != 200 && (response.contentType() == null || !response.contentType().equals("html/text")))
                    throw new UnsupportedMimeTypeException("msg", "unknown type", response.url().toString());

                page.setCode(statusCode);
                page.setContent(response.body());
                pages.put(currentShortPath, page);
                log.info(currentShortPath + " - записана ссылка");
                return page;
            } catch (UnsupportedMimeTypeException mimeTypeEx) {
                log.warn(mimeTypeEx.getUrl() + " - тип ссылки не поддерживается (передана ссылка на картинку, zip и т.д.)");
                return null;
            } catch (IOException ioEx) {
                log.error(ioEx.getMessage() + " поймано в методе: putInMap(Connection connection)");
                return null;
            }
        }
    }

    /**
     * Находит все леммы для текущей страницы. Создает сущности Lemma для каждой строковой леммы
     * Заполняет {@link #lemmaMap} - структуру хранения всех лемм сайта
     *
     * @param doc
     * @throws IOException
     */
    private Map<Lemma, Integer> putLemmasInMap(Document doc) throws IOException {
        synchronized (lemmaMap) {
            Map<Lemma, Integer> lemmaMapForCurrentThread = new HashMap<>();

            for (Field field : fieldsForIndex) {
                Elements select = doc.select(field.getSelector());
                String cleanPage = Jsoup.clean(select.toString(), Safelist.none());

                Map<String, Integer> foundLemmas = Lemmatizer.getInstance().collectLemmas(cleanPage);

                foundLemmas.forEach((foundLemma, amount) -> {

                    Lemma lemmaEntity = null;
                    if (!lemmaMap.containsKey(foundLemma)) {
                        lemmaEntity = new Lemma();
                        lemmaEntity.setFrequency(1);
                        lemmaEntity.setLemma(foundLemma);
                    } else {
                        lemmaEntity = lemmaMap.get(foundLemma);

                        if (lemmaMapForCurrentThread.containsKey(lemmaEntity)) return;

                        int existsFrequency = lemmaEntity.getFrequency();
                        lemmaEntity.setFrequency(existsFrequency + 1);
                    }
                    lemmaMapForCurrentThread.put(lemmaEntity, amount);
                    Lemma put = lemmaMap.put(foundLemma, lemmaEntity);
                    if (put == null) countLemmas();
                });
            }
            return lemmaMapForCurrentThread;
        }
    }

    /**
     * Создает сущности Index по количеству лемм для каждой страницы
     *
     * @throws IOException
     */
    private Set<Index> createIndex(Page currentPage, Map<Lemma, Integer> currentLemmas) throws IOException {
        synchronized (indexMap) {
            Map<Pair<String, String>, Index> indexesMapForCurrentThread = new ConcurrentHashMap<>();

            for (Field field : fieldsForIndex) {
                currentLemmas.forEach((lemmaEntity, amount) -> {

                    Pair<String, String> key = Pair.of(getShortPath(), lemmaEntity.getLemma());
                    Index curIndex = indexesMapForCurrentThread.get(key);
                    if (curIndex != null) {
                        curIndex.setRank(curIndex.getRank() + field.getWeight() * amount);
                    } else {
                        curIndex = new Index();
                        curIndex.setPage(currentPage);
                        curIndex.setLemma(lemmaEntity);
                        curIndex.setRank(field.getWeight() * amount);
                    }
                    indexesMapForCurrentThread.put(key, curIndex);
                });
            }
            indexMap.putAll(indexesMapForCurrentThread);
            return new HashSet<>(indexesMapForCurrentThread.values());
        }
    }

    private String getShortPath() {
        String s = "/" + site.replace(parentDomain, "");
        int anchorIndex = s.indexOf("#");
        if (anchorIndex == -1) {
            return s;
        } else {
            return s.substring(0, anchorIndex);
        }
    }


    public List<Index> getIndexes() {
        return new ArrayList<>(indexMap.values());
    }

    public void setFieldForIndex(Iterable<Field> filedForIndex) {
        Parser.fieldsForIndex = filedForIndex;
    }

    public static void setCanceled(boolean isCanceled) {
        Parser.isCanceled = isCanceled;
    }

    private void countPages() {
        if (pagesCount.containsKey(parentDomain)) {
            pagesCount.put(parentDomain, pagesCount.get(parentDomain) + 1);
        } else {
            pagesCount.put(parentDomain, 1);
        }
    }

    private void countLemmas() {
        if (lemmasCount.containsKey(parentDomain)) {
            lemmasCount.put(parentDomain, lemmasCount.get(parentDomain) + 1);
        } else {
            lemmasCount.put(parentDomain, 1);
        }
    }

    public static int getPagesCount(String site) {
        return pagesCount.get(site);
    }

    public static int getLemmaCount(String site) {
        return lemmasCount.get(site);
    }
}

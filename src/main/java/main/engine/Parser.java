package main.engine;

import lombok.extern.log4j.Log4j2;
import main.model.Field;
import main.model.Index;
import main.model.Lemma;
import main.model.Page;
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
     * Структуры хранения данных java. В единственном экземпляре для всех потоков.
     * Далее {@link #indexMap} отдается наружу для заполнения БД.
     */
    private static final Map<String, Page> pages = new ConcurrentHashMap<>();
    private static final Map<String, Lemma> lemmaMap = new ConcurrentHashMap<>();
    private static final Map<Pair<String, String>, Index> indexMap = new ConcurrentHashMap<>();

    private static Iterable<Field> fieldsForIndex;//передаем снаружи, здесь не работаем с БД

    //for main client
    //формат http://www.site.com/
    public Parser(String site) {
        this.site = site;
        this.parentDomain = site;
    }

    //for subtasks
    public Parser(String subSite, String parentSite) {
        this.site = subSite;
        this.parentDomain = parentSite;
    }


    /**
     * Рекурсивно обходит все страница сайта, пока не упирается в уже пройденную;
     * для каждой страницы вызывает методы
     * {@link #putPageInMap(Connection)}, {@link #putLemmasInMap(Document)}, {@link #createIndex(Page, Map)}
     */
    @Override
    protected void compute() {
        try {
            List<Parser> subParsers = new ArrayList<>();

            Connection connection = Jsoup.connect(site)
                    .userAgent("AdvancedSearchBot")
                    .referrer("http://www.google.com/");
            Document doc = connection.get();

            Page curPage = putPageInMap(connection);
            if (curPage == null) return;

            Map<Integer, Lemma> curLemmas = putLemmasInMap(doc);
            Set<Index> indexes = createIndex(curPage, curLemmas);
            curPage.setIndexes(indexes);
            curLemmas.forEach((amount, lemmaEntity) -> lemmaEntity.setIndexes(indexes));


            Elements elements = doc.select("a");


            for (Element e : elements) {
                String currentLink = e.absUrl("href");
                if (currentLink.contains(parentDomain)) {
                    Parser parser = new Parser(currentLink, parentDomain);
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
     * @param connection
     * @return boolean
     */
    private Page putPageInMap(Connection connection) {
        Page page = new Page();
        String currentShortPath = getShortPath();
        if (pages.containsKey(currentShortPath)) return null;
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

    /**
     * Находит все леммы для текущей страницы. Создает сущности Lemma для каждой строковой леммы
     * Заполняет {@link #lemmaMap} - структуру хранения всех лемм сайта
     * @param doc
     * @throws IOException
     */
    private Map<Integer, Lemma> putLemmasInMap(Document doc) throws IOException {
        Map<Integer, Lemma> lemmaMapForCurrentThread = new HashMap<>();

        for (Field field : fieldsForIndex) {
            Elements select = doc.select(field.getSelector());
            String cleanPage = Jsoup.clean(select.toString(), Safelist.none());

            Map<String, Integer> foundLemmas = getLemmas(cleanPage);

            foundLemmas.forEach((foundLemma, amount) -> {
                Lemma lemmaEntity = new Lemma();
                lemmaEntity.setLemma(foundLemma);

                if (!lemmaMap.containsKey(foundLemma)) {
                    lemmaEntity.setFrequency(1);
                } else {
                    int existsFrequency = lemmaMap.get(foundLemma).getFrequency();
                    lemmaEntity.setFrequency(existsFrequency + 1);
                }
                lemmaMapForCurrentThread.put(amount, lemmaEntity);
                lemmaMap.put(foundLemma, lemmaEntity);
            });
        }
        return lemmaMapForCurrentThread;
    }

    /**
     * Создает сущности Index по количеству лемм для каждой страницы
     * @throws IOException
     */
    private Set<Index> createIndex(Page currentPage, Map<Integer, Lemma> currentLemmas) throws IOException {

        Map<Pair<String, String>, Index> indexesMapForCurrentThread = new ConcurrentHashMap<>();

        for (Field field : fieldsForIndex) {
            currentLemmas.forEach((amount, lemmaEntity) -> {

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

    private Map<String, Integer> getLemmas(String text) throws IOException {
        Lemmatizer lemmatizer = Lemmatizer.getInstance();
        return lemmatizer.collectLemmas(text);

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


    public static Map<Pair<String, String>, Index> getIndexMap() {
        return indexMap;
    }

    public void setFieldForIndex(Iterable<Field> filedForIndex) {
        Parser.fieldsForIndex = filedForIndex;
    }

}

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
     * Далее отдаюся наружу для заполнения БД.
     */
    private static final Map<String, Page> pages = new ConcurrentHashMap<>();
    private static final Map<String, Lemma> lemmaMap = new ConcurrentHashMap<>();
    private static final Map<Pair<String, String>, Index> coldIndexMap = new ConcurrentHashMap<>();//key = pair of (path to lemma)

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
     * для каждой страницы вызывает методы  {@link #putPageInMap(Connection)}, {@link #createIndex(Connection)}
     */
    @Override
    protected void compute() {
        try {
            List<Parser> subParsers = new ArrayList<>();

            Connection connection = Jsoup.connect(site)
                    .userAgent("AdvancedSearchBot")
                    .referrer("http://www.google.com/");

            if (!putPageInMap(connection)) return;
            createIndex(connection);

            Document doc = connection.get();
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
     * Создает сущность Page и кладет ее в {@link #pages}, если такой страницы нет
     * @param connection
     * @return boolean
     */
    private boolean putPageInMap(Connection connection) {
        Page page = new Page();
        String currentShortPath = getShortPath();
        if (pages.containsKey(currentShortPath)) return false;
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
            return true;
        } catch (UnsupportedMimeTypeException mimeTypeEx) {
            log.warn(mimeTypeEx.getUrl() + " - тип ссылки не поддерживается (передана ссылка на картинку, zip и т.д.)");
            return false;
        } catch (IOException ioEx) {
            log.error(ioEx.getMessage() + " поймано в методе: putInMap(Connection connection)");
            return false;
        }
    }

    /**
     * Создает сущность Lemma и кладет ее в {@link #lemmaMap},
     * Также создает сущность Index, но без lemma_id, page_id
     * lemma_id, page_id можно получить только после записи в БД
     * @param connection
     * @throws IOException
     */
    private void createIndex(Connection connection) throws IOException {
        Document doc = connection.get();

        Map<String, Lemma> lemmaMapForCurrentThread = new HashMap<>();

        for (Field field : fieldsForIndex) {
            Elements select = doc.select(field.getSelector());

            String cleanPage = Jsoup.clean(select.toString(), Safelist.none());

            Lemmatizer lemmatizer = Lemmatizer.getInstance();
            Map<String, Integer> currentLemmas = lemmatizer.collectLemmas(cleanPage);

            currentLemmas.forEach((foundLemma, amount) -> {
                Lemma lemmaEntity = new Lemma();
                lemmaEntity.setLemma(foundLemma);

                if (!lemmaMap.containsKey(foundLemma)) {
                    lemmaEntity.setFrequency(1);
                } else {
                    int existsFrequency = lemmaMap.get(foundLemma).getFrequency();
                    lemmaEntity.setFrequency(existsFrequency + 1);
                }
                lemmaMapForCurrentThread.put(foundLemma, lemmaEntity);
                //todo Index logic
                Pair<String, String> key = Pair.of(getShortPath(), foundLemma);

                Index curIndex = coldIndexMap.get(key);
                if (curIndex != null) {
                    curIndex.setRank(curIndex.getRank() + field.getWeight() * amount);
                } else {
                    curIndex = new Index();
                    curIndex.setRank(field.getWeight() * amount);
                }
                coldIndexMap.put(key, curIndex);
            });
        }
        lemmaMap.putAll(lemmaMapForCurrentThread);
    }

    private String getShortPath() {
        return "/" + site.replace(parentDomain, "");
    }

    public static List<Page> getPages() {
        return new ArrayList<>(pages.values());
    }

    public static List<Lemma> getLemmas() {
        return new ArrayList<>(lemmaMap.values());
    }

    public static Map<Pair<String, String>, Index> getColdIndexMap() {
        return coldIndexMap;
    }

    public void setFieldForIndex(Iterable<Field> filedForIndex) {
        Parser.fieldsForIndex = filedForIndex;
    }

}

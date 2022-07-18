package main.engine;

import lombok.extern.log4j.Log4j2;
import main.model.Page;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

@Log4j2
public class Parser extends RecursiveAction {

    private String site;
    private String parentDomain;

    private static final Map<String, Page> pages = new HashMap<>();

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


    @Override
    protected void compute() {
        try {
            List<Parser> subParsers = new ArrayList<>();

            Connection connection = Jsoup.connect(site)
                    .userAgent("AdvancedSearchBot")
                    .referrer("http://www.google.com/");

            if (!putInMap(connection)) return;

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


    private boolean putInMap(Connection connection) {
        Page page = new Page();
        String currentShortPath = "/" + site.replace(parentDomain, "");
        if (pages.containsKey(currentShortPath)) return false;
        page.setPath(currentShortPath);

        try {
            Connection.Response response = connection.execute();
            page.setCode(response.statusCode());
            page.setContent(response.body());
            pages.put(currentShortPath, page);
            log.info(site + " - записана ссылка");
            return true;
        }/* catch (HttpStatusException httpStatusEx) {
            page.setCode(httpStatusEx.getStatusCode());
            page.setContent("");
            pages.put(currentShortPath, page);
            log.info(site + " - записана ссылка c кодом отличном от 200");
            return true; //TODO
        }*/ catch (UnsupportedMimeTypeException mimeTypeEx) {
            log.warn(mimeTypeEx.getUrl() + " - тип ссылки не поддерживается (передана ссылка на картинку, zip и т.д.)");
            return false;
        } catch (IOException ioEx) {
            log.error(ioEx.getMessage() + " поймано в методе: putInMap(Connection connection)");
            return false;
        }
    }

    public static List<Page> getPages() {
        return new ArrayList<>(pages.values());
    }
}

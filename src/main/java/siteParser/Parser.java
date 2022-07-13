package siteParser;

import model.Page;
import model.PageDao;
import model.PageDaoImpl;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

public class Parser extends RecursiveAction {

    private String site;
    private String parentDomain;
    private PageDao dao;

    private static final Object lock = new Object();

    //for main client
    //формат http://www.site.com/
    public Parser(String site) {
        this.site = site;
        this.parentDomain = site;
        dao = new PageDaoImpl();
    }

    //for subtasks
    public Parser(String subSite, String parentSite) {
        this.site = subSite;
        this.parentDomain = parentSite;

        dao = new PageDaoImpl();
    }


    public Set<Page> getAllSubPagesList() throws IOException {
        Set<Page> paths = new HashSet<>();

        Document doc = Jsoup.connect(site)
                .userAgent("AdvancedSearchBot")
                .referrer("http://www.google.com/")
                .get();

        Elements elements = doc.select("a");
        for (Element e : elements) {
            String currentLink = e.absUrl("href");

            try {
                org.jsoup.Connection.Response currentConnection = Jsoup.connect(currentLink)
                        .userAgent("AdvancedSearchBot")
                        .referrer("http://www.google.com/")
                        .execute();

                Page page = new Page();
                page.setPath("/" + currentLink.replace(parentDomain, ""));
                page.setCode(currentConnection.statusCode());
                page.setContent(currentConnection.body());
                paths.add(page);
                System.out.println(currentLink);
            } catch (IllegalArgumentException | IOException ex) {
                System.err.println("Ссылка не поддерживается: " + ex.getMessage());
            }
        }
        return paths;
    }

    @Override
    protected void compute() {
        try {
            List<Parser> subParsers = new ArrayList<>();

            Document doc = Jsoup.connect(site)
                    .userAgent("AdvancedSearchBot")
                    .referrer("http://www.google.com/")
                    .get();

            Elements elements = doc.select("a");

            for (Element e : elements) {
                String currentLink = e.absUrl("href");
                if (currentLink.contains(parentDomain)) {
                    if (putInDB(currentLink)) {
                        Parser parser = new Parser(currentLink, parentDomain);
                        subParsers.add(parser);
                    }
                }
            }
            ForkJoinTask.invokeAll(subParsers);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private boolean putInDB(String currentLink) {
        synchronized (lock) {
            if (dao.getPageByPath("/" + currentLink.replace(parentDomain, "")) != null) {
                return false;
            }

            Page page = new Page();
            String currentShortPath = "/" + currentLink.replace(parentDomain, "");
            page.setPath(currentShortPath);

            try {
                Connection.Response currentConnection = Jsoup.connect(currentLink)
                        .userAgent("AdvancedSearchBot")
                        .referrer("http://www.google.com/")
                        .execute();

                page.setCode(currentConnection.statusCode());
                page.setContent(currentConnection.body());


                System.out.println(currentShortPath + " - записана ссылка с помощью потока : " + Thread.currentThread().getName());
            } catch (HttpStatusException e) {
                page.setCode(e.getStatusCode());
                page.setContent("");
            } catch (IOException e) {
                System.err.println(currentShortPath + " - ссылка не поддерживается: " + e.getMessage());
                return false;
            }

            dao.createPage(page);
            return true;
        }
    }
}

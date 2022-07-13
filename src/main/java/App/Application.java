package App;

import model.DBConnection;
import model.Page;
import model.PageDao;
import org.hibernate.Session;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;

public class Application {
    public static void main(String[] args) throws IOException {

        DBConnection.initDb();


        Document doc = Jsoup.connect("http://www.playback.ru/")
                .userAgent("AdvancedSearchBot")
                .referrer("http://www.google.com")
                .get();


        PageDao pageDao = new PageDao();
        pageDao.openCurrentSessionWithTransaction();

        Elements elements = doc.select("a");
        for (Element e : elements) {
            String currentLink = e.absUrl("href");

            try {
                org.jsoup.Connection.Response currentConnection = Jsoup.connect(currentLink)
                        .userAgent("AdvancedSearchBot")
                        .referrer("http://www.google.com")
                        .execute();

                Page page = new Page();
                page.setPath(currentLink);
                page.setCode(currentConnection.statusCode());
                page.setContent(currentConnection.body());
                pageDao.createPage(page);
            } catch (IllegalArgumentException ex) {
                System.err.println("Ссылка не поддерживается: " + ex.getMessage());
            }
        }


    }

}
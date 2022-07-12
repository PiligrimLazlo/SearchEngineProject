import org.hibernate.Session;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class Application {
    public static void main(String[] args) throws IOException {

        java.sql.Connection connection = DBConnection.getConnection();

        Document doc = Jsoup.connect("http://www.playback.ru/")
                .userAgent("AdvancedSearchBot")
                .referrer("http://www.google.com")
                .get();


        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        session.beginTransaction();

        Elements elements = doc.select("a");
        for (Element e : elements) {
            String currentLink = e.absUrl("href");

            Connection.Response currentConnection = Jsoup.connect(currentLink)
                    .userAgent("AdvancedSearchBot")
                    .referrer("http://www.google.com")
                    .execute();

            Page page = new Page();
            page.setPath(currentLink);
            page.setCode(currentConnection.statusCode());
            page.setContent(currentConnection.parse().body().toString());

            session.save(page);
        }
        session.getTransaction().commit();
        session.close();
    }

}
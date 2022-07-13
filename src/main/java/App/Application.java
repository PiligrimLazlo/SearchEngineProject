package App;

import model.DBConnection;
import model.Page;
import model.PageDao;
import model.PageDaoImpl;
import siteParser.Parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;

public class Application {
    public static void main(String[] args) {

        Long start = System.currentTimeMillis();

        DBConnection.initDb();
        Parser parser = new Parser("http://www.playback.ru/");
        //Parser parser = new Parser("http://www.uderzo.it/main_products/space_sniffer/download_alt.html/", "http://www.uderzo.it");
        ForkJoinPool pool = new ForkJoinPool();
        pool.invoke(parser);

        System.out.println("Время парсинга: " + ((System.currentTimeMillis() - start) / 1000) + " секунд");
    }

}
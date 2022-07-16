package App;

import model.DBConnection;
import model.Page;
import model.PageDao;
import model.PageDaoImpl;
import siteParser.Parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ForkJoinPool;

public class Application {
    public static void main(String[] args) {

        Long start = System.currentTimeMillis();


        DBConnection.initDb();
        Parser parser = new Parser("http://www.playback.ru/");
        //Parser parser = new Parser("http://www.uderzo.it/main_products/space_sniffer/index.html", "http://www.uderzo.it");
        ForkJoinPool pool = ForkJoinPool.commonPool();
        pool.invoke(parser);

        PageDao pageDao = new PageDaoImpl();
        pageDao.createAll(Parser.getPages());

        System.out.println("Время парсинга: " + ((System.currentTimeMillis() - start) / 1000) + " секунд");
    }

}
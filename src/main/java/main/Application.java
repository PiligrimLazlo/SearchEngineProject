package main;

import main.OldWay.DBConnection;
import main.model.PageRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import main.siteParser.Parser;

import java.util.concurrent.ForkJoinPool;

@Configuration
@EnableAutoConfiguration
public class Application {


    public static void main(String[] args) {

        Long start = System.currentTimeMillis();


        //if need recreate db
        DBConnection.initDb();

        Parser parser = new Parser("https://www.nikoartgallery.com/");
        //Parser parser = new Parser("http://www.playback.ru/");
        //Parser parser = new Parser("http://www.uderzo.it/main_products/space_sniffer/index.html", "http://www.uderzo.it/");
        ForkJoinPool pool = ForkJoinPool.commonPool();
        pool.invoke(parser);

        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        PageRepository repository = context.getBean(PageRepository.class);

        //PageDao pageDao = new PageDaoImpl();
        //pageDao.createAll(Parser.getPages());

        repository.saveAll(Parser.getPages());

        context.close();


        System.out.println("Время парсинга: " + ((System.currentTimeMillis() - start) / 1000) + " секунд");
    }

}
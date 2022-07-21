package main;

import main.engine.Lemmatizer;
import main.model.PageRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import main.engine.Parser;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

@Configuration
@EnableAutoConfiguration
public class Application {


    public static void main(String[] args) throws IOException {

        Long start = System.currentTimeMillis();

        //if need recreate db
        DBConnection.initDb();

        //test data
        //Parser parser = new Parser("https://www.nikoartgallery.com/");
        //Parser parser = new Parser("http://www.playback.ru/");
        Parser parser = new Parser("http://www.uderzo.it/main_products/space_sniffer/index.html", "http://www.uderzo.it/");

        ForkJoinPool pool = ForkJoinPool.commonPool();
        pool.invoke(parser);

        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        PageRepository repository = context.getBean(PageRepository.class);

        repository.saveAll(Parser.getPages());
        context.close();

        System.out.println("Время парсинга: " + ((System.currentTimeMillis() - start) / 1000) + " секунд");
    }

}
package main;

import main.model.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import main.engine.Parser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.util.Pair;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

@Configuration
@EnableAutoConfiguration
public class Application {


    public static void main(String[] args) throws IOException, InterruptedException {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        FieldRepository fieldRepo = context.getBean(FieldRepository.class);
        LemmaRepository lemmaRepo = context.getBean(LemmaRepository.class);
        PageRepository pageRepo = context.getBean(PageRepository.class);
        IndexRepository indexRepo = context.getBean(IndexRepository.class);

        //test data
        String sitePath = "https://www.nikoartgallery.com/";
        //String sitePath = "https://www.lutherancathedral.ru/";
        //String sitePath = "http://www.aot.ru/";
        //String sitePath = "http://www.playback.ru/";

        createIndex(fieldRepo, indexRepo, sitePath);



        context.close();
    }


    private static void createIndex(
            FieldRepository fieldRepo,
            IndexRepository indexRepo,
            String sitePath
    ) {
        //if need recreate db
        DBCreator.initDb();

        Parser parser = new Parser(sitePath);
        parser.setFieldForIndex(fieldRepo.findAll());

        ForkJoinPool pool = ForkJoinPool.commonPool();
        pool.invoke(parser);

        List<Index> indexes = Parser.getIndexes();
        indexRepo.saveAll(indexes);
    }

}
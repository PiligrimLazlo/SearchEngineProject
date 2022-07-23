package main;

import main.model.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import main.engine.Parser;
import org.springframework.data.util.Pair;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

@Configuration
@EnableAutoConfiguration
public class Application {


    public static void main(String[] args) throws IOException, InterruptedException {


        //if need recreate db
        DBCreator.initDb();

        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        FieldRepository fieldRepo = context.getBean(FieldRepository.class);
        LemmaRepository lemmaRepo = context.getBean(LemmaRepository.class);
        IndexRepository indexRepo = context.getBean(IndexRepository.class);
        PageRepository pageRepo = context.getBean(PageRepository.class);

        //test data
        Parser parser = new Parser("https://www.nikoartgallery.com/");
        //Parser parser = new Parser("http://www.playback.ru/");
        //Parser parser = new Parser("http://www.uderzo.it/main_products/space_sniffer/index.html", "http://www.uderzo.it/");
        parser.setFieldForIndex(fieldRepo.findAll());

        ForkJoinPool pool = ForkJoinPool.commonPool();
        pool.invoke(parser);

        Iterable<Page> savedPagesIterable = pageRepo.saveAll(Parser.getPages());
        Iterable<Lemma> savedLemmasIterable = lemmaRepo.saveAll(Parser.getLemmas());


        //!!!!
        Map<Pair<String, String>, Index> coldIndexMap = Parser.getColdIndexMap();
        // После записи в базу данных появляется возможность определить идентификаторы леммы и страницы,
        // таким образом можем обновить информацию об индексах
        savedPagesIterable.forEach(savedPage -> {
            savedLemmasIterable.forEach(savedLemma -> {
                Pair<String, String> key = Pair.of(savedPage.getPath(), savedLemma.getLemma());
                Index index = coldIndexMap.get(key);
                if (index != null) {
                    index.setPageId(savedPage.getId());
                    index.setLemmaId(savedLemma.getId());
                }
            });
        });

        indexRepo.saveAll(coldIndexMap.values());


        context.close();
    }

}
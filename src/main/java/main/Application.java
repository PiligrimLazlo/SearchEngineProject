package main;

import com.google.common.collect.Lists;
import main.engine.Searcher;
import main.entities.Index;
import main.repositories.*;
import main.utils.DBCreator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.util.List;

/*@Configuration
@EnableAutoConfiguration*/
@SpringBootApplication
public class Application {

    public static void main(String[] args) throws IOException, InterruptedException {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        FieldRepository fieldRepo = context.getBean(FieldRepository.class);
        LemmaRepository lemmaRepo = context.getBean(LemmaRepository.class);
        PageRepository pageRepo = context.getBean(PageRepository.class);
        IndexRepository indexRepo = context.getBean(IndexRepository.class);
        SiteRepository siteRepo = context.getBean(SiteRepository.class);

        //test data
        String sitePath = "https://www.nikoartgallery.com/";

        //if need recreate db
        DBCreator.initDb();

        /*//create site and put in db
        Site site = DBCombiner.createCurrentSite(siteRepo, sitePath, "Галерея Нико");
        indexing
        List<Index> indexList = DBCombiner.createIndex(fieldRepo, indexRepo, siteRepo, site);

        //test search
        Iterable<Index> siteIndexes = indexRepo.findAll();
        Searcher searcher = new Searcher("свой жизнь", Lists.newArrayList(siteIndexes));

        searcher.getSearchedPageList().forEach(System.out::println);
        context.close();*/
    }


}
package main;

import com.google.common.collect.Lists;
import main.engine.Lemmatizer;
import main.engine.Searcher;
import main.entities.Index;
import main.entities.Lemma;
import main.repositories.*;
import main.utils.DBCreator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/*@Configuration
@EnableAutoConfiguration*/
@SpringBootApplication
public class Application {

    public static void main(String[] args) throws IOException, InterruptedException {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        //if need recreate db
        //DBCreator.initDb();
    }


}
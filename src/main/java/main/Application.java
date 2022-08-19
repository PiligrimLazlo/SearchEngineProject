package main;

import main.utils.DBCreator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

/*@Configuration
@EnableAutoConfiguration*/
@SpringBootApplication
public class Application {

    public static void main(String[] args) throws IOException, InterruptedException {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        //if need recreate db
        DBCreator.initDb();
    }


}
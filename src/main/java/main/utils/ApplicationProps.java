package main.utils;

import lombok.Getter;
import lombok.Setter;
import main.model.Site;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties
public class ApplicationProps {

    @Getter
    @Setter
    private List<Site> sites;

}

package main.model.responses.statistics;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import main.model.Site;
import main.model.Status;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class DetailedStatistics {
    private String url;
    private String name;
    private Status status;
    private long statusTime;
    private String error;
    @JsonProperty("pages")
    private long pages;
    @JsonProperty("lemmas")
    private long lemmas;

    private DetailedStatistics(
            String url,
            String name,
            Status status,
            long statusTime,
            String error,
            long pages,
            long lemmas
    ) {
        this.url = url;
        this.name = name;
        this.status = status;
        this.statusTime = statusTime;
        this.error = error;
        this.pages = pages;//count
        this.lemmas = lemmas;//count
    }

    public static List<DetailedStatistics> getDetailedStatistics(List<Site> sites, long pagesCount, long lemmasCount) {
        return sites.stream().map(site -> new DetailedStatistics(
                site.getUrl(),
                site.getName(),
                site.getStatus(),
                site.getStatusTime().getTime(),
                site.getLastError(),
                pagesCount,
                lemmasCount
        )).collect(Collectors.toList());

    }
}

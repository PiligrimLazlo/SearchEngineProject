package main.entities.responses.statistics;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TotalStatistics {

    private final long sites;//count
    private final long pages;//count
    private final long lemmas;//count
    private final boolean isIndexing;

    public TotalStatistics(long sites, long pages, long lemmas, boolean isIndexing) {
        this.sites = sites;
        this.pages = pages;
        this.lemmas = lemmas;
        this.isIndexing = isIndexing;
    }
}

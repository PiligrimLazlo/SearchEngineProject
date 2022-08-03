package main.model.responses.statistics;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import main.model.Site;

import java.util.List;

@Data
public class StatisticsResponse {
    private boolean result;
    private Statistics statistics;

    public StatisticsResponse(boolean result, Statistics statistics) {
        this.result = result;
        this.statistics = statistics;
    }
}


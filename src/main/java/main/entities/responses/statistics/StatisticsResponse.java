package main.entities.responses.statistics;

import lombok.Data;

@Data
public class StatisticsResponse {
    private boolean result;
    private Statistics statistics;

    public StatisticsResponse(boolean result, Statistics statistics) {
        this.result = result;
        this.statistics = statistics;
    }
}


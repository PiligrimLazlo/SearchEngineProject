package main.model.responses.statistics;

import lombok.Data;

import java.util.List;

@Data
public class Statistics {
    private TotalStatistics total;
    private List<DetailedStatistics> detailed;

    public Statistics(TotalStatistics total, List<DetailedStatistics> detailed) {
        this.total = total;
        this.detailed = detailed;
    }
}

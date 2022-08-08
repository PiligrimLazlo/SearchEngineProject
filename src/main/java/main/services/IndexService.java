package main.services;

import main.entities.responses.IndexResponse;
import main.entities.responses.statistics.StatisticsResponse;

public interface IndexService {
    IndexResponse startIndexing();

    IndexResponse stopIndexing();

    IndexResponse indexPage(String url);

    StatisticsResponse getStatistics();
}

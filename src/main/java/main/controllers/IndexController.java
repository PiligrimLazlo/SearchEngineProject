package main.controllers;

import main.entities.responses.IndexResponse;
import main.entities.responses.statistics.StatisticsResponse;
import main.services.IndexService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {
    private final IndexService indexService;

    public IndexController(IndexService indexService) {
        this.indexService = indexService;
    }


    @GetMapping("/startIndexing")
    public IndexResponse startIndexing() {
        return indexService.startIndexing();
    }

    @GetMapping("/stopIndexing")
    public IndexResponse stopIndexing() {
        return indexService.stopIndexing();
    }

    @PostMapping("/indexPage")
    public IndexResponse indexPage(@RequestParam String url) {
        return indexService.indexPage(url);
    }


    @GetMapping("/statistics")
    public StatisticsResponse getStatistics() {
        return indexService.getStatistics();
    }
}

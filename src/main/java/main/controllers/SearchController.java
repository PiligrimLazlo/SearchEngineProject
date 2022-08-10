package main.controllers;

import main.entities.responses.SearchResponse;
import main.services.IndexService;
import main.services.SearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class SearchController {
    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/search")
    public SearchResponse search(
            @RequestParam String query,
            @RequestParam (required = false) String site,
            @RequestParam(defaultValue = "0", name = "offset") Integer offset,
            @RequestParam(defaultValue = "20", name = "limit") Integer limit
    ) throws IOException {
        return searchService.search(query, site, offset, limit);
    }
}

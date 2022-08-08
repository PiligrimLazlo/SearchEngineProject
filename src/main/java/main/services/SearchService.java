package main.services;

import main.entities.responses.SearchResponse;

public interface SearchService {
    SearchResponse search(String query, String sitePath, int offset, int limit);
}

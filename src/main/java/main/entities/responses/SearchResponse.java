package main.entities.responses;

import lombok.Data;
import main.entities.SearchedPage;

import java.util.List;

@Data
public class SearchResponse {

    private final boolean result;
    private final int count;
    private final String error;
    private final List<SearchedPage> data;

    public SearchResponse(boolean result, int count, List<SearchedPage> data) {
        this.result = result;
        this.count = count;
        this.error = null;
        this.data = data;
    }

    public SearchResponse(boolean result, String error) {
        this.result = result;
        this.count = 0;
        this.error = error;
        this.data = null;
    }
}

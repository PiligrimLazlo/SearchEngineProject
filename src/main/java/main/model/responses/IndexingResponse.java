package main.model.responses;

import lombok.Data;

@Data
public class IndexingResponse {

    private final boolean result;
    private final String error;

    public IndexingResponse(boolean result, String error) {
        this.result = result;
        this.error = error;
    }
}
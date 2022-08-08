package main.entities.responses;

import lombok.Data;

@Data
public class IndexResponse {

    private final boolean result;
    private final String error;

    public IndexResponse(boolean result, String error) {
        this.result = result;
        this.error = error;
    }

    public IndexResponse(boolean result) {
        this.result = result;
        this.error = null;
    }
}
package main.model;

import lombok.Data;

@Data
public class SearchedPage {
    private String uri;
    private String title;
    private String snippet;
    private double relevance;
}

package main.entities;

import lombok.Data;

@Data
public class SearchedPage {
    private String site;
    private String siteName;
    private String uri;
    private String title;
    private String snippet;
    private double relevance;
}

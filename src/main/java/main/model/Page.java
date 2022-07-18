package main.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "page")
@Data
public class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String path;
    private int code;
    private String content;
}

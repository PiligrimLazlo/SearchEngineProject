package main.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "page")
public class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Setter
    private int id;
    @Getter
    @Setter
    private String path;
    @Getter
    @Setter
    private int code;
    @Getter
    @Setter
    private String content;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "page")
    @Column(nullable = false)
    @Getter
    @Setter
    private Set<Index> indexes;
}

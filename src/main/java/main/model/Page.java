package main.model;

import lombok.*;

import javax.persistence.*;
import java.util.Objects;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Page page = (Page) o;
        return id == page.id && code == page.code && Objects.equals(path, page.path) && Objects.equals(content, page.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, path, code, content);
    }
}

package main.entities;

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
    @Column(nullable = false)
    private int id;

    @Getter
    @Setter
    @Column(nullable = false)
    private String path;

    @Getter
    @Setter
    @Column(nullable = false)
    private int code;

    @Getter
    @Setter
    @Column(nullable = false)
    private String content;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "page")
    @Column(nullable = false)
    @Getter
    @Setter
    private Set<Index> indexes;

    @Getter
    @Setter
    @ManyToOne
    private Site site;

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

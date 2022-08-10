package main.entities;

import lombok.*;

import javax.persistence.*;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "lemma")
public class Lemma {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Setter
    @Column(nullable = false)
    private int id;

    @Getter
    @Setter
    @Column(nullable = false)
    private String lemma;

    @Getter
    @Setter
    @Column(nullable = false)
    private int frequency;


    @OneToMany(fetch = FetchType.LAZY, mappedBy = "lemma")
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
        Lemma lemma1 = (Lemma) o;
        return id == lemma1.id && frequency == lemma1.frequency && Objects.equals(lemma, lemma1.lemma);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, lemma, frequency);
    }
}

package main.model;

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
    private int id;
    @Getter
    @Setter
    private String lemma;
    @Getter
    @Setter
    private int frequency;


    @OneToMany(fetch = FetchType.EAGER, mappedBy = "lemma")
    @Column(nullable = false)
    @Getter
    @Setter
    private Set<Index> indexes;

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

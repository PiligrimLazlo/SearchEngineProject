package main.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
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
}

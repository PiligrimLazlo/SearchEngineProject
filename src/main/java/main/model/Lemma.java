package main.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "lemma")
@Data
public class Lemma {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String lemma;
    private int frequency;
}

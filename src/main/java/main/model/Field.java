package main.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "field")
@Data
public class Field {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private String selector;
    private float weight;
}

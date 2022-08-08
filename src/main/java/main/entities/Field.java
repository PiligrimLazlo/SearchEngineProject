package main.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "field")
public class Field {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Setter
    @Column(nullable = false)
    private int id;

    @Getter
    @Setter
    @Column(nullable = false)
    private String name;

    @Getter
    @Setter
    @Column(nullable = false)
    private String selector;

    @Getter
    @Setter
    @Column(nullable = false)
    private float weight;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Field field = (Field) o;
        return id == field.id && Float.compare(field.weight, weight) == 0 && Objects.equals(name, field.name) && Objects.equals(selector, field.selector);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, selector, weight);
    }
}

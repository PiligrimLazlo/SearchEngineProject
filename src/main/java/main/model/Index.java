package main.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "`index`")
@Data
public class Index {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "page_id")
    private int pageId;

    @Column(name = "lemma_id")
    private int lemmaId;

    @Column(name = "`rank`")
    private float rank;
}

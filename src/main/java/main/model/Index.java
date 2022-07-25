package main.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Table(name = "`index`")
public class Index {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Setter
    private int id;

    @ManyToOne (fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "page_id", nullable = false)
    @Getter
    @Setter
    private Page page;

    @ManyToOne (fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "lemma_id", nullable = false)
    @Getter
    @Setter
    private Lemma lemma;

    @Column(name = "`rank`")
    @Getter
    @Setter
    private float rank;
}

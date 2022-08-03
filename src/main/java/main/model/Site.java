package main.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "site")
public class Site {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    @Getter
    @Setter
    private int id;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "enum", nullable = false)
    private Status status;

    @Getter
    @Setter
    @Column(name = "status_time", nullable = false)
    private Date statusTime;

    @Getter
    @Setter
    @Column(name = "last_error")
    @JsonProperty("error")
    private String lastError;

    @Getter
    @Setter
    @Column(nullable = false)
    private String url;

    @Getter
    @Setter
    @Column(nullable = false)
    private String name;

/*    @Getter
    @Setter
    @OneToMany(cascade = CascadeType.ALL)
    private List<Page> pages;

    @Getter
    @Setter
    @OneToMany(cascade = CascadeType.ALL)
    private List<Lemma> lemmas;*/
}

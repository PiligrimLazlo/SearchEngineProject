package main.model;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LemmaRepository extends CrudRepository<Lemma, Integer> {

    @Query(value = "SELECT COUNT(*) from lemma where site_id = :site_id", nativeQuery = true)
    long countLemmas(@Param("site_id") int siteId);
}

package main.repositories;

import main.entities.Lemma;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface LemmaRepository extends CrudRepository<Lemma, Integer> {

    @Query(value = "SELECT COUNT(*) from lemma where site_id = :site_id", nativeQuery = true)
    long countLemmas(@Param("site_id") int siteId);
}

package main.repositories;

import main.entities.Lemma;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LemmaRepository extends CrudRepository<Lemma, Integer> {

    @Query(value = "SELECT COUNT(*) from lemma where site_id = :site_id", nativeQuery = true)
    long countLemmas(@Param("site_id") int siteId);


    @Query(value = "SELECT * FROM lemma WHERE lemma IN :searched_lemmas AND frequency < 20 ORDER BY frequency",
            nativeQuery = true)
    List<Lemma> selectIntersectionOfSearchAndSiteLemmasFromDb
            (@Param("searched_lemmas") String... searchedLemmas);


    @Query(value = "SELECT * FROM lemma WHERE lemma IN :searched_lemmas AND frequency < 100 AND site_id=:site_id ORDER BY frequency",
            nativeQuery = true)
    List<Lemma> selectIntersectionOfSearchAndSiteLemmasFromDb
            (@Param("site_id") int siteId, @Param("searched_lemmas") String... searchedLemmas);
}

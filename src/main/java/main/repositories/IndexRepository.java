package main.repositories;

import main.entities.Index;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IndexRepository extends CrudRepository<Index, Integer> {

    @Query(value = "SELECT * FROM `index` LIMIT :offset, :limit", nativeQuery = true)
    List<Index> selectWithLimitAndOffset(@Param("offset") int offset, @Param("limit") int limit);

    @Query(value = "SELECT * FROM `index` WHERE lemma_id=:lemma_id", nativeQuery = true)
    List<Index> selectIndexByLemmaId(@Param("lemma_id") int lemmaId);
}

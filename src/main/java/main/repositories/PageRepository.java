package main.repositories;


import main.entities.Page;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface PageRepository extends CrudRepository<Page, Integer> {

    @Query(value = "SELECT COUNT(*) from page where site_id = :site_id", nativeQuery = true)
    long countPages(@Param("site_id") int siteId);

}

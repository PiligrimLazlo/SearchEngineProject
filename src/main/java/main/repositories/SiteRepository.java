package main.repositories;

import main.entities.Site;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface SiteRepository extends CrudRepository<Site, Integer> {

    Optional<Site> findByUrl(String url);

}

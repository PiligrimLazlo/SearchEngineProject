package main.model;


import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

public interface PageRepository extends CrudRepository<Page, Integer> {
}

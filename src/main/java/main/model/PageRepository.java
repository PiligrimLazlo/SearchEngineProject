package main.model;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface PageRepository extends CrudRepository<Page, Integer> {


}

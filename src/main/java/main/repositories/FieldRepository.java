package main.repositories;

import main.entities.Field;
import org.springframework.data.repository.CrudRepository;

public interface FieldRepository extends CrudRepository<Field, Integer> {
}

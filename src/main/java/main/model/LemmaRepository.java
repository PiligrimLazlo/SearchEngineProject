package main.model;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface LemmaRepository extends CrudRepository<Lemma, Integer> {

    Optional<Lemma> findByLemma(String lemma);
}

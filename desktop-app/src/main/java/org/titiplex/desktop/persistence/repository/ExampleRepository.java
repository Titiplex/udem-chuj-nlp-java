package org.titiplex.desktop.persistence.repository;

import org.titiplex.desktop.domain.example.Example;

import java.util.List;
import java.util.Optional;

public interface ExampleRepository {
    List<Example> findAll();

    List<Example> search(String term);

    Optional<Example> findById(Long id);

    Example save(Example example);

    void saveAll(List<Example> examples);

    void deleteById(Long id);
}

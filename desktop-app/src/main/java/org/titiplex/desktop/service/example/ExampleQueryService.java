package org.titiplex.desktop.service.example;

import org.titiplex.desktop.domain.example.Example;
import org.titiplex.desktop.persistence.repository.ExampleRepository;

import java.util.List;
import java.util.Optional;

public final class ExampleQueryService {
    private final ExampleRepository exampleRepository;

    public ExampleQueryService(ExampleRepository exampleRepository) {
        this.exampleRepository = exampleRepository;
    }

    public List<Example> listAll() {
        return exampleRepository.findAll();
    }

    public List<Example> search(String term) {
        return exampleRepository.search(term);
    }

    public Optional<Example> findById(Long id) {
        return exampleRepository.findById(id);
    }

    public Example save(Example example) {
        return exampleRepository.save(example);
    }
}

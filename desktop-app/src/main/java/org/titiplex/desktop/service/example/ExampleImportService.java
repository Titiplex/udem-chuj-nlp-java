package org.titiplex.desktop.service.example;

import org.titiplex.desktop.domain.example.Example;
import org.titiplex.desktop.persistence.repository.ExampleRepository;

import java.util.List;

public final class ExampleImportService {
    private final ExampleRepository exampleRepository;

    public ExampleImportService(ExampleRepository exampleRepository) {
        this.exampleRepository = exampleRepository;
    }

    public void importExamples(List<Example> examples) {
        exampleRepository.saveAll(examples);
    }
}

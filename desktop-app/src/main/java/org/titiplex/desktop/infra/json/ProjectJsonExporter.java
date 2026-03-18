package org.titiplex.desktop.infra.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.titiplex.desktop.domain.correction.Correction;
import org.titiplex.desktop.domain.example.Example;
import org.titiplex.desktop.domain.lexicon.LexiconEntry;
import org.titiplex.desktop.domain.rule.Rule;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class ProjectJsonExporter {
    private final ObjectMapper objectMapper;

    public ProjectJsonExporter() {
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .enable(SerializationFeature.INDENT_OUTPUT);
    }

    public void export(
            List<Rule> rules,
            List<Example> examples,
            List<Correction> corrections,
            List<LexiconEntry> lexiconEntries,
            Path outputFile
    ) {
        Map<String, Object> payload = Map.of(
                "rules", rules,
                "examples", examples,
                "corrections", corrections,
                "lexiconEntries", lexiconEntries
        );

        try {
            objectMapper.writeValue(outputFile.toFile(), payload);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to export project JSON", exception);
        }
    }
}

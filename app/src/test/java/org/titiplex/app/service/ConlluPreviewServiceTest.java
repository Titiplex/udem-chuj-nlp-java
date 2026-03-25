package org.titiplex.app.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.titiplex.app.persistence.entity.CorrectedEntry;
import org.titiplex.conllu.AnnotationConfig;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConlluPreviewServiceTest {

    private final ConlluPreviewService service = new ConlluPreviewService();

    @Test
    void previewProducesConlluStringForSingleEntry() {
        CorrectedEntry entry = entry(12L, "ix naq", "A1 ganar", "I beat you");

        String preview = service.preview(entry, new AnnotationConfig());

        assertThat(preview).isNotBlank();
        assertThat(preview).contains("ix");
        assertThat(preview).contains("naq");
    }

    @Test
    void exportWritesPreviewToDisk(@TempDir Path tempDir) throws Exception {
        CorrectedEntry entry = entry(12L, "ix naq", "A1 ganar", "I beat you");
        Path output = tempDir.resolve("one.conllu");

        service.export(entry, new AnnotationConfig(), output);

        assertThat(Files.exists(output)).isTrue();
        assertThat(Files.readString(output)).contains("ix");
    }

    @Test
    void exportAllConcatenatesMultipleEntries(@TempDir Path tempDir) throws Exception {
        Path output = tempDir.resolve("all.conllu");
        List<CorrectedEntry> entries = List.of(
                entry(1L, "ha", "A1", "one"),
                entry(2L, "tin", "win", "two")
        );

        service.exportAll(entries, new AnnotationConfig(), output);

        String body = Files.readString(output);
        assertThat(body).contains("ha");
        assertThat(body).contains("tin");
    }

    private static CorrectedEntry entry(Long id, String rawText, String glossText, String translationText) {
        CorrectedEntry entry = new CorrectedEntry();
        entry.setId(id);
        entry.setRawText(rawText);
        entry.setGlossText(glossText);
        entry.setTranslationText(translationText);
        return entry;
    }
}

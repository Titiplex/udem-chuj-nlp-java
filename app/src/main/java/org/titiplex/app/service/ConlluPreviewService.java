package org.titiplex.app.service;

import org.springframework.stereotype.Service;
import org.titiplex.align.TokenAligner;
import org.titiplex.app.persistence.entity.CorrectedEntry;
import org.titiplex.conllu.AnnotationConfig;
import org.titiplex.conllu.ConlluEntry;
import org.titiplex.io.ConlluWriter;
import org.titiplex.model.CorrectedBlock;
import org.titiplex.pipeline.ConlluPipeline;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Service
public class ConlluPreviewService {

    public String preview(CorrectedEntry entry, AnnotationConfig annotationConfig) {
        return toConlluEntry(entry, annotationConfig).toConlluString();
    }

    public void export(CorrectedEntry entry, AnnotationConfig annotationConfig, Path output) throws IOException {
        new ConlluWriter().writeEntry(output, toConlluEntry(entry, annotationConfig));
    }

    public void exportAll(List<CorrectedEntry> entries, AnnotationConfig annotationConfig, Path output) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (CorrectedEntry entry : entries) {
            sb.append(toConlluEntry(entry, annotationConfig).toConlluString()).append("\n");
        }
        new ConlluWriter().writeRaw(output, sb.toString());
    }

    private ConlluEntry toConlluEntry(CorrectedEntry entry, AnnotationConfig annotationConfig) {
        String chuj = nullToEmpty(entry.getRawText());
        String gloss = nullToEmpty(entry.getGlossText());
        String translation = nullToEmpty(entry.getTranslationText());

        CorrectedBlock block = new CorrectedBlock(
                entry.getId() == null ? 0 : entry.getId().intValue(),
                chuj,
                gloss,
                translation,
                new TokenAligner().align(splitWords(chuj), splitWords(gloss))
        );

        return new ConlluPipeline(annotationConfig).toEntry(block);
    }

    private static List<String> splitWords(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return List.of(value.trim().split("\\s+"));
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
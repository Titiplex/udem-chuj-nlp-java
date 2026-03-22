package org.titiplex.app.service;

import org.springframework.stereotype.Service;
import org.titiplex.align.TokenAligner;
import org.titiplex.app.persistence.entity.CorrectedEntry;
import org.titiplex.conllu.AnnotationConfig;
import org.titiplex.model.CorrectedBlock;
import org.titiplex.pipeline.ConlluPipeline;

import java.util.Collections;

@Service
public class ConlluPreviewService {

    public String preview(CorrectedEntry entry, AnnotationConfig annotationConfig) {
        CorrectedBlock block = new CorrectedBlock(
                Math.toIntExact(entry.getId()),
                entry.getRawText(),
                entry.getGlossText(),
                entry.getTranslationText(),
                new TokenAligner().align(Collections.singletonList(entry.getRawText()), Collections.singletonList(entry.getGlossText()))
        );

        return new ConlluPipeline(annotationConfig)
                .toEntry(block)
                .toConlluString();
    }
}
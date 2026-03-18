package org.titiplex.pipeline;

import org.titiplex.conllu.AnnotationConfig;
import org.titiplex.conllu.AnnotationEngine;
import org.titiplex.conllu.ConlluEntry;
import org.titiplex.model.ConlluSentence;
import org.titiplex.model.ConlluToken;
import org.titiplex.model.CorrectedBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ConlluPipeline {
    private final AnnotationEngine engine;

    public ConlluPipeline() {
        this(new AnnotationConfig());
    }

    public ConlluPipeline(AnnotationConfig config) {
        this.engine = new AnnotationEngine(config);
    }

    public ConlluEntry toEntry(CorrectedBlock block) {
        return engine.annotate(block);
    }

    public ConlluSentence toConllu(CorrectedBlock block) {
        ConlluEntry entry = toEntry(block);
        List<ConlluToken> out = new ArrayList<>();
        for (var line : entry.lines()) {
            out.add(new ConlluToken(
                    line.id(),
                    line.form(),
                    line.lemma(),
                    line.upos(),
                    line.xpos(),
                    Map.copyOf(line.feats()),
                    line.head(),
                    line.deprel(),
                    line.deps(),
                    line.misc()
            ));
        }
        return new ConlluSentence(Integer.toString(block.id()), block.chujText(), out);
    }
}

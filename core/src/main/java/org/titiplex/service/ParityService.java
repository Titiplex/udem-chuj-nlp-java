package org.titiplex.service;

import org.titiplex.align.TokenAligner;
import org.titiplex.conllu.AnnotationConfig;
import org.titiplex.io.BlockReader;
import org.titiplex.io.ConlluWriter;
import org.titiplex.io.CorrectedDocxWriter;
import org.titiplex.model.CorrectedBlock;
import org.titiplex.model.CorrectionEntry;
import org.titiplex.model.RawBlock;
import org.titiplex.pipeline.ConlluPipeline;
import org.titiplex.pipeline.CorrectionPipeline;
import org.titiplex.rules.RuleEngine;
import org.titiplex.stats.CorpusStats;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ParityService {
    private final BlockReader reader;
    private final RuleEngine ruleEngine;
    private final AnnotationConfig annotationConfig;

    public ParityService(BlockReader reader, RuleEngine ruleEngine, AnnotationConfig annotationConfig) {
        this.reader = reader;
        this.ruleEngine = ruleEngine;
        this.annotationConfig = annotationConfig;
    }

    public List<CorrectionEntry> correct(InputStream in) throws IOException {
        CorrectionPipeline correctionPipeline = new CorrectionPipeline(new TokenAligner(), ruleEngine);
        List<CorrectionEntry> out = new ArrayList<>();
        for (RawBlock raw : reader.read(in)) {
            CorrectedBlock corrected = correctionPipeline.process(raw);
            out.add(new CorrectionEntry(raw.id(), raw, corrected));
        }
        return out;
    }

    public void writeCorrectedDocx(InputStream in, Path outDocx) throws IOException {
        new CorrectedDocxWriter().write(outDocx, correct(in));
    }

    public void writeConllu(InputStream in, Path outConllu) throws IOException {
        ConlluPipeline pipeline = new ConlluPipeline(annotationConfig);
        StringBuilder sb = new StringBuilder();
        for (CorrectionEntry entry : correct(in)) {
            sb.append(pipeline.toEntry(entry.corrected()).toConlluString()).append("\n");
        }
        new ConlluWriter().writeRaw(outConllu, sb.toString());
    }

    public void writeStats(InputStream in, Path outTxt) throws IOException {
        CorpusStats stats = new CorpusStats();
        for (CorrectionEntry entry : correct(in)) {
            stats.accept(entry.corrected());
        }
        Files.writeString(outTxt, stats.toReportString(), StandardCharsets.UTF_8);
    }
}

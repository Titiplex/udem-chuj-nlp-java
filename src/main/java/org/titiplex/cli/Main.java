package org.titiplex.cli;

import org.titiplex.align.TokenAligner;
import org.titiplex.conllu.AnnotationConfig;
import org.titiplex.conllu.AnnotationConfigLoader;
import org.titiplex.io.ConlluWriter;
import org.titiplex.io.DocxReader;
import org.titiplex.io.YamlRuleLoader;
import org.titiplex.pipeline.ConlluPipeline;
import org.titiplex.pipeline.CorrectionPipeline;
import org.titiplex.rules.RuleEngine;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 4) {
            System.err.println("Usage: java ... Main <input.docx> <correction.yaml> <pos.yaml> <output.conllu>");
            System.exit(1);
        }

        Path inputDocx = Path.of(args[0]);
        Path rulesYaml = Path.of(args[1]);
        Path posYaml = Path.of(args[2]);
        Path outputConllu = Path.of(args[3]);

        DocxReader docxReader = new DocxReader();
        YamlRuleLoader ruleLoader = new YamlRuleLoader();
        RuleEngine ruleEngine;
        try (FileInputStream ruleIn = new FileInputStream(rulesYaml.toFile())) {
            ruleEngine = new RuleEngine(ruleLoader.load(ruleIn));
        }

        AnnotationConfig annotationConfig;
        try (FileInputStream posIn = new FileInputStream(posYaml.toFile())) {
            annotationConfig = new AnnotationConfigLoader().load(posIn);
        }

        CorrectionPipeline correctionPipeline = new CorrectionPipeline(new TokenAligner(), ruleEngine);
        ConlluPipeline conlluPipeline = new ConlluPipeline(annotationConfig);

        StringBuilder sb = new StringBuilder();

        try (FileInputStream docIn = new FileInputStream(inputDocx.toFile())) {
            for (var rawBlock : docxReader.read(docIn)) {
                var corrected = correctionPipeline.process(rawBlock);
                sb.append(conlluPipeline.toEntry(corrected).toConlluString());
            }
        }

        new ConlluWriter().writeRaw(outputConllu, sb.toString());
    }
}

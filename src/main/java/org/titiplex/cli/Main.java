package org.titiplex.cli;

import org.titiplex.align.TokenAligner;
import org.titiplex.io.ConlluWriter;
import org.titiplex.io.DocxReader;
import org.titiplex.io.YamlRuleLoader;
import org.titiplex.model.ConlluSentence;
import org.titiplex.pipeline.ConlluPipeline;
import org.titiplex.pipeline.CorrectionPipeline;
import org.titiplex.rules.RuleEngine;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.err.println("Usage: java ... Main <input.docx> <rules.yaml> <output.conllu>");
            System.exit(1);
        }

        Path inputDocx = Path.of(args[0]);
        Path rulesYaml = Path.of(args[1]);
        Path outputConllu = Path.of(args[2]);

        DocxReader docxReader = new DocxReader();
        YamlRuleLoader ruleLoader = new YamlRuleLoader();
        RuleEngine ruleEngine;
        try (FileInputStream ruleIn = new FileInputStream(rulesYaml.toFile())) {
            ruleEngine = new RuleEngine(ruleLoader.load(ruleIn));
        }

        CorrectionPipeline correctionPipeline = new CorrectionPipeline(new TokenAligner(), ruleEngine);
        ConlluPipeline conlluPipeline = new ConlluPipeline();
        List<ConlluSentence> sentences = new ArrayList<>();

        try (FileInputStream docIn = new FileInputStream(inputDocx.toFile())) {
            for (var rawBlock : docxReader.read(docIn)) {
                var corrected = correctionPipeline.process(rawBlock);
                sentences.add(conlluPipeline.toConllu(corrected));
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputConllu.toFile()))) {
            new ConlluWriter().write(sentences, writer);
        }
    }
}

package org.titiplex.cli;

import org.titiplex.conllu.AnnotationConfig;
import org.titiplex.conllu.AnnotationConfigLoader;
import org.titiplex.io.BlockReader;
import org.titiplex.io.DocxReader;
import org.titiplex.io.RawTextReader;
import org.titiplex.io.YamlRuleLoader;
import org.titiplex.rules.RuleEngine;
import org.titiplex.service.ParityService;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            usage();
            System.exit(1);
        }

        String command = args[0];
        switch (command) {
            case "prepare" -> runPrepare(args);
            case "correct-docx" -> runCorrectDocx(args);
            case "stats" -> runStats(args);
            default -> {
                if (args.length == 4) {
                    // backward-compatible mode
                    runPrepare(new String[]{"prepare", args[0], args[1], args[2], args[3]});
                } else {
                    usage();
                    System.exit(1);
                }
            }
        }
    }

    private static void runPrepare(String[] args) throws IOException {
        if (args.length < 5) {
            System.err.println("Usage: prepare <input.docx|input.txt> <correction.yaml> <pos.yaml> <output.conllu>");
            System.exit(1);
        }
        Path inputPath = Path.of(args[1]);
        Path rulesYaml = Path.of(args[2]);
        Path posYaml = Path.of(args[3]);
        Path outputConllu = Path.of(args[4]);
        ParityService service = serviceFor(inputPath, rulesYaml, posYaml);
        try (FileInputStream in = new FileInputStream(inputPath.toFile())) {
            service.writeConllu(in, outputConllu);
        }
    }

    private static void runCorrectDocx(String[] args) throws IOException {
        if (args.length < 4) {
            System.err.println("Usage: correct-docx <input.docx|input.txt> <correction.yaml> <output.docx>");
            System.exit(1);
        }
        Path inputPath = Path.of(args[1]);
        Path rulesYaml = Path.of(args[2]);
        Path outputDocx = Path.of(args[3]);
        ParityService service = serviceFor(inputPath, rulesYaml, null);
        try (FileInputStream in = new FileInputStream(inputPath.toFile())) {
            service.writeCorrectedDocx(in, outputDocx);
        }
    }

    private static void runStats(String[] args) throws IOException {
        if (args.length < 4) {
            System.err.println("Usage: stats <input.docx|input.txt> <correction.yaml> <output.txt>");
            System.exit(1);
        }
        Path inputPath = Path.of(args[1]);
        Path rulesYaml = Path.of(args[2]);
        Path outputTxt = Path.of(args[3]);
        ParityService service = serviceFor(inputPath, rulesYaml, null);
        try (FileInputStream in = new FileInputStream(inputPath.toFile())) {
            service.writeStats(in, outputTxt);
        }
    }

    private static ParityService serviceFor(Path inputPath, Path rulesYaml, Path posYaml) throws IOException {
        BlockReader reader = inputPath.toString().toLowerCase().endsWith(".docx") ? new DocxReader() : new RawTextReader();
        YamlRuleLoader ruleLoader = new YamlRuleLoader();
        RuleEngine ruleEngine;
        try (FileInputStream ruleIn = new FileInputStream(rulesYaml.toFile())) {
            ruleEngine = new RuleEngine(ruleLoader.load(ruleIn));
        }
        AnnotationConfig annotationConfig = new AnnotationConfig();
        if (posYaml != null) {
            annotationConfig = new AnnotationConfigLoader().load(posYaml);
        }
        return new ParityService(reader, ruleEngine, annotationConfig);
    }

    private static void usage() {
        System.err.println("""
                Commands:
                  prepare <input.docx|input.txt> <correction.yaml> <pos.yaml> <output.conllu>
                  correct-docx <input.docx|input.txt> <correction.yaml> <output.docx>
                  stats <input.docx|input.txt> <correction.yaml> <output.txt>""");
    }
}

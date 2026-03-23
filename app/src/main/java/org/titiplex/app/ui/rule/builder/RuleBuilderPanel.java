package org.titiplex.app.ui.rule.builder;

import org.titiplex.app.persistence.entity.RuleKind;
import org.yaml.snakeyaml.Yaml;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RuleBuilderPanel extends JPanel {
    private final JComboBox<RuleKind> kindBox = new JComboBox<>(RuleKind.values());
    private final JTextField idField = new JTextField();
    private final JTextField nameField = new JTextField();
    private final JTextArea descriptionArea = new JTextArea(3, 60);

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);

    // correction
    private final JComboBox<String> correctionTypeBox = new JComboBox<>(new String[]{
            "rewrite_before_after",
            "regex_sub",
            "split_suffix",
            "delete_chars"
    });
    private final JTextField correctionMatchGlossField = new JTextField();
    private final JTextField correctionBeforeField = new JTextField();
    private final JTextField correctionAfterField = new JTextField();
    private final JTextField correctionRegexPatternField = new JTextField();
    private final JTextField correctionRegexReplacementField = new JTextField();
    private final JTextField correctionSuffixesField = new JTextField();
    private final JTextField correctionDeleteCharsField = new JTextField();

    // conllu
    private final JComboBox<String> conlluTypeBox = new JComboBox<>(new String[]{
            "set_upos",
            "set_feat",
            "extract_only"
    });
    private final JTextField conlluMatchGlossField = new JTextField();
    private final JTextField conlluMatchRegexField = new JTextField();
    private final JTextField conlluLexiconNameField = new JTextField();
    private final JTextField conlluLexiconFileField = new JTextField();
    private final JTextField conlluUposField = new JTextField();
    private final JTextField conlluFeatKeyField = new JTextField();
    private final JTextField conlluFeatValueField = new JTextField();
    private final JTextField conlluExtractorNameField = new JTextField("agreement_verbs");
    private final JTextArea conlluExtractorIncludeArea = new JTextArea(4, 40);
    private final JTextArea conlluFeatTemplateArea = new JTextArea(5, 40);

    private final JTextArea previewArea = new JTextArea(20, 80);

    public RuleBuilderPanel() {
        setLayout(new BorderLayout(8, 8));

        previewArea.setEditable(false);
        previewArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        JPanel top = new JPanel(new GridBagLayout());
        top.setBorder(BorderFactory.createTitledBorder("Guided rule builder"));
        GridBagConstraints c = baseConstraints();

        addRow(top, c, 0, "Rule kind", kindBox);
        addRow(top, c, 1, "Rule id", idField);
        addRow(top, c, 2, "Name", nameField);
        addRow(top, c, 3, "Description", new JScrollPane(descriptionArea));

        cards.add(buildCorrectionCard(), RuleKind.CORRECTION.name());
        cards.add(buildConlluCard(), RuleKind.CONLLU.name());

        kindBox.addActionListener(event -> switchKindCard());

        JButton generateButton = new JButton("Generate YAML");
        generateButton.addActionListener(event -> previewArea.setText(generateYaml()));

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.add(cards, BorderLayout.CENTER);
        center.add(generateButton, BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(new JScrollPane(previewArea), BorderLayout.SOUTH);

        switchKindCard();
    }

    public RuleKind getSelectedKind() {
        return (RuleKind) kindBox.getSelectedItem();
    }

    public String generateYaml() {
        RuleKind kind = getSelectedKind();
        if (kind == RuleKind.CONLLU) {
            return generateConlluYaml();
        }
        return generateCorrectionYaml();
    }

    private JPanel buildCorrectionCard() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Correction rule"));
        GridBagConstraints c = baseConstraints();

        addRow(panel, c, 0, "Correction type", correctionTypeBox);
        addRow(panel, c, 1, "Match gloss", correctionMatchGlossField);
        addRow(panel, c, 2, "Before (comma-separated)", correctionBeforeField);
        addRow(panel, c, 3, "After (comma-separated)", correctionAfterField);
        addRow(panel, c, 4, "Regex pattern", correctionRegexPatternField);
        addRow(panel, c, 5, "Regex replacement", correctionRegexReplacementField);
        addRow(panel, c, 6, "Suffixes (comma-separated)", correctionSuffixesField);
        addRow(panel, c, 7, "Chars to delete (comma-separated)", correctionDeleteCharsField);

        return panel;
    }

    private JPanel buildConlluCard() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("CoNLL-U rule"));
        GridBagConstraints c = baseConstraints();

        addRow(panel, c, 0, "CoNLL-U type", conlluTypeBox);
        addRow(panel, c, 1, "Match gloss", conlluMatchGlossField);
        addRow(panel, c, 2, "Match regex", conlluMatchRegexField);
        addRow(panel, c, 3, "Lexicon name", conlluLexiconNameField);
        addRow(panel, c, 4, "Lexicon file path", conlluLexiconFileField);
        addRow(panel, c, 5, "UPOS", conlluUposField);
        addRow(panel, c, 6, "Feat key", conlluFeatKeyField);
        addRow(panel, c, 7, "Feat value", conlluFeatValueField);
        addRow(panel, c, 8, "Extractor name", conlluExtractorNameField);
        addRow(panel, c, 9, "Extractor include file(s), one per line", new JScrollPane(conlluExtractorIncludeArea));
        addRow(panel, c, 10, "Feat templates (key=value per line)", new JScrollPane(conlluFeatTemplateArea));

        return panel;
    }

    private void switchKindCard() {
        RuleKind kind = getSelectedKind();
        cardLayout.show(cards, kind.name());
    }

    private String generateCorrectionYaml() {
        String id = require(idField.getText(), "Rule id cannot be blank");
        String name = require(nameField.getText(), "Rule name cannot be blank");
        String type = String.valueOf(correctionTypeBox.getSelectedItem());

        Map<String, Object> rule = new LinkedHashMap<>();
        rule.put("id", id.trim());
        rule.put("name", name.trim());
        putIfNotBlank(rule, "description", descriptionArea.getText());

        if (!correctionMatchGlossField.getText().isBlank()) {
            Map<String, Object> match = new LinkedHashMap<>();
            match.put("gloss", correctionMatchGlossField.getText().trim());
            rule.put("match", match);
        }

        Map<String, Object> rewrite = new LinkedHashMap<>();
        switch (type) {
            case "rewrite_before_after" -> {
                rewrite.put("before", csv(correctionBeforeField.getText()));
                rewrite.put("after", csv(correctionAfterField.getText()));
            }
            case "regex_sub" -> {
                Map<String, Object> regexSub = new LinkedHashMap<>();
                regexSub.put("scope", "chuj");
                regexSub.put("pattern", correctionRegexPatternField.getText().trim());
                regexSub.put("repl", correctionRegexReplacementField.getText().trim());
                rewrite.put("regex_sub", regexSub);
            }
            case "split_suffix" -> {
                Map<String, Object> split = new LinkedHashMap<>();
                split.put("type", "suffix");
                split.put("suffixes", csv(correctionSuffixesField.getText()));
                rewrite.put("split", split);
            }
            case "delete_chars" -> {
                Map<String, Object> delete = new LinkedHashMap<>();
                delete.put("type", "chars");
                delete.put("chars", csv(correctionDeleteCharsField.getText()));
                rewrite.put("delete", delete);
            }
            default -> throw new IllegalStateException("Unsupported correction type: " + type);
        }

        rule.put("rewrite", rewrite);

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("rules", List.of(rule));
        return new Yaml().dump(root);
    }

    private String generateConlluYaml() {
        String id = require(idField.getText(), "Rule id cannot be blank");
        String name = require(nameField.getText(), "Rule name cannot be blank");
        String type = String.valueOf(conlluTypeBox.getSelectedItem());

        Map<String, Object> root = new LinkedHashMap<>();
        Map<String, Object> rule = new LinkedHashMap<>();
        rule.put("id", id.trim());
        rule.put("name", name.trim());
        putIfNotBlank(rule, "description", descriptionArea.getText());

        Map<String, Object> match = new LinkedHashMap<>();
        Map<String, Object> gloss = new LinkedHashMap<>();

        if (!conlluMatchGlossField.getText().isBlank()) {
            rule.put("match", Map.of("gloss", conlluMatchGlossField.getText().trim()));
        } else {
            if (!conlluMatchRegexField.getText().isBlank()) {
                gloss.put("regex", conlluMatchRegexField.getText().trim());
            }
            if (!conlluLexiconNameField.getText().isBlank()) {
                gloss.put("in_lexicon", conlluLexiconNameField.getText().trim());
            }
            if (!gloss.isEmpty()) {
                match.put("gloss", gloss);
                rule.put("match", match);
            }
        }

        if (!conlluLexiconNameField.getText().isBlank() && !conlluLexiconFileField.getText().isBlank()) {
            Map<String, Object> lexicons = new LinkedHashMap<>();
            lexicons.put(conlluLexiconNameField.getText().trim(), conlluLexiconFileField.getText().trim());
            root.put("lexicons", lexicons);
        }

        List<String> extractorFiles = nonBlankLines(conlluExtractorIncludeArea.getText());
        if (!extractorFiles.isEmpty()) {
            root.put("extractors_file", extractorFiles);
        }

        Map<String, Object> set = new LinkedHashMap<>();
        switch (type) {
            case "set_upos" -> set.put("upos", require(conlluUposField.getText(), "UPOS cannot be blank").trim());
            case "set_feat" -> {
                Map<String, Object> feats = new LinkedHashMap<>();
                feats.put(
                        require(conlluFeatKeyField.getText(), "Feat key cannot be blank").trim(),
                        require(conlluFeatValueField.getText(), "Feat value cannot be blank").trim()
                );
                set.put("feats", feats);
            }
            case "extract_only" -> {
                List<Map<String, String>> extracts = new ArrayList<>();
                Map<String, String> extract = new LinkedHashMap<>();
                extract.put("type", "scan_agreement");
                extract.put("extractor", require(conlluExtractorNameField.getText(), "Extractor name cannot be blank").trim());
                extracts.add(extract);
                set.put("extract", extracts);

                Map<String, String> featsTemplate = parseKeyValueLines(conlluFeatTemplateArea.getText());
                if (!featsTemplate.isEmpty()) {
                    set.put("feats_template", featsTemplate);
                }
            }
            default -> throw new IllegalStateException("Unsupported CoNLL-U type: " + type);
        }

        rule.put("set", set);
        root.put("rules", List.of(rule));
        return new Yaml().dump(root);
    }

    private static GridBagConstraints baseConstraints() {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 1;
        return c;
    }

    private static void addRow(JPanel panel, GridBagConstraints c, int row, String label, Component component) {
        c.gridx = 0;
        c.gridy = row;
        c.weightx = 0;
        panel.add(new JLabel(label), c);

        c.gridx = 1;
        c.weightx = 1;
        panel.add(component, c);
    }

    private static void putIfNotBlank(Map<String, Object> map, String key, String value) {
        if (value != null && !value.isBlank()) {
            map.put(key, value.trim());
        }
    }

    private static String require(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private static List<String> csv(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return List.of(text.trim().split("\\s*,\\s*"));
    }

    private static List<String> nonBlankLines(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return text.lines()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    private static Map<String, String> parseKeyValueLines(String text) {
        Map<String, String> out = new LinkedHashMap<>();
        for (String line : nonBlankLines(text)) {
            int idx = line.indexOf('=');
            if (idx <= 0 || idx == line.length() - 1) {
                continue;
            }
            out.put(line.substring(0, idx).trim(), line.substring(idx + 1).trim());
        }
        return out;
    }
}
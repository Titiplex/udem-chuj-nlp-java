package org.titiplex.app.ui.rule.builder;

import org.yaml.snakeyaml.Yaml;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RuleBuilderPanel extends JPanel {
    private final JTextField idField = new JTextField();
    private final JTextField nameField = new JTextField();

    private final JComboBox<String> typeBox = new JComboBox<>(new String[]{
            "rewrite_before_after",
            "regex_sub",
            "split_suffix",
            "delete_chars"
    });

    private final JTextField matchGlossField = new JTextField();
    private final JTextField beforeField = new JTextField();
    private final JTextField afterField = new JTextField();
    private final JTextField regexPatternField = new JTextField();
    private final JTextField regexReplacementField = new JTextField();
    private final JTextField suffixesField = new JTextField();
    private final JTextField deleteCharsField = new JTextField();

    private final JTextArea previewArea = new JTextArea(18, 60);

    public RuleBuilderPanel() {
        setLayout(new BorderLayout(8, 8));

        previewArea.setEditable(false);
        previewArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Guided rule builder"));

        form.add(new JLabel("Rule id"));
        form.add(idField);

        form.add(new JLabel("Name"));
        form.add(nameField);

        form.add(new JLabel("Type"));
        form.add(typeBox);

        form.add(new JLabel("Match gloss (optional)"));
        form.add(matchGlossField);

        form.add(new JLabel("Before (comma-separated)"));
        form.add(beforeField);

        form.add(new JLabel("After (comma-separated)"));
        form.add(afterField);

        form.add(new JLabel("Regex pattern"));
        form.add(regexPatternField);

        form.add(new JLabel("Regex replacement"));
        form.add(regexReplacementField);

        form.add(new JLabel("Suffixes (comma-separated)"));
        form.add(suffixesField);

        form.add(new JLabel("Chars to delete (comma-separated)"));
        form.add(deleteCharsField);

        JButton generateButton = new JButton("Generate YAML");
        generateButton.addActionListener(event -> previewArea.setText(generateYaml()));

        add(form, BorderLayout.NORTH);
        add(generateButton, BorderLayout.CENTER);
        add(new JScrollPane(previewArea), BorderLayout.SOUTH);
    }

    public String generateYaml() {
        String id = idField.getText().trim();
        String name = nameField.getText().trim();
        String type = (String) typeBox.getSelectedItem();

        if (id.isBlank()) {
            throw new IllegalArgumentException("Rule id cannot be blank");
        }
        if (name.isBlank()) {
            throw new IllegalArgumentException("Rule name cannot be blank");
        }

        Map<String, Object> rule = new LinkedHashMap<>();
        rule.put("id", id);
        rule.put("name", name);

        if (!matchGlossField.getText().trim().isBlank()) {
            Map<String, Object> match = new LinkedHashMap<>();
            match.put("gloss", matchGlossField.getText().trim());
            rule.put("match", match);
        }

        Map<String, Object> rewrite = new LinkedHashMap<>();

        switch (type) {
            case "rewrite_before_after" -> {
                rewrite.put("before", csv(beforeField.getText()));
                rewrite.put("after", csv(afterField.getText()));
            }
            case "regex_sub" -> {
                Map<String, Object> regexSub = new LinkedHashMap<>();
                regexSub.put("scope", "chuj");
                regexSub.put("pattern", regexPatternField.getText().trim());
                regexSub.put("repl", regexReplacementField.getText().trim());
                rewrite.put("regex_sub", regexSub);
            }
            case "split_suffix" -> {
                Map<String, Object> split = new LinkedHashMap<>();
                split.put("type", "suffix");
                split.put("suffixes", csv(suffixesField.getText()));
                rewrite.put("split", split);
            }
            case "delete_chars" -> {
                Map<String, Object> delete = new LinkedHashMap<>();
                delete.put("type", "chars");
                delete.put("chars", csv(deleteCharsField.getText()));
                rewrite.put("delete", delete);
            }
            default -> throw new IllegalStateException("Unsupported builder type: " + type);
        }

        rule.put("rewrite", rewrite);

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("rules", List.of(rule));

        return new Yaml().dump(root);
    }

    private List<String> csv(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return List.of(text.trim().split("\\s*,\\s*"));
    }
}
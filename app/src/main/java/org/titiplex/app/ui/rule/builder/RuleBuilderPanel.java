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
            "delete_chars",
            "rewrite_before_after",
            "regex_sub",
            "split_suffix",
            "merge_sequence"
    });

    private final JTextArea payloadArea = new JTextArea(12, 60);
    private final JTextArea previewArea = new JTextArea(16, 60);

    public RuleBuilderPanel() {
        setLayout(new BorderLayout(8, 8));
        payloadArea.setBorder(BorderFactory.createTitledBorder("Rule parameters"));
        previewArea.setBorder(BorderFactory.createTitledBorder("Generated YAML"));
        previewArea.setEditable(false);

        JButton generateButton = new JButton("Generate YAML");
        generateButton.addActionListener(e -> previewArea.setText(generateYaml()));

        JPanel top = new JPanel(new GridLayout(4, 1, 6, 6));
        top.add(labeled("Id", idField));
        top.add(labeled("Name", nameField));
        top.add(labeled("Type", typeBox));
        top.add(generateButton);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(payloadArea), BorderLayout.CENTER);
        add(new JScrollPane(previewArea), BorderLayout.SOUTH);
    }

    private JPanel labeled(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(6, 0));
        p.add(new JLabel(label), BorderLayout.WEST);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private String generateYaml() {
        String id = idField.getText().trim();
        String name = nameField.getText().trim();
        String type = (String) typeBox.getSelectedItem();

        Map<String, Object> rule = new LinkedHashMap<>();
        rule.put("id", id);
        rule.put("name", name);

        Map<String, Object> rewrite = new LinkedHashMap<>();

        switch (type) {
            case "delete_chars" -> {
                Map<String, Object> delete = new LinkedHashMap<>();
                delete.put("type", "chars");
                delete.put("chars", List.of(payloadArea.getText().trim().split("\\s*,\\s*")));
                rewrite.put("delete", delete);
            }
            case "regex_sub" -> {
                Map<String, Object> regex = new LinkedHashMap<>();
                regex.put("scope", "chuj");
                regex.put("pattern", payloadArea.getText().trim());
                regex.put("repl", "");
                rewrite.put("regex_sub", regex);
            }
        }

        if (!rewrite.isEmpty()) {
            rule.put("rewrite", rewrite);
        }

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("rules", List.of(rule));

        return new Yaml().dump(root);
    }
}
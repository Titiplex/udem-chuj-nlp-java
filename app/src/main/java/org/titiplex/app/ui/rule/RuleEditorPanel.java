package org.titiplex.app.ui.rule;

import org.titiplex.app.domain.rule.RuleId;
import org.titiplex.app.domain.rule.RuleVersion;
import org.titiplex.app.persistence.entity.Rule;

import javax.swing.*;
import java.awt.*;
import java.time.Instant;

public final class RuleEditorPanel extends JPanel {
    private final JTextField ruleIdField = new JTextField();
    private final JTextField nameField = new JTextField();
    private final JTextField sourceField = new JTextField();
    private final JTextField versionField = new JTextField();
    private final JTextArea descriptionArea = new JTextArea(5, 80);
    private final JTextArea yamlArea = new JTextArea(24, 80);
    private final JCheckBox enabledBox = new JCheckBox("Enabled", true);

    private Rule currentRule;

    public RuleEditorPanel() {
        setLayout(new BorderLayout(8, 8));

        sourceField.setEditable(false);
        versionField.setEditable(false);

        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        yamlArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
    }

    private JComponent buildHeader() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Rule metadata"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0;
        c.gridx = 0;
        c.gridy = 0;
        panel.add(new JLabel("Rule id"), c);

        c.gridx = 1;
        c.weightx = 1;
        panel.add(ruleIdField, c);

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        panel.add(new JLabel("Name"), c);

        c.gridx = 1;
        c.weightx = 1;
        panel.add(nameField, c);

        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0;
        panel.add(new JLabel("Source"), c);

        c.gridx = 1;
        c.weightx = 1;
        panel.add(sourceField, c);

        c.gridx = 0;
        c.gridy = 3;
        c.weightx = 0;
        panel.add(new JLabel("Version"), c);

        c.gridx = 1;
        c.weightx = 1;
        panel.add(versionField, c);

        c.gridx = 1;
        c.gridy = 4;
        panel.add(enabledBox, c);

        return panel;
    }

    private JComponent buildCenter() {
        JSplitPane splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                wrap("YAML body", yamlArea),
                wrap("Description", descriptionArea)
        );
        splitPane.setResizeWeight(0.78);
        return splitPane;
    }

    private JComponent wrap(String title, JTextArea area) {
        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setBorder(BorderFactory.createTitledBorder(title));
        return scrollPane;
    }

    public void setRule(Rule rule) {
        this.currentRule = rule;

        if (rule == null) {
            ruleIdField.setText("");
            nameField.setText("");
            sourceField.setText("desktop-editor");
            versionField.setText("1");
            descriptionArea.setText("");
            yamlArea.setText("rules:\n  - id: new_rule\n    name: New rule\n");
            enabledBox.setSelected(true);
            return;
        }

        ruleIdField.setText(rule.getStableId());
        nameField.setText(rule.getName());
        sourceField.setText(rule.getSourceFile());
        versionField.setText(String.valueOf(rule.getVersionNo()));
        descriptionArea.setText(rule.getDescription());
        yamlArea.setText(rule.getYamlBody());
        enabledBox.setSelected(rule.isEnabled());
    }

    public void setYamlBody(String yamlBody) {
        yamlArea.setText(yamlBody == null ? "" : yamlBody);
    }

    public Rule toRule() {
        String stableId = new RuleId(ruleIdField.getText().trim()).toString();
        String name = nameField.getText().trim();
        if (name.isBlank()) {
            throw new IllegalArgumentException("Rule name cannot be blank");
        }

        Instant now = Instant.now();
        return new Rule(
                currentRule == null ? null : currentRule.getId(),
                stableId,
                name,
                enabledBox.isSelected(),
                yamlArea.getText(),
                currentRule == null ? "desktop-editor" : currentRule.getSourceFile(),
                descriptionArea.getText().trim(),
                currentRule == null ? new RuleVersion(1).value() : currentRule.getVersionNo(),
                currentRule == null ? now : currentRule.getCreatedAt(),
                now
        );
    }
}
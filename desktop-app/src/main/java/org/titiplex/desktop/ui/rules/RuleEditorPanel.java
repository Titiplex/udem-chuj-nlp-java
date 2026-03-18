package org.titiplex.desktop.ui.rules;

import org.titiplex.desktop.domain.rule.Rule;
import org.titiplex.desktop.domain.rule.RuleId;
import org.titiplex.desktop.domain.rule.RuleVersion;

import javax.swing.*;
import java.awt.*;
import java.time.Instant;

public final class RuleEditorPanel extends JPanel {
    private final JTextField ruleIdField = new JTextField();
    private final JTextField nameField = new JTextField();
    private final JTextArea descriptionArea = new JTextArea(4, 80);
    private final JTextArea yamlArea = new JTextArea(20, 80);
    private final JCheckBox enabledBox = new JCheckBox("Enabled", true);

    private Rule currentRule;

    public RuleEditorPanel() {
        setLayout(new BorderLayout(8, 8));

        JPanel header = new JPanel(new GridLayout(3, 1, 8, 8));
        header.setBorder(BorderFactory.createTitledBorder("Rule metadata"));
        header.add(ruleIdField);
        header.add(nameField);
        header.add(enabledBox);

        descriptionArea.setBorder(BorderFactory.createTitledBorder("Description"));
        yamlArea.setBorder(BorderFactory.createTitledBorder("YAML body"));

        add(header, BorderLayout.NORTH);
        add(new JScrollPane(yamlArea), BorderLayout.CENTER);
        add(new JScrollPane(descriptionArea), BorderLayout.SOUTH);
    }

    public void setRule(Rule rule) {
        this.currentRule = rule;
        if (rule == null) {
            ruleIdField.setText("");
            nameField.setText("");
            descriptionArea.setText("");
            yamlArea.setText("");
            enabledBox.setSelected(true);
            return;
        }

        ruleIdField.setText(rule.ruleId().value());
        nameField.setText(rule.name());
        descriptionArea.setText(rule.description());
        yamlArea.setText(rule.yamlBody());
        enabledBox.setSelected(rule.enabled());
    }

    public Rule toRule() {
        Instant now = Instant.now();
        return new Rule(
                currentRule == null ? null : currentRule.id(),
                new RuleId(ruleIdField.getText().trim()),
                nameField.getText().trim(),
                enabledBox.isSelected(),
                yamlArea.getText(),
                currentRule == null ? "desktop-editor" : currentRule.sourceFile(),
                descriptionArea.getText(),
                currentRule == null ? new RuleVersion(1) : currentRule.version(),
                currentRule == null ? now : currentRule.createdAt(),
                now
        );
    }
}

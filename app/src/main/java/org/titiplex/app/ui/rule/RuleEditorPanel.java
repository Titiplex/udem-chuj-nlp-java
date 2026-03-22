package org.titiplex.app.ui.rule;

import org.titiplex.app.domain.rule.RuleId;
import org.titiplex.app.domain.rule.RuleVersion;
import org.titiplex.app.persistence.entity.Rule;
import org.titiplex.app.ui.common.JPlaceholderTextField;

import javax.swing.*;
import java.awt.*;
import java.time.Instant;

public final class RuleEditorPanel extends JPanel {
    private final JPlaceholderTextField ruleIdField = new JPlaceholderTextField("Rule id");
    private final JPlaceholderTextField nameField = new JPlaceholderTextField("Rule name");
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

        ruleIdField.setText(rule.getStableId());
        nameField.setText(rule.getName());
        descriptionArea.setText(rule.getDescription());
        yamlArea.setText(rule.getYamlBody());
        enabledBox.setSelected(rule.isEnabled());
    }

    public Rule toRule() {
        Instant now = Instant.now();
        return new Rule(
                currentRule == null ? null : currentRule.getId(),
                new RuleId(ruleIdField.getText().trim()).toString(),
                nameField.getText().trim(),
                enabledBox.isSelected(),
                yamlArea.getText(),
                currentRule == null ? "desktop-editor" : currentRule.getSourceFile(),
                descriptionArea.getText(),
                currentRule == null ? new RuleVersion(1).value() : currentRule.getVersionNo(),
                currentRule == null ? now : currentRule.getCreatedAt(),
                now
        );
    }
}

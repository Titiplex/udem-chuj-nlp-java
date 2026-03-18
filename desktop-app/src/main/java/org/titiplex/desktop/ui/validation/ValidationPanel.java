package org.titiplex.desktop.ui.validation;

import org.titiplex.desktop.domain.validation.ValidationRun;
import org.titiplex.desktop.service.correction.RuleApplicationService;
import org.titiplex.desktop.service.rule.RuleValidationService;

import javax.swing.*;
import java.awt.*;

public final class ValidationPanel extends JPanel {
    private final RuleValidationService ruleValidationService;
    private final RuleApplicationService ruleApplicationService;
    private final JTextArea textArea = new JTextArea();

    public ValidationPanel(
            RuleValidationService ruleValidationService,
            RuleApplicationService ruleApplicationService
    ) {
        this.ruleValidationService = ruleValidationService;
        this.ruleApplicationService = ruleApplicationService;

        setLayout(new BorderLayout());

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        JButton validateButton = new JButton("Validate rules");
        JButton dryRunButton = new JButton("Dry run");
        toolBar.add(validateButton);
        toolBar.add(dryRunButton);

        validateButton.addActionListener(event -> render(ruleValidationService.validateAll()));
        dryRunButton.addActionListener(event -> render(ruleApplicationService.dryRunRules()));

        add(toolBar, BorderLayout.NORTH);
        add(new JScrollPane(textArea), BorderLayout.CENTER);
    }

    private void render(ValidationRun run) {
        StringBuilder builder = new StringBuilder();
        builder.append(run.summary()).append("\n\n");
        run.messages().forEach(message -> builder
                .append("[")
                .append(message.severity())
                .append("] ")
                .append(message.message())
                .append("\n"));
        textArea.setText(builder.toString());
    }
}

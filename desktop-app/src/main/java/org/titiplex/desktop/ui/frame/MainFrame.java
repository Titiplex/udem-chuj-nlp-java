package org.titiplex.desktop.ui.frame;

import org.titiplex.desktop.bootstrap.AppConfig;
import org.titiplex.desktop.service.correction.CorrectionService;
import org.titiplex.desktop.service.correction.RuleApplicationService;
import org.titiplex.desktop.service.example.ExampleImportService;
import org.titiplex.desktop.service.example.ExampleQueryService;
import org.titiplex.desktop.service.project.ProjectExportService;
import org.titiplex.desktop.service.rule.RuleEditorService;
import org.titiplex.desktop.service.rule.RuleExportService;
import org.titiplex.desktop.service.rule.RuleImportService;
import org.titiplex.desktop.service.rule.RuleValidationService;
import org.titiplex.desktop.ui.common.Dialogs;
import org.titiplex.desktop.ui.corrections.CorrectionPanel;
import org.titiplex.desktop.ui.examples.ExamplePanel;
import org.titiplex.desktop.ui.rules.RulePanel;
import org.titiplex.desktop.ui.validation.ValidationPanel;

import javax.swing.*;
import java.awt.*;

public final class MainFrame extends JFrame {
    private final JLabel statusLabel = new JLabel("Ready");

    public MainFrame(
            AppConfig config,
            RuleImportService ruleImportService,
            RuleEditorService ruleEditorService,
            RuleValidationService ruleValidationService,
            RuleExportService ruleExportService,
            ExampleImportService exampleImportService,
            ExampleQueryService exampleQueryService,
            RuleApplicationService ruleApplicationService,
            CorrectionService correctionService,
            ProjectExportService projectExportService
    ) {
        super(config.appName());

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        RulePanel rulePanel = new RulePanel(ruleImportService, ruleEditorService, ruleExportService);
        ExamplePanel examplePanel = new ExamplePanel(exampleQueryService);
        CorrectionPanel correctionPanel = new CorrectionPanel(correctionService);
        ValidationPanel validationPanel = new ValidationPanel(ruleValidationService, ruleApplicationService);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Rules", rulePanel);
        tabs.addTab("Examples", examplePanel);
        tabs.addTab("Corrections", correctionPanel);
        tabs.addTab("Validation", validationPanel);

        add(tabs, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem exportProjectItem = new JMenuItem("Export project JSON");
        exportProjectItem.addActionListener(event -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            try {
                projectExportService.exportProject(chooser.getSelectedFile().toPath());
                statusLabel.setText("Project exported.");
            } catch (Exception exception) {
                Dialogs.error(this, "Failed to export project", exception);
            }
        });
        fileMenu.add(exportProjectItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
    }
}

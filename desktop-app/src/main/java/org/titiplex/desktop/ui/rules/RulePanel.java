package org.titiplex.desktop.ui.rules;

import org.titiplex.desktop.service.rule.RuleEditorService;
import org.titiplex.desktop.service.rule.RuleExportService;
import org.titiplex.desktop.service.rule.RuleImportService;
import org.titiplex.desktop.ui.common.Dialogs;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public final class RulePanel extends JPanel {
    private final RuleTableModel tableModel = new RuleTableModel();
    private final RuleEditorPanel editorPanel = new RuleEditorPanel();
    private final JTable table = new JTable(tableModel);

    private final RuleImportService ruleImportService;
    private final RuleEditorService ruleEditorService;
    private final RuleExportService ruleExportService;

    public RulePanel(
            RuleImportService ruleImportService,
            RuleEditorService ruleEditorService,
            RuleExportService ruleExportService
    ) {
        this.ruleImportService = ruleImportService;
        this.ruleEditorService = ruleEditorService;
        this.ruleExportService = ruleExportService;

        setLayout(new BorderLayout());

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        JButton importButton = new JButton("Import YAML");
        JButton saveButton = new JButton("Save rule");
        JButton deleteButton = new JButton("Delete");
        JButton exportButton = new JButton("Export YAML");
        JButton refreshButton = new JButton("Refresh");

        toolBar.add(importButton);
        toolBar.add(saveButton);
        toolBar.add(deleteButton);
        toolBar.add(exportButton);
        toolBar.add(refreshButton);

        add(toolBar, BorderLayout.NORTH);

        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                editorPanel.setRule(tableModel.getRuleAt(table.getSelectedRow()));
            }
        });

        importButton.addActionListener(event -> importYaml());
        saveButton.addActionListener(event -> saveCurrentRule());
        deleteButton.addActionListener(event -> deleteCurrentRule());
        exportButton.addActionListener(event -> exportYaml());
        refreshButton.addActionListener(event -> refresh());

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(table),
                editorPanel
        );
        splitPane.setResizeWeight(0.35);

        add(splitPane, BorderLayout.CENTER);
        refresh();
    }

    public void refresh() {
        tableModel.setRules(ruleEditorService.listRules());
    }

    private void importYaml() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try {
            int count = ruleImportService.importYaml(chooser.getSelectedFile().toPath());
            refresh();
            Dialogs.info(this, "Imported " + count + " rule(s).");
        } catch (Exception exception) {
            Dialogs.error(this, "Failed to import YAML", exception);
        }
    }

    private void saveCurrentRule() {
        try {
            ruleEditorService.save(editorPanel.toRule());
            refresh();
            Dialogs.info(this, "Rule saved.");
        } catch (Exception exception) {
            Dialogs.error(this, "Failed to save rule", exception);
        }
    }

    private void deleteCurrentRule() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }

        if (!Dialogs.confirm(this, "Delete selected rule?")) {
            return;
        }

        Long id = tableModel.getRuleAt(row).id();
        ruleEditorService.delete(id);
        refresh();
    }

    private void exportYaml() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("rules-export.yaml"));
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try {
            ruleExportService.exportYaml(chooser.getSelectedFile().toPath());
            Dialogs.info(this, "Rules exported.");
        } catch (Exception exception) {
            Dialogs.error(this, "Failed to export YAML", exception);
        }
    }
}

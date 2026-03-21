package org.titiplex.app.ui.rule;

import org.titiplex.app.service.RuleService;
import org.titiplex.app.ui.common.Dialogs;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public final class RulePanel extends JPanel {
    private final RuleTableModel tableModel = new RuleTableModel();
    private final RuleEditorPanel editorPanel = new RuleEditorPanel();
    private final JTable table = new JTable(tableModel);

    private final RuleService ruleService;

    public RulePanel(RuleService ruleService) {
        this.ruleService = ruleService;

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

        deleteButton.addActionListener(event -> deleteCurrentRule());
        saveButton.addActionListener(event -> saveCurrentRule());
        refreshButton.addActionListener(event -> refresh());
        importButton.addActionListener(event -> importYaml());
        exportButton.addActionListener(event -> exportYaml());

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
        tableModel.setRules(ruleService.getAll());
    }

    private void deleteCurrentRule() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }

        if (!Dialogs.confirm(this, "Delete selected rule?")) {
            return;
        }

        Long id = tableModel.getRuleAt(row).getId();
        ruleService.delete(id);
        refresh();
    }

    private void saveCurrentRule() {
        try {
            ruleService.save(editorPanel.toRule());
            refresh();
            Dialogs.info(this, "Rule saved.");
        } catch (Exception exception) {
            Dialogs.error(this, "Failed to save rule", exception);
        }
    }

    private void importYaml() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try {
            int count = ruleService.importYaml(chooser.getSelectedFile().toPath());
            refresh();
            Dialogs.info(this, "Imported " + count + " rule(s).");
        } catch (Exception exception) {
            Dialogs.error(this, "Failed to import YAML", exception);
        }
    }

    private void exportYaml() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("rules-export.yaml"));
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try {
            ruleService.exportYaml(chooser.getSelectedFile().toPath());
            Dialogs.info(this, "Rules exported.");
        } catch (Exception exception) {
            Dialogs.error(this, "Failed to export YAML", exception);
        }
    }
}

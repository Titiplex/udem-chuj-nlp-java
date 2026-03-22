package org.titiplex.app.ui.rule;

import org.titiplex.app.domain.validation.ValidationRun;
import org.titiplex.app.persistence.entity.Rule;
import org.titiplex.app.service.RuleService;
import org.titiplex.app.ui.common.Dialogs;
import org.titiplex.app.ui.common.ValidationDialogs;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.function.Consumer;

public final class RulePanel extends JPanel {
    private final RuleTableModel tableModel = new RuleTableModel();
    private final RuleEditorPanel editorPanel = new RuleEditorPanel();
    private final JTable table = new JTable(tableModel);
    private final RuleService ruleService;
    private final Consumer<String> statusConsumer;

    public RulePanel(RuleService ruleService, Consumer<String> statusConsumer) {
        this.ruleService = ruleService;
        this.statusConsumer = statusConsumer;

        setLayout(new BorderLayout(8, 8));

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        JButton newButton = new JButton("New");
        JButton importButton = new JButton("Import YAML");
        JButton validateButton = new JButton("Validate");
        JButton validateAllButton = new JButton("Validate all");
        JButton saveButton = new JButton("Save");
        JButton deleteButton = new JButton("Delete");
        JButton exportButton = new JButton("Export YAML");
        JButton refreshButton = new JButton("Refresh");

        toolBar.add(newButton);
        toolBar.add(importButton);
        toolBar.add(validateButton);
        toolBar.add(validateAllButton);
        toolBar.add(saveButton);
        toolBar.add(deleteButton);
        toolBar.add(exportButton);
        toolBar.add(refreshButton);

        add(toolBar, BorderLayout.NORTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
                editorPanel.setRule(tableModel.getRuleAt(modelRow));
            }
        });

        newButton.addActionListener(event -> createNewRule());
        importButton.addActionListener(event -> importYaml());
        validateButton.addActionListener(event -> validateCurrentRule());
        validateAllButton.addActionListener(event -> validateAllRules());
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

    public void createNewRule() {
        table.clearSelection();
        editorPanel.setRule(null);
    }

    public void refresh() {
        tableModel.setRules(ruleService.getAll());
        statusConsumer.accept(tableModel.getRowCount() + " rule(s) loaded.");
    }

    public void exportYaml() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("rules-export.yaml"));
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try {
            ruleService.exportYaml(chooser.getSelectedFile().toPath());
            statusConsumer.accept("Rules exported to " + chooser.getSelectedFile().getName());
            Dialogs.info(this, "Rules exported.");
        } catch (Exception exception) {
            Dialogs.error(this, "Failed to export YAML", exception);
        }
    }

    private void validateCurrentRule() {
        try {
            ValidationRun run = ruleService.validate(editorPanel.toRule());
            ValidationDialogs.showValidation(this, "Rule validation", run);
            statusConsumer.accept(run.summary());
        } catch (Exception exception) {
            Dialogs.error(this, "Failed to validate rule", exception);
        }
    }

    private void validateAllRules() {
        try {
            ValidationRun run = ruleService.validateAll();
            ValidationDialogs.showValidation(this, "All rules validation", run);
            statusConsumer.accept(run.summary());
        } catch (Exception exception) {
            Dialogs.error(this, "Failed to validate all rules", exception);
        }
    }

    private void deleteCurrentRule() {
        int row = table.getSelectedRow();
        if (row < 0) {
            Dialogs.info(this, "Select a rule first.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(row);
        Rule rule = tableModel.getRuleAt(modelRow);

        if (!Dialogs.confirm(this, "Delete selected rule: " + rule.getStableId() + " ?")) {
            return;
        }

        ruleService.delete(rule.getId());
        refresh();
        editorPanel.setRule(null);
        statusConsumer.accept("Rule deleted: " + rule.getStableId());
    }

    private void saveCurrentRule() {
        try {
            Rule saved = ruleService.save(editorPanel.toRule());
            refresh();
            editorPanel.setRule(saved);
            statusConsumer.accept("Rule saved: " + saved.getStableId());
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
            statusConsumer.accept("Imported " + count + " rule(s).");
            Dialogs.info(this, "Imported " + count + " rule(s).");
        } catch (Exception exception) {
            Dialogs.error(this, "Failed to import YAML", exception);
        }
    }
}
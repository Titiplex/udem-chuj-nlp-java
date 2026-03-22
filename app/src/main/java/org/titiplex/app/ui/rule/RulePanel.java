package org.titiplex.app.ui.rule;

import org.titiplex.app.domain.validation.ValidationRun;
import org.titiplex.app.persistence.entity.Rule;
import org.titiplex.app.service.RuleService;
import org.titiplex.app.ui.common.Dialogs;
import org.titiplex.app.ui.common.ValidationDialogs;
import org.titiplex.app.ui.rule.builder.RuleBuilderPanel;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.util.function.Consumer;

public final class RulePanel extends JPanel {
    private final RuleTableModel tableModel = new RuleTableModel();
    private final RuleEditorPanel editorPanel = new RuleEditorPanel();
    private final RuleBuilderPanel builderPanel = new RuleBuilderPanel();
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
        JButton validateButton = new JButton("Validate");
        JButton validateAllButton = new JButton("Validate all");
        JButton saveButton = new JButton("Save");
        JButton deleteButton = new JButton("Delete");
        JButton loadBuilderButton = new JButton("Load builder YAML");
        JButton refreshButton = new JButton("Refresh");

        toolBar.add(newButton);
        toolBar.add(validateButton);
        toolBar.add(validateAllButton);
        toolBar.add(saveButton);
        toolBar.add(deleteButton);
        toolBar.add(loadBuilderButton);
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
        validateButton.addActionListener(event -> validateCurrentRule());
        validateAllButton.addActionListener(event -> validateAllRules());
        saveButton.addActionListener(event -> saveCurrentRule());
        deleteButton.addActionListener(event -> deleteCurrentRule());
        loadBuilderButton.addActionListener(event -> loadBuilderYamlIntoEditor());
        refreshButton.addActionListener(event -> refresh());

        JTabbedPane rightTabs = new JTabbedPane();
        rightTabs.addTab("YAML editor", editorPanel);
        rightTabs.addTab("Guided builder", builderPanel);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(table),
                rightTabs
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

    public void importYaml(Path path) {
        try {
            int count = ruleService.importYaml(path);
            refresh();
            statusConsumer.accept("Imported " + count + " rule(s).");
            Dialogs.info(this, "Imported " + count + " rule(s).");
        } catch (Exception exception) {
            Dialogs.error(this, "Failed to import YAML", exception);
        }
    }

    public void exportYaml(Path path) {
        try {
            ruleService.exportYaml(path);
            statusConsumer.accept("Rules exported to " + path.getFileName());
            Dialogs.info(this, "Rules exported.");
        } catch (Exception exception) {
            Dialogs.error(this, "Failed to export YAML", exception);
        }
    }

    private void loadBuilderYamlIntoEditor() {
        try {
            editorPanel.setYamlBody(builderPanel.generateYaml());
            statusConsumer.accept("Builder YAML loaded into editor.");
        } catch (Exception exception) {
            Dialogs.error(this, "Failed to generate YAML from builder", exception);
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
}
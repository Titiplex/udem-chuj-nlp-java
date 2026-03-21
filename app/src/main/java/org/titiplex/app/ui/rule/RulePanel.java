package org.titiplex.app.ui.rule;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public final class RulePanel extends JPanel {
    private final RuleTableModel tableModel = new RuleTableModel();
    private final RuleEditorPanel editorPanel = new RuleEditorPanel();
    private final JTable table = new JTable(tableModel);

    public RulePanel() {
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

        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                editorPanel.setRule(tableModel.getRuleAt(table.getSelectedRow()));
            }
        });

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
        tableModel.setRules(new ArrayList<>());
    }
}

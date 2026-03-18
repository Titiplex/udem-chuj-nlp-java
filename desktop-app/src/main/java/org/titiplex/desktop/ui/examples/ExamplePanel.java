package org.titiplex.desktop.ui.examples;

import org.titiplex.desktop.service.example.ExampleQueryService;
import org.titiplex.desktop.ui.common.Dialogs;

import javax.swing.*;
import java.awt.*;

public final class ExamplePanel extends JPanel {
    private final ExampleTableModel tableModel = new ExampleTableModel();
    private final ExampleEditorPanel editorPanel = new ExampleEditorPanel();
    private final JTable table = new JTable(tableModel);

    private final ExampleQueryService exampleQueryService;

    public ExamplePanel(ExampleQueryService exampleQueryService) {
        this.exampleQueryService = exampleQueryService;

        setLayout(new BorderLayout());

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        JButton saveButton = new JButton("Save example");
        JButton refreshButton = new JButton("Refresh");

        toolBar.add(searchField);
        toolBar.add(searchButton);
        toolBar.add(saveButton);
        toolBar.add(refreshButton);

        add(toolBar, BorderLayout.NORTH);

        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                editorPanel.setExample(tableModel.getExampleAt(table.getSelectedRow()));
            }
        });

        searchButton.addActionListener(event -> tableModel.setExamples(exampleQueryService.search(searchField.getText())));
        saveButton.addActionListener(event -> saveCurrentExample());
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
        tableModel.setExamples(exampleQueryService.listAll());
    }

    private void saveCurrentExample() {
        try {
            exampleQueryService.save(editorPanel.toExample());
            refresh();
            Dialogs.info(this, "Example saved.");
        } catch (Exception exception) {
            Dialogs.error(this, "Failed to save example", exception);
        }
    }
}

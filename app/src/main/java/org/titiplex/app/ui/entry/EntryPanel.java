package org.titiplex.app.ui.entry;

import org.titiplex.app.service.CorrectedEntryService;
import org.titiplex.app.service.RawEntryService;

import javax.swing.*;
import java.awt.*;

public class EntryPanel extends JPanel {
    private final EntryTableModel tableModel = new EntryTableModel();
    private final EntryEditorPanel editorPanel;
    private final JTable table = new JTable(tableModel);
    private final CorrectedEntryService entryService;
    private final RawEntryService rawEntryService;

    public EntryPanel(
            CorrectedEntryService entryService,
            RawEntryService rawEntryService
    ) {
        this.entryService = entryService;
        this.rawEntryService = rawEntryService;
        this.editorPanel = new EntryEditorPanel(rawEntryService);

        setLayout(new BorderLayout());

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        JButton saveButton = new JButton("Save rule");
        JButton deleteButton = new JButton("Delete");
        JButton refreshButton = new JButton("Refresh");

        toolBar.add(saveButton);
        toolBar.add(deleteButton);
        toolBar.add(refreshButton);

        add(toolBar, BorderLayout.NORTH);

        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                editorPanel.setEntry(tableModel.getEntryAt(table.getSelectedRow()));
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
        tableModel.setEntries(entryService.getAll());
    }
}

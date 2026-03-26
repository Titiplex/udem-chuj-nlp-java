package org.titiplex.app.ui.entry;

import org.titiplex.app.persistence.entity.CorrectedEntry;
import org.titiplex.app.service.AppRefreshCoordinator;
import org.titiplex.app.service.AutoCorrectionService;
import org.titiplex.app.service.CorrectedEntryService;
import org.titiplex.app.service.RawEntryService;
import org.titiplex.app.ui.common.Dialogs;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class EntryPanel extends JPanel {
    private final EntryTableModel tableModel = new EntryTableModel();
    private final EntryEditorPanel editorPanel;
    private final JTable table = new JTable(tableModel);
    private final CorrectedEntryService entryService;
    private final AutoCorrectionService autoCorrectionService;
    private final AppRefreshCoordinator refreshCoordinator;
    private final Consumer<String> statusConsumer;

    public EntryPanel(
            CorrectedEntryService entryService,
            RawEntryService rawEntryService,
            AutoCorrectionService autoCorrectionService,
            AppRefreshCoordinator refreshCoordinator,
            Consumer<String> statusConsumer
    ) {
        this.entryService = entryService;
        this.autoCorrectionService = autoCorrectionService;
        this.refreshCoordinator = refreshCoordinator;
        this.statusConsumer = statusConsumer;
        this.editorPanel = new EntryEditorPanel(rawEntryService);

        setLayout(new BorderLayout(8, 8));

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        JButton newButton = new JButton("New");
        JButton saveButton = new JButton("Save");
        JButton recomputeButton = new JButton("Recompute draft");
        JButton deleteButton = new JButton("Delete");
        JButton refreshButton = new JButton("Refresh");

        toolBar.add(newButton);
        toolBar.add(saveButton);
        toolBar.add(recomputeButton);
        toolBar.add(deleteButton);
        toolBar.add(refreshButton);

        add(toolBar, BorderLayout.NORTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
                editorPanel.setEntry(tableModel.getEntryAt(modelRow));
            }
        });

        newButton.addActionListener(event -> createNewEntry());
        saveButton.addActionListener(event -> saveCurrentEntry());
        recomputeButton.addActionListener(event -> recomputeCurrentEntryAsDraft());
        deleteButton.addActionListener(event -> deleteCurrentEntry());
        refreshButton.addActionListener(event -> refresh());

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(table),
                editorPanel
        );
        splitPane.setResizeWeight(0.30);

        add(splitPane, BorderLayout.CENTER);

        refreshCoordinator.subscribe(AppRefreshCoordinator.Topic.CORRECTED_ENTRIES, this::refresh);
        refreshCoordinator.subscribe(AppRefreshCoordinator.Topic.RAW_ENTRIES, editorPanel::reloadRawEntryChoices);

        refresh();
    }

    public void createNewEntry() {
        table.clearSelection();
        editorPanel.setEntry(null);
    }

    public void refresh() {
        tableModel.setEntries(entryService.getAll());
        statusConsumer.accept(tableModel.getRowCount() + " entrie(s) loaded.");
    }

    private void saveCurrentEntry() {
        try {
            CorrectedEntry saved = entryService.save(editorPanel.toEntry());
            refreshCoordinator.publish(AppRefreshCoordinator.Topic.CORRECTED_ENTRIES);
            refresh();
            editorPanel.setEntry(saved);
            statusConsumer.accept("Entry saved: #" + saved.getId());
            Dialogs.info(this, "Entry saved.");
        } catch (Exception exception) {
            Dialogs.error(this, "Failed to save entry", exception);
        }
    }

    private void recomputeCurrentEntryAsDraft() {
        int row = table.getSelectedRow();
        if (row < 0) {
            Dialogs.info(this, "Select an entry first.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(row);
        CorrectedEntry entry = tableModel.getEntryAt(modelRow);

        try {
            CorrectedEntry recomputed = autoCorrectionService.regenerateDraftFromRaw(entry.getId());
            refreshCoordinator.publish(AppRefreshCoordinator.Topic.CORRECTED_ENTRIES);
            refresh();
            editorPanel.setEntry(recomputed);
            statusConsumer.accept("Draft recomputed from raw for corrected entry #" + recomputed.getId());
            Dialogs.info(this, "Draft recomputed from the linked raw entry using the current rules.");
        } catch (Exception exception) {
            Dialogs.error(this, "Failed to recompute draft", exception);
        }
    }

    private void deleteCurrentEntry() {
        int row = table.getSelectedRow();
        if (row < 0) {
            Dialogs.info(this, "Select an entry first.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(row);
        CorrectedEntry entry = tableModel.getEntryAt(modelRow);

        if (!Dialogs.confirm(this, "Delete corrected entry #" + entry.getId() + " ?")) {
            return;
        }

        entryService.delete(entry.getId());
        refreshCoordinator.publish(AppRefreshCoordinator.Topic.CORRECTED_ENTRIES);
        refresh();
        editorPanel.setEntry(null);
        statusConsumer.accept("Entry deleted: #" + entry.getId());
    }
}

package org.titiplex.app.ui.raw;

import org.titiplex.app.persistence.entity.RawEntry;
import org.titiplex.app.service.AutoCorrectionService;
import org.titiplex.app.service.CorpusImportService;
import org.titiplex.app.service.RawEntryService;
import org.titiplex.app.ui.common.Dialogs;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.util.function.Consumer;

public class RawEntryPanel extends JPanel {
    private final RawEntryTableModel tableModel = new RawEntryTableModel();
    private final RawEntryEditorPanel editorPanel = new RawEntryEditorPanel();
    private final JTable table = new JTable(tableModel);

    private final RawEntryService rawEntryService;
    private final CorpusImportService corpusImportService;
    private final AutoCorrectionService autoCorrectionService;
    private final Consumer<String> statusConsumer;

    public RawEntryPanel(
            RawEntryService rawEntryService,
            CorpusImportService corpusImportService,
            AutoCorrectionService autoCorrectionService,
            Consumer<String> statusConsumer
    ) {
        this.rawEntryService = rawEntryService;
        this.corpusImportService = corpusImportService;
        this.autoCorrectionService = autoCorrectionService;
        this.statusConsumer = statusConsumer;

        setLayout(new BorderLayout(8, 8));

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        JButton newButton = new JButton("New");
        JButton saveButton = new JButton("Save");
        JButton applyButton = new JButton("Apply rules");
        JButton applyAllButton = new JButton("Apply rules to all");
        JButton deleteButton = new JButton("Delete");
        JButton refreshButton = new JButton("Refresh");

        toolBar.add(newButton);
        toolBar.add(saveButton);
        toolBar.add(applyButton);
        toolBar.add(applyAllButton);
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
        applyButton.addActionListener(event -> applyRulesToCurrent());
        applyAllButton.addActionListener(event -> applyRulesToAll());
        deleteButton.addActionListener(event -> deleteCurrent());
        refreshButton.addActionListener(event -> refresh());

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(table),
                editorPanel
        );
        splitPane.setResizeWeight(0.28);

        add(splitPane, BorderLayout.CENTER);
        refresh();
    }

    public void createNewEntry() {
        table.clearSelection();
        editorPanel.setEntry(null);
        statusConsumer.accept("New raw entry editor opened.");
    }

    public void refresh() {
        tableModel.setEntries(rawEntryService.getAll());
        statusConsumer.accept(tableModel.getRowCount() + " raw entrie(s) loaded.");
    }

    public void importFile(Path path) {
        try {
            int count = corpusImportService.importFile(path);
            refresh();
            Dialogs.info(this, "Imported " + count + " raw entrie(s).");
            statusConsumer.accept("Imported " + count + " raw entrie(s).");
        } catch (Exception exception) {
            Dialogs.error(this, "Failed to import file", exception);
        }
    }

    private void saveCurrentEntry() {
        try {
            RawEntry saved = rawEntryService.save(editorPanel.toEntry());

            if (editorPanel.isAutoApplyRulesSelected()) {
                autoCorrectionService.applyToRawEntry(saved);
                statusConsumer.accept("Raw entry saved and rules applied: #" + saved.getId());
                Dialogs.info(this, "Raw entry saved and rules applied.");
            } else {
                statusConsumer.accept("Raw entry saved: #" + saved.getId());
                Dialogs.info(this, "Raw entry saved.");
            }

            refresh();
            editorPanel.setEntry(saved);
        } catch (Exception exception) {
            Dialogs.error(this, "Failed to save raw entry", exception);
        }
    }

    private void applyRulesToCurrent() {
        try {
            RawEntry entry = editorPanel.toEntry();
            if (entry.getId() == null) {
                entry = rawEntryService.save(entry);
            }
            boolean approvedBefore = entry.getId() != null
                    && rawEntryService.getById(entry.getId()) != null;
            var corrected = autoCorrectionService.applyToRawEntry(entry);

            if (Boolean.TRUE.equals(corrected.getIsCorrect())) {
                Dialogs.info(this, "Linked corrected entry is approved, so it was not overwritten.");
                statusConsumer.accept("Approved corrected entry preserved.");
            } else {
                Dialogs.info(this, "Rules applied to raw entry #" + entry.getId() + ".");
                statusConsumer.accept("Rules applied to raw entry #" + entry.getId());
            }
        } catch (Exception exception) {
            Dialogs.error(this, "Failed to apply rules", exception);
        }
    }

    private void applyRulesToAll() {
        try {
            int count = autoCorrectionService.applyToAll(rawEntryService.getAll());
            Dialogs.info(this, "Rules applied to " + count + " raw entrie(s). Approved entries were preserved.");
            statusConsumer.accept("Rules applied to " + count + " raw entrie(s).");
        } catch (Exception exception) {
            Dialogs.error(this, "Failed to apply rules to all raw entries", exception);
        }
    }

    private void deleteCurrent() {
        int row = table.getSelectedRow();
        if (row < 0) {
            Dialogs.info(this, "Select a raw entry first.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(row);
        RawEntry entry = tableModel.getEntryAt(modelRow);

        if (!Dialogs.confirm(this, "Delete raw entry #" + entry.getId() + " ?")) {
            return;
        }

        rawEntryService.delete(entry.getId());
        refresh();
        editorPanel.setEntry(null);
        statusConsumer.accept("Raw entry deleted: #" + entry.getId());
    }
}
package org.titiplex.app.ui.conllu;

import org.titiplex.app.persistence.entity.CorrectedEntry;
import org.titiplex.app.service.AnnotationConfigStateService;
import org.titiplex.app.service.ConlluPreviewService;
import org.titiplex.app.service.CorrectedEntryService;
import org.titiplex.app.ui.common.Dialogs;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.function.Consumer;

public class ConlluPanel extends JPanel {
    private final ConlluEntrySelectionTableModel tableModel = new ConlluEntrySelectionTableModel();
    private final JTable table = new JTable(tableModel);
    private final JTextArea previewArea = new JTextArea(30, 80);
    private final JLabel configStatusLabel = new JLabel("Annotation config: default empty config");

    private final CorrectedEntryService correctedEntryService;
    private final AnnotationConfigStateService annotationConfigStateService;
    private final ConlluPreviewService conlluPreviewService;
    private final Consumer<String> statusConsumer;

    public ConlluPanel(
            CorrectedEntryService correctedEntryService,
            AnnotationConfigStateService annotationConfigStateService,
            ConlluPreviewService conlluPreviewService,
            Consumer<String> statusConsumer
    ) {
        this.correctedEntryService = correctedEntryService;
        this.annotationConfigStateService = annotationConfigStateService;
        this.conlluPreviewService = conlluPreviewService;
        this.statusConsumer = statusConsumer;

        setLayout(new BorderLayout(8, 8));

        previewArea.setEditable(false);
        previewArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        JButton loadConfigButton = new JButton("Load annotation YAML");
        JButton resetConfigButton = new JButton("Reset config");
        JButton previewButton = new JButton("Preview");
        JButton exportButton = new JButton("Export selected .conllu");
        JButton exportAllButton = new JButton("Export all .conllu");
        JButton refreshButton = new JButton("Refresh");

        toolBar.add(loadConfigButton);
        toolBar.add(resetConfigButton);
        toolBar.add(previewButton);
        toolBar.add(exportButton);
        toolBar.add(exportAllButton);
        toolBar.add(refreshButton);

        add(toolBar, BorderLayout.NORTH);
        add(configStatusLabel, BorderLayout.SOUTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                previewSelectedEntry();
            }
        });

        loadConfigButton.addActionListener(event -> loadAnnotationConfig());
        resetConfigButton.addActionListener(event -> resetAnnotationConfig());
        previewButton.addActionListener(event -> previewSelectedEntry());
        exportButton.addActionListener(event -> exportSelectedEntry());
        exportAllButton.addActionListener(event -> exportAllEntries());
        refreshButton.addActionListener(event -> refresh());

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(table),
                new JScrollPane(previewArea)
        );
        splitPane.setResizeWeight(0.30);

        add(splitPane, BorderLayout.CENTER);

        refresh();
        updateConfigStatus();
    }

    public void refresh() {
        tableModel.setEntries(correctedEntryService.getAll());
        statusConsumer.accept(tableModel.getRowCount() + " corrected entrie(s) available for CoNLL-U.");
    }

    public void exportAllFromMenu() {
        exportAllEntries();
    }

    private void loadAnnotationConfig() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("annotation.yaml"));
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try {
            annotationConfigStateService.load(chooser.getSelectedFile().toPath());
            updateConfigStatus();
            statusConsumer.accept("Annotation config loaded: " + chooser.getSelectedFile().getName());
            previewSelectedEntry();
        } catch (Exception exception) {
            Dialogs.error(this, "Failed to load annotation YAML", exception);
        }
    }

    private void resetAnnotationConfig() {
        annotationConfigStateService.reset();
        updateConfigStatus();
        previewSelectedEntry();
        statusConsumer.accept("Annotation config reset.");
    }

    private void previewSelectedEntry() {
        int row = table.getSelectedRow();
        if (row < 0) {
            previewArea.setText("");
            return;
        }

        int modelRow = table.convertRowIndexToModel(row);
        CorrectedEntry entry = tableModel.getEntryAt(modelRow);

        try {
            String preview = conlluPreviewService.preview(entry, annotationConfigStateService.getCurrentConfig());
            previewArea.setText(preview);
            previewArea.setCaretPosition(0);
            statusConsumer.accept("CoNLL-U preview generated for entry #" + entry.getId());
        } catch (Exception exception) {
            Dialogs.error(this, "Failed to generate CoNLL-U preview", exception);
        }
    }

    private void exportSelectedEntry() {
        int row = table.getSelectedRow();
        if (row < 0) {
            Dialogs.info(this, "Select a corrected entry first.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(row);
        CorrectedEntry entry = tableModel.getEntryAt(modelRow);

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("entry-" + entry.getId() + ".conllu"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try {
            conlluPreviewService.export(
                    entry,
                    annotationConfigStateService.getCurrentConfig(),
                    chooser.getSelectedFile().toPath()
            );
            statusConsumer.accept("CoNLL-U exported for entry #" + entry.getId());
            Dialogs.info(this, "CoNLL-U exported.");
        } catch (Exception exception) {
            Dialogs.error(this, "Failed to export CoNLL-U", exception);
        }
    }

    private void exportAllEntries() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("corpus.conllu"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try {
            conlluPreviewService.exportAll(
                    tableModel.getEntries(),
                    annotationConfigStateService.getCurrentConfig(),
                    chooser.getSelectedFile().toPath()
            );
            statusConsumer.accept("Batch CoNLL-U exported.");
            Dialogs.info(this, "Batch CoNLL-U exported.");
        } catch (Exception exception) {
            Dialogs.error(this, "Failed to export all CoNLL-U entries", exception);
        }
    }

    private void updateConfigStatus() {
        if (annotationConfigStateService.getCurrentPath() == null) {
            configStatusLabel.setText("Annotation config: default empty config");
        } else {
            configStatusLabel.setText("Annotation config: " + annotationConfigStateService.getCurrentPath());
        }
    }
}
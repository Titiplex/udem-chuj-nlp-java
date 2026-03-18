package org.titiplex.desktop.ui.corrections;

import org.titiplex.desktop.domain.correction.Correction;
import org.titiplex.desktop.service.correction.CorrectionService;
import org.titiplex.desktop.ui.common.Dialogs;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class CorrectionPanel extends JPanel {
    private final CorrectionService correctionService;
    private final CorrectionTableModel tableModel = new CorrectionTableModel();
    private final JTable table = new JTable(tableModel);
    private final JTextArea detailArea = new JTextArea();

    public CorrectionPanel(CorrectionService correctionService) {
        this.correctionService = correctionService;

        setLayout(new BorderLayout());

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        JButton refreshButton = new JButton("Refresh");
        JButton decideButton = new JButton("Decide");
        toolBar.add(refreshButton);
        toolBar.add(decideButton);

        add(toolBar, BorderLayout.NORTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                showCorrection(tableModel.getCorrectionAt(table.getSelectedRow()));
            }
        });

        refreshButton.addActionListener(event -> refresh());
        decideButton.addActionListener(event -> decideSelected());

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(new JScrollPane(detailArea), BorderLayout.SOUTH);
        detailArea.setRows(10);

        refresh();
    }

    public void refresh() {
        tableModel.setCorrections(correctionService.listAll());
    }

    private void showCorrection(Correction correction) {
        detailArea.setText("""
                Before:
                %s
                
                After:
                %s
                
                Comment:
                %s
                """.formatted(
                correction.beforePayload(),
                correction.afterPayload(),
                correction.comment()
        ));
    }

    private void decideSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }

        Correction correction = tableModel.getCorrectionAt(row);
        CorrectionDecisionDialog.Result decision = CorrectionDecisionDialog.show(this);
        if (decision == null) {
            return;
        }

        try {
            correctionService.decide(correction, decision.decision(), decision.comment());
            refresh();
        } catch (Exception exception) {
            Dialogs.error(this, "Failed to update correction", exception);
        }
    }

    private static final class CorrectionTableModel extends AbstractTableModel {
        private final String[] columns = {"Id", "Example", "Rule", "Decision", "Origin"};
        private final List<Correction> corrections = new ArrayList<>();

        public void setCorrections(List<Correction> newCorrections) {
            corrections.clear();
            corrections.addAll(newCorrections);
            fireTableDataChanged();
        }

        public Correction getCorrectionAt(int row) {
            return corrections.get(row);
        }

        @Override
        public int getRowCount() {
            return corrections.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Correction correction = corrections.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> correction.id();
                case 1 -> correction.exampleId();
                case 2 -> correction.ruleId();
                case 3 -> correction.decision();
                case 4 -> correction.origin();
                default -> "";
            };
        }
    }
}

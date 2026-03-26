package org.titiplex.app.ui.entry;

import org.titiplex.app.persistence.entity.CorrectedEntry;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class EntryTableModel extends AbstractTableModel {

    private final String[] columns = {"Id", "Status", "Reason", "Updated"};
    private final List<CorrectedEntry> entries = new ArrayList<>();

    public void setEntries(List<CorrectedEntry> newEntries) {
        entries.clear();
        entries.addAll(newEntries);
        fireTableDataChanged();
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    public CorrectedEntry getEntryAt(int row) {
        return entries.get(row);
    }

    @Override
    public int getRowCount() {
        return entries.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        CorrectedEntry entry = getEntryAt(rowIndex);
        return switch (columnIndex) {
            case 0 -> entry.getId();
            case 1 -> entry.workflowStatusLabel();
            case 2 -> entry.stalenessReasonLabel();
            case 3 -> entry.getUpdatedAt() == null ? "" : entry.getUpdatedAt().toString();
            default -> null;
        };
    }
}
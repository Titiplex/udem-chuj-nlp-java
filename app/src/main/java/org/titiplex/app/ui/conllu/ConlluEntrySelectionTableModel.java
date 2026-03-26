package org.titiplex.app.ui.conllu;

import org.titiplex.app.persistence.entity.CorrectedEntry;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class ConlluEntrySelectionTableModel extends AbstractTableModel {
    private final String[] columns = {"Id", "Translation", "Status"};
    private final List<CorrectedEntry> entries = new ArrayList<>();

    public void setEntries(List<CorrectedEntry> newEntries) {
        entries.clear();
        entries.addAll(newEntries);
        fireTableDataChanged();
    }

    public CorrectedEntry getEntryAt(int row) {
        return entries.get(row);
    }

    public List<CorrectedEntry> getEntries() {
        return List.copyOf(entries);
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
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        CorrectedEntry entry = entries.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> entry.getId();
            case 1 -> shorten(entry.getTranslationText());
            case 2 -> entry.workflowStatusLabel();
            default -> "";
        };
    }

    private static String shorten(String value) {
        if (value == null) {
            return "";
        }
        return value.length() <= 60 ? value : value.substring(0, 57) + "...";
    }
}
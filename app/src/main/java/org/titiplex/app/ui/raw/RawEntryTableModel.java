package org.titiplex.app.ui.raw;

import org.titiplex.app.persistence.entity.RawEntry;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class RawEntryTableModel extends AbstractTableModel {
    private final String[] columns = {"Id", "Translation", "Updated"};
    private final List<RawEntry> entries = new ArrayList<>();

    public void setEntries(List<RawEntry> newEntries) {
        entries.clear();
        entries.addAll(newEntries);
        fireTableDataChanged();
    }

    public RawEntry getEntryAt(int row) {
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
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        RawEntry entry = entries.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> entry.getId();
            case 1 -> shorten(entry.getTranslationText());
            case 2 -> entry.getUpdatedAt() == null ? "" : entry.getUpdatedAt().toString();
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
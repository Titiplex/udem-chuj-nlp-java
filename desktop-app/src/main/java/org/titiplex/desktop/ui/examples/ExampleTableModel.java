package org.titiplex.desktop.ui.examples;

import org.titiplex.desktop.domain.example.Example;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public final class ExampleTableModel extends AbstractTableModel {
    private final String[] columns = {"Id", "External ID", "Surface", "Gloss", "Translation", "Status"};
    private final List<Example> examples = new ArrayList<>();

    public void setExamples(List<Example> newExamples) {
        examples.clear();
        examples.addAll(newExamples);
        fireTableDataChanged();
    }

    public Example getExampleAt(int row) {
        return examples.get(row);
    }

    @Override
    public int getRowCount() {
        return examples.size();
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
        Example example = examples.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> example.id();
            case 1 -> example.externalId();
            case 2 -> example.surfaceText();
            case 3 -> example.glossText();
            case 4 -> example.translationText();
            case 5 -> example.status();
            default -> "";
        };
    }
}

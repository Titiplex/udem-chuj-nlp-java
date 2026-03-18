package org.titiplex.desktop.ui;

import org.titiplex.desktop.model.RuleRecord;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public final class RuleTableModel extends AbstractTableModel {
    private final List<RuleRecord> rows = new ArrayList<>();

    public void setRows(List<RuleRecord> rules) {
        rows.clear();
        rows.addAll(rules);
        fireTableDataChanged();
    }

    public RuleRecord at(int index) {
        return rows.get(index);
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public String getColumnName(int column) {
        return switch (column) {
            case 0 -> "Rule ID";
            case 1 -> "Name";
            case 2 -> "Enabled";
            case 3 -> "Updated";
            default -> "";
        };
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        RuleRecord rule = rows.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> rule.ruleId();
            case 1 -> rule.name();
            case 2 -> rule.enabled();
            case 3 -> rule.updatedAt();
            default -> "";
        };
    }
}
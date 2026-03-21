package org.titiplex.app.ui.rule;

import org.titiplex.app.persistence.entity.Rule;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public final class RuleTableModel extends AbstractTableModel {
    private final String[] columns = {"Id", "Name", "Enabled"};
    private final List<Rule> rules = new ArrayList<>();

    public void setRules(List<Rule> newRules) {
        rules.clear();
        rules.addAll(newRules);
        fireTableDataChanged();
    }

    public Rule getRuleAt(int row) {
        return rules.get(row);
    }

    @Override
    public int getRowCount() {
        return rules.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Rule rule = rules.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> rule.getStableId();
            case 1 -> rule.getName();
            case 2 -> rule.isEnabled();
            default -> null;
        };
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }
}

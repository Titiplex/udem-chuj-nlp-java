package org.titiplex.desktop.ui.rules;

import org.titiplex.desktop.domain.rule.Rule;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public final class RuleTableModel extends AbstractTableModel {
    private final String[] columns = {"Id", "Rule ID", "Name", "Enabled", "Version"};
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
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Rule rule = rules.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> rule.id();
            case 1 -> rule.ruleId().value();
            case 2 -> rule.name();
            case 3 -> rule.enabled();
            case 4 -> rule.version().value();
            default -> "";
        };
    }
}

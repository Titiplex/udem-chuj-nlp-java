package org.titiplex.app.ui.rule.builder.support;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class StringListTablePanel extends JPanel {
    private final DefaultTableModel model;
    private final JTable table;

    public StringListTablePanel(String title, String columnName) {
        setLayout(new BorderLayout(6, 6));
        setBorder(BorderFactory.createTitledBorder(title));

        model = new DefaultTableModel(new Object[]{columnName}, 0);
        table = new JTable(model);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JButton addButton = new JButton("+");
        JButton removeButton = new JButton("-");
        addButton.addActionListener(e -> model.addRow(new Object[]{""}));
        removeButton.addActionListener(e -> removeSelectedRow());
        buttons.add(addButton);
        buttons.add(Box.createHorizontalStrut(6));
        buttons.add(removeButton);

        add(buttons, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public List<String> getValues() {
        List<String> out = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            Object value = model.getValueAt(i, 0);
            if (value != null) {
                String s = value.toString().trim();
                if (!s.isBlank()) {
                    out.add(s);
                }
            }
        }
        return out;
    }

    public void setValues(List<String> values) {
        model.setRowCount(0);
        if (values == null) return;
        for (String value : values) {
            model.addRow(new Object[]{value});
        }
    }

    private void removeSelectedRow() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            model.removeRow(row);
        }
    }
}
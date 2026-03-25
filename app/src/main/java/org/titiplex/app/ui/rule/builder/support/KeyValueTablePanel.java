package org.titiplex.app.ui.rule.builder.support;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public final class KeyValueTablePanel extends JPanel {
    private final DefaultTableModel model;
    private final JTable table;

    public KeyValueTablePanel(String title, String keyColumn, String valueColumn) {
        setLayout(new BorderLayout(6, 6));
        setBorder(BorderFactory.createTitledBorder(title));

        model = new DefaultTableModel(new Object[]{keyColumn, valueColumn}, 0);
        table = new JTable(model);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JButton addButton = new JButton("+");
        JButton removeButton = new JButton("-");
        addButton.addActionListener(e -> model.addRow(new Object[]{"", ""}));
        removeButton.addActionListener(e -> removeSelectedRow());
        buttons.add(addButton);
        buttons.add(Box.createHorizontalStrut(6));
        buttons.add(removeButton);

        add(buttons, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public Map<String, String> getMap() {
        Map<String, String> out = new LinkedHashMap<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            Object k = model.getValueAt(i, 0);
            Object v = model.getValueAt(i, 1);
            String key = k == null ? "" : k.toString().trim();
            String value = v == null ? "" : v.toString().trim();
            if (!key.isBlank() && !value.isBlank()) {
                out.put(key, value);
            }
        }
        return out;
    }

    public void setMap(Map<String, String> values) {
        model.setRowCount(0);
        if (values == null) return;
        for (var e : values.entrySet()) {
            model.addRow(new Object[]{e.getKey(), e.getValue()});
        }
    }

    private void removeSelectedRow() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            model.removeRow(row);
        }
    }
}
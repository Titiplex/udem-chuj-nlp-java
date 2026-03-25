package org.titiplex.app.ui.rule.builder.support;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ExtractSpecTablePanel extends JPanel {
    private final DefaultTableModel model;
    private final JTable table;

    public ExtractSpecTablePanel(String title) {
        setLayout(new BorderLayout(6, 6));
        setBorder(BorderFactory.createTitledBorder(title));

        model = new DefaultTableModel(new Object[]{"type", "extractor", "into", "key", "value"}, 0);
        table = new JTable(model);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JButton addButton = new JButton("+");
        JButton removeButton = new JButton("-");
        addButton.addActionListener(e -> model.addRow(new Object[]{"", "", "", "", ""}));
        removeButton.addActionListener(e -> removeSelectedRow());
        buttons.add(addButton);
        buttons.add(Box.createHorizontalStrut(6));
        buttons.add(removeButton);

        add(buttons, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public List<Map<String, String>> getSpecs() {
        List<Map<String, String>> out = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            Map<String, String> row = new LinkedHashMap<>();
            putIfPresent(row, "type", model.getValueAt(i, 0));
            putIfPresent(row, "extractor", model.getValueAt(i, 1));
            putIfPresent(row, "into", model.getValueAt(i, 2));
            putIfPresent(row, "key", model.getValueAt(i, 3));
            putIfPresent(row, "value", model.getValueAt(i, 4));
            if (!row.isEmpty()) {
                out.add(row);
            }
        }
        return out;
    }

    private void putIfPresent(Map<String, String> row, String key, Object value) {
        if (value == null) return;
        String s = value.toString().trim();
        if (!s.isBlank()) {
            row.put(key, s);
        }
    }

    private void removeSelectedRow() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            model.removeRow(row);
        }
    }
}
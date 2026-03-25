package org.titiplex.app.ui.rule.builder.support;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class GlossFeatMapTablePanel extends JPanel {
    private final DefaultTableModel model;
    private final JTable table;

    public GlossFeatMapTablePanel(String title) {
        setLayout(new BorderLayout(6, 6));
        setBorder(BorderFactory.createTitledBorder(title));

        model = new DefaultTableModel(new Object[]{"Gloss", "Feat", "Value"}, 0);
        table = new JTable(model);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JButton addButton = new JButton("+");
        JButton removeButton = new JButton("-");
        addButton.addActionListener(e -> model.addRow(new Object[]{"", "", ""}));
        removeButton.addActionListener(e -> removeSelectedRow());
        buttons.add(addButton);
        buttons.add(Box.createHorizontalStrut(6));
        buttons.add(removeButton);

        add(buttons, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public List<Map<String, Object>> getEntries() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            String gloss = str(model.getValueAt(i, 0));
            String feat = str(model.getValueAt(i, 1));
            String value = str(model.getValueAt(i, 2));
            if (!gloss.isBlank() && !feat.isBlank() && !value.isBlank()) {
                out.add(Map.of(gloss, List.of(feat, value)));
            }
        }
        return out;
    }

    private String str(Object value) {
        return value == null ? "" : value.toString().trim();
    }

    private void removeSelectedRow() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            model.removeRow(row);
        }
    }
}
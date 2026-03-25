package org.titiplex.app.ui.rule.builder.support;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class TokenSequenceTablePanel extends JPanel {
    private final DefaultTableModel model;
    private final JTable table;

    public TokenSequenceTablePanel(String title) {
        setLayout(new BorderLayout(6, 6));
        setBorder(BorderFactory.createTitledBorder(title));

        model = new DefaultTableModel(new Object[]{"Sequence (comma-separated)"}, 0);
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

    public List<List<String>> getSequences() {
        List<List<String>> out = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            Object value = model.getValueAt(i, 0);
            if (value == null) continue;
            String line = value.toString().trim();
            if (line.isBlank()) continue;

            List<String> seq = Arrays.stream(line.split("\\s*,\\s*"))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .toList();
            if (!seq.isEmpty()) {
                out.add(new ArrayList<>(seq));
            }
        }
        return out;
    }

    private void removeSelectedRow() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            model.removeRow(row);
        }
    }
}
package org.titiplex.app.ui.common;

import javax.swing.*;
import java.awt.*;

public final class FormRow {
    private FormRow() {
    }

    public static JPanel build(String label, JComponent field, String helpTitle, String helpText) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        left.add(new JLabel(label));
        if (helpText != null && !helpText.isBlank()) {
            left.add(new HelpIconButton(helpTitle, helpText));
        }
        row.add(left, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        return row;
    }
}
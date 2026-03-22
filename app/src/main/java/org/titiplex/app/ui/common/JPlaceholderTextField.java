package org.titiplex.app.ui.common;

import javax.swing.*;
import java.awt.*;

public final class JPlaceholderTextField extends JTextField {
    private String placeholder;

    public JPlaceholderTextField() {
        super();
    }

    public JPlaceholderTextField(String placeholder) {
        super();
        this.placeholder = placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        repaint();
    }

    public String getPlaceholder() {
        return placeholder;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (placeholder == null || placeholder.isBlank() || !getText().isEmpty() || isFocusOwner()) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setColor(UIManager.getColor("TextField.placeholderForeground") != null
                    ? UIManager.getColor("TextField.placeholderForeground")
                    : Color.GRAY);
            Insets insets = getInsets();
            FontMetrics fm = g2.getFontMetrics();
            int y = insets.top + fm.getAscent() + 1;
            g2.drawString(placeholder, insets.left + 2, y);
        } finally {
            g2.dispose();
        }
    }
}
package org.titiplex.app.ui.common;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public final class JPlaceholderTextField extends JTextField {
    public JPlaceholderTextField() {
        super();
        this.setForeground(Color.GRAY);
    }

    public JPlaceholderTextField(String placeholder) {
        super();
        this.setForeground(Color.GRAY);
        this.setPlaceholder(placeholder);
    }

    public void setPlaceholder(String placeholder) {
        this.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (getText().equals(placeholder)) {
                    setText("");
                    setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (getText().isEmpty()) {
                    setForeground(Color.GRAY);
                    setText(placeholder);
                }
            }
        });
    }
}

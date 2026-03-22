package org.titiplex.app.ui.common;

import javax.swing.*;
import java.awt.*;

public final class HelpIconButton extends JButton {
    public HelpIconButton(String title, String helpText) {
        super("?");
        setMargin(new Insets(2, 8, 2, 8));
        setFocusable(false);
        addActionListener(e ->
                JOptionPane.showMessageDialog(
                        SwingUtilities.getWindowAncestor(this),
                        helpText,
                        title,
                        JOptionPane.INFORMATION_MESSAGE
                )
        );
    }
}
package org.titiplex.app.ui.common;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;

public final class Dialogs {
    private Dialogs() {
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void error(Component parent, String message, Throwable throwable) {
        if (throwable == null) {
            JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String shortMessage = throwable.getMessage();
        if (shortMessage == null || shortMessage.isBlank()) {
            shortMessage = throwable.getClass().getSimpleName();
        }

        JTextArea area = new JTextArea(18, 80);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));

        area.setText(message + "\n\n" + shortMessage + "\n\n" + sw);
        area.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(area);
        JOptionPane.showMessageDialog(parent, scrollPane, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Confirm", JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION;
    }
}
package org.titiplex.desktop.ui.common;

import javax.swing.*;
import java.awt.*;

public final class Dialogs {
    private Dialogs() {
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void error(Component parent, String message, Throwable throwable) {
        String fullMessage = throwable == null ? message : message + "\n\n" + throwable.getMessage();
        JOptionPane.showMessageDialog(parent, fullMessage, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Confirm", JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION;
    }
}

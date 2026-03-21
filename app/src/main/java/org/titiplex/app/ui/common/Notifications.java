package org.titiplex.app.ui.common;

import javax.swing.*;

public final class Notifications {
    private Notifications() {
    }

    public static void setStatus(JLabel label, String message) {
        if (label != null) {
            label.setText(message);
        }
    }
}
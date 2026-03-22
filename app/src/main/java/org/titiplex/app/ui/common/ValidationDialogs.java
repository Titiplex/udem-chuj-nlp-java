package org.titiplex.app.ui.common;

import org.titiplex.app.domain.validation.ValidationMessage;
import org.titiplex.app.domain.validation.ValidationRun;

import javax.swing.*;
import java.awt.*;

public final class ValidationDialogs {
    private ValidationDialogs() {
    }

    public static void showValidation(Component parent, String title, ValidationRun run) {
        JTextArea area = new JTextArea(buildText(run), 18, 80);
        area.setEditable(false);
        area.setCaretPosition(0);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(area);
        JOptionPane.showMessageDialog(
                parent,
                scrollPane,
                title,
                run.ok() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE
        );
    }

    private static String buildText(ValidationRun run) {
        StringBuilder sb = new StringBuilder();
        sb.append("Summary: ").append(run.summary()).append("\n");
        sb.append("Status: ").append(run.ok() ? "OK" : "FAILED").append("\n\n");

        if (run.messages() == null || run.messages().isEmpty()) {
            sb.append("No validation messages.");
            return sb.toString();
        }

        for (ValidationMessage message : run.messages()) {
            sb.append("[")
                    .append(message.severity())
                    .append("] ")
                    .append(message.message());

            if (message.ruleId() != null) {
                sb.append(" (ruleId=").append(message.ruleId()).append(")");
            }
            if (message.exampleId() != null) {
                sb.append(" (exampleId=").append(message.exampleId()).append(")");
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}
package org.titiplex.desktop.ui.corrections;

import org.titiplex.desktop.domain.correction.CorrectionDecision;

import javax.swing.*;
import java.awt.*;

public final class CorrectionDecisionDialog {
    private CorrectionDecisionDialog() {
    }

    public static Result show(Component parent) {
        JComboBox<CorrectionDecision> decisionBox = new JComboBox<>(CorrectionDecision.values());
        JTextArea commentArea = new JTextArea(5, 40);

        Object[] message = {
                "Decision:", decisionBox,
                "Comment:", new JScrollPane(commentArea)
        };

        int result = JOptionPane.showConfirmDialog(parent, message, "Correction decision", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        return new Result((CorrectionDecision) decisionBox.getSelectedItem(), commentArea.getText());
    }

    public record Result(CorrectionDecision decision, String comment) {
    }
}

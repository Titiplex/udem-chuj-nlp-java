package org.titiplex.app.ui.raw;

import org.titiplex.app.persistence.entity.RawEntry;
import org.titiplex.app.ui.common.FormRow;
import org.titiplex.app.ui.common.HelpIconButton;

import javax.swing.*;
import java.awt.*;

public class RawEntryEditorPanel extends JPanel {
    private final JTextField idField = new JTextField();
    private final JTextField createdField = new JTextField();
    private final JTextField updatedField = new JTextField();
    private final JTextField translationField = new JTextField();

    private final JTextArea rawTextArea = new JTextArea(8, 50);
    private final JTextArea rawGlossArea = new JTextArea(8, 50);
    private final JTextArea descriptionArea = new JTextArea(5, 50);

    private final JCheckBox autoApplyRulesBox = new JCheckBox("Apply rules automatically after save", true);

    private RawEntry currentEntry;

    public RawEntryEditorPanel() {
        setLayout(new BorderLayout(8, 8));

        idField.setEditable(false);
        createdField.setEditable(false);
        updatedField.setEditable(false);

        rawTextArea.setLineWrap(true);
        rawTextArea.setWrapStyleWord(true);
        rawGlossArea.setLineWrap(true);
        rawGlossArea.setWrapStyleWord(true);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        add(buildMetaPanel(), BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildBottomPanel(), BorderLayout.SOUTH);
    }

    private JComponent buildMetaPanel() {
        JPanel panel = new JPanel(new GridLayout(5, 1, 8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Raw entry metadata"));

        panel.add(FormRow.build(
                "ID",
                idField,
                "Raw entry ID",
                "Identifiant technique de l'entrée en base."
        ));
        panel.add(FormRow.build(
                "Created",
                createdField,
                "Created",
                "Date de création de l'entrée."
        ));
        panel.add(FormRow.build(
                "Updated",
                updatedField,
                "Updated",
                "Date de dernière modification."
        ));
        panel.add(FormRow.build(
                "Translation",
                translationField,
                "Translation",
                "Traduction libre de l'énoncé."
        ));

        JPanel autoApplyRow = new JPanel(new BorderLayout(8, 0));
        autoApplyRow.add(autoApplyRulesBox, BorderLayout.WEST);
        autoApplyRow.add(new HelpIconButton(
                "Automatic rule application",
                "Si coché, la sauvegarde de cette raw entry relance immédiatement la correction."
        ), BorderLayout.EAST);
        panel.add(autoApplyRow);

        return panel;
    }

    private JComponent buildCenterPanel() {
        JSplitPane splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                wrap("Raw text", rawTextArea, "Texte brut", "Texte chuj brut tel qu'importé ou saisi."),
                wrap("Raw gloss", rawGlossArea, "Gloss brute", "Gloss interlinéaire brute alignée au texte.")
        );
        splitPane.setResizeWeight(0.5);
        return splitPane;
    }

    private JComponent buildBottomPanel() {
        return wrap("Description", descriptionArea, "Description", "Notes libres sur cette entrée.");
    }

    private JComponent wrap(String title, JTextArea area, String helpTitle, String helpText) {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        JPanel header = new JPanel(new BorderLayout());
        header.add(new JLabel(title), BorderLayout.WEST);
        header.add(new HelpIconButton(helpTitle, helpText), BorderLayout.EAST);

        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(header, BorderLayout.NORTH);
        panel.add(new JScrollPane(area), BorderLayout.CENTER);
        return panel;
    }

    public void setEntry(RawEntry entry) {
        this.currentEntry = entry;

        if (entry == null) {
            idField.setText("");
            createdField.setText("");
            updatedField.setText("");
            translationField.setText("");
            rawTextArea.setText("");
            rawGlossArea.setText("");
            descriptionArea.setText("");
            autoApplyRulesBox.setSelected(true);
            return;
        }

        idField.setText(entry.getId() == null ? "" : String.valueOf(entry.getId()));
        createdField.setText(entry.getCreatedAt() == null ? "" : entry.getCreatedAt().toString());
        updatedField.setText(entry.getUpdatedAt() == null ? "" : entry.getUpdatedAt().toString());
        translationField.setText(nullToEmpty(entry.getTranslationText()));
        rawTextArea.setText(nullToEmpty(entry.getRawText()));
        rawGlossArea.setText(nullToEmpty(entry.getGlossText()));
        descriptionArea.setText(nullToEmpty(entry.getDescription()));
    }

    public RawEntry toEntry() {
        RawEntry out = currentEntry == null ? new RawEntry() : currentEntry;
        out.setTranslationText(translationField.getText().trim());
        out.setRawText(rawTextArea.getText());
        out.setGlossText(rawGlossArea.getText());
        out.setDescription(descriptionArea.getText().trim());
        return out;
    }

    public boolean isAutoApplyRulesSelected() {
        return autoApplyRulesBox.isSelected();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
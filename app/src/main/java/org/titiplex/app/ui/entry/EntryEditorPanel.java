package org.titiplex.app.ui.entry;

import org.titiplex.app.persistence.entity.CorrectedEntry;
import org.titiplex.app.persistence.entity.RawEntry;
import org.titiplex.app.service.RawEntryService;

import javax.swing.*;
import java.awt.*;

public class EntryEditorPanel extends JPanel {
    private final JTextField idField = new JTextField();
    private final JTextField createdField = new JTextField();
    private final JTextField updatedField = new JTextField();
    private final JTextField translationField = new JTextField();

    private final JTextArea correctedTextArea = new JTextArea(4, 40);
    private final JTextArea correctedGlossArea = new JTextArea(4, 40);

    private final JTextArea rawTextArea = new JTextArea(4, 40);
    private final JTextArea rawGlossArea = new JTextArea(4, 40);

    private final JTextArea descriptionArea = new JTextArea(5, 80);

    private final JCheckBox approvedBox = new JCheckBox("Approved", false);

    private CorrectedEntry entry;
    private final RawEntryService rawEntryService;

    public EntryEditorPanel(RawEntryService rawEntryService) {
        this.rawEntryService = rawEntryService;

        setLayout(new BorderLayout(8, 8));

        idField.setEditable(false);
        createdField.setEditable(false);
        updatedField.setEditable(false);

        correctedTextArea.setLineWrap(true);
        correctedTextArea.setWrapStyleWord(true);

        correctedGlossArea.setLineWrap(true);
        correctedGlossArea.setWrapStyleWord(true);

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
        JPanel meta = new JPanel(new GridLayout(5, 2, 8, 8));
        meta.setBorder(BorderFactory.createTitledBorder("Metadata"));

        meta.add(new JLabel("ID"));
        meta.add(idField);
        meta.add(new JLabel("Created"));
        meta.add(createdField);

        meta.add(new JLabel("Updated"));
        meta.add(updatedField);
        meta.add(new JLabel("Translation"));
        meta.add(translationField);

        meta.add(new JLabel());
        meta.add(approvedBox);

        return meta;
    }

    private JComponent buildCenterPanel() {
        JPanel correctedPanel = new JPanel(new BorderLayout(8, 8));
        correctedPanel.setBorder(BorderFactory.createTitledBorder("Corrected"));

        JPanel correctedInner = new JPanel(new GridLayout(2, 1, 8, 8));
        correctedInner.add(wrap("Text", correctedTextArea));
        correctedInner.add(wrap("Gloss", correctedGlossArea));
        correctedPanel.add(correctedInner, BorderLayout.CENTER);

        JPanel rawPanel = new JPanel(new BorderLayout(8, 8));
        rawPanel.setBorder(BorderFactory.createTitledBorder("Raw"));

        JPanel rawInner = new JPanel(new GridLayout(2, 1, 8, 8));
        rawInner.add(wrap("Text", rawTextArea));
        rawInner.add(wrap("Gloss", rawGlossArea));
        rawPanel.add(rawInner, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                correctedPanel,
                rawPanel
        );
        splitPane.setResizeWeight(0.5);

        return splitPane;
    }

    private JComponent buildBottomPanel() {
        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.setBorder(BorderFactory.createTitledBorder("Description"));
        bottom.add(new JScrollPane(descriptionArea), BorderLayout.CENTER);
        return bottom;
    }

    private JComponent wrap(String title, JTextArea area) {
        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setBorder(BorderFactory.createTitledBorder(title));
        return scrollPane;
    }

    public void setEntry(CorrectedEntry entry) {
        this.entry = entry;

        if (entry == null) {
            idField.setText("");
            createdField.setText("");
            updatedField.setText("");
            translationField.setText("");
            correctedTextArea.setText("");
            correctedGlossArea.setText("");
            rawTextArea.setText("");
            rawGlossArea.setText("");
            descriptionArea.setText("");
            approvedBox.setSelected(false);
            return;
        }

        idField.setText(String.valueOf(entry.getId()));
        createdField.setText(String.valueOf(entry.getCreatedAt()));
        updatedField.setText(String.valueOf(entry.getUpdatedAt()));
        translationField.setText(entry.getTranslationText());

        correctedTextArea.setText(entry.getRawText());
        correctedGlossArea.setText(entry.getGlossText());

        approvedBox.setSelected(entry.getIsCorrect());

        RawEntry rawEntry = rawEntryService.getById(entry.getRawEntryId());
        if (rawEntry != null) {
            rawTextArea.setText(rawEntry.getRawText());
            rawGlossArea.setText(rawEntry.getGlossText());
        } else {
            rawTextArea.setText("");
            rawGlossArea.setText("");
        }

        descriptionArea.setText(entry.getDescription());
    }
}
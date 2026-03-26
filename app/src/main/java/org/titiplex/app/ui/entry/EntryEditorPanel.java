package org.titiplex.app.ui.entry;

import org.titiplex.app.persistence.entity.CorrectedEntry;
import org.titiplex.app.persistence.entity.RawEntry;
import org.titiplex.app.service.RawEntryService;

import javax.swing.*;
import java.awt.*;
import java.time.Instant;
import java.util.List;

public class EntryEditorPanel extends JPanel {
    private final JTextField idField = new JTextField();
    private final JComboBox<RawEntryRef> rawEntryComboBox = new JComboBox<>();
    private final JTextField statusField = new JTextField();
    private final JTextField approvedRawUpdatedAtField = new JTextField();
    private final JTextField createdField = new JTextField();
    private final JTextField updatedField = new JTextField();
    private final JTextField translationField = new JTextField();

    private final JTextArea correctedTextArea = new JTextArea(4, 40);
    private final JTextArea correctedGlossArea = new JTextArea(4, 40);

    private final JTextArea rawTextArea = new JTextArea(4, 40);
    private final JTextArea rawGlossArea = new JTextArea(4, 40);

    private final JTextArea descriptionArea = new JTextArea(5, 80);
    private final JCheckBox approvedBox = new JCheckBox("Approved", false);
    private final JLabel staleWarningLabel = new JLabel(" ");

    private CorrectedEntry entry;
    private final RawEntryService rawEntryService;

    public EntryEditorPanel(RawEntryService rawEntryService) {
        this.rawEntryService = rawEntryService;

        setLayout(new BorderLayout(8, 8));

        idField.setEditable(false);
        statusField.setEditable(false);
        approvedRawUpdatedAtField.setEditable(false);
        createdField.setEditable(false);
        updatedField.setEditable(false);

        rawTextArea.setEditable(false);
        rawGlossArea.setEditable(false);

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

        staleWarningLabel.setForeground(new Color(180, 80, 0));

        rawEntryComboBox.addActionListener(event -> refreshLinkedRawPreview());

        add(buildMetaPanel(), BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildBottomPanel(), BorderLayout.SOUTH);

        reloadRawEntryChoices();
    }

    private JComponent buildMetaPanel() {
        JPanel meta = new JPanel(new GridLayout(8, 2, 8, 8));
        meta.setBorder(BorderFactory.createTitledBorder("Metadata"));

        meta.add(new JLabel("ID"));
        meta.add(idField);

        meta.add(new JLabel("Linked raw entry"));
        meta.add(rawEntryComboBox);

        meta.add(new JLabel("Status"));
        meta.add(statusField);

        meta.add(new JLabel("Approved against raw updated at"));
        meta.add(approvedRawUpdatedAtField);

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
        rawPanel.setBorder(BorderFactory.createTitledBorder("Linked raw entry"));

        JPanel rawInner = new JPanel(new GridLayout(2, 1, 8, 8));
        rawInner.add(wrap("Raw text", rawTextArea));
        rawInner.add(wrap("Raw gloss", rawGlossArea));
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
        bottom.add(staleWarningLabel, BorderLayout.NORTH);
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
        reloadRawEntryChoices();

        if (entry == null) {
            idField.setText("");
            rawEntryComboBox.setSelectedIndex(-1);
            statusField.setText("");
            approvedRawUpdatedAtField.setText("");
            createdField.setText("");
            updatedField.setText("");
            translationField.setText("");
            correctedTextArea.setText("");
            correctedGlossArea.setText("");
            rawTextArea.setText("");
            rawGlossArea.setText("");
            descriptionArea.setText("");
            approvedBox.setSelected(false);
            staleWarningLabel.setText(" ");
            return;
        }

        idField.setText(entry.getId() == null ? "" : String.valueOf(entry.getId()));
        statusField.setText(entry.workflowStatusLabel());
        approvedRawUpdatedAtField.setText(formatInstant(entry.getApprovedRawUpdatedAt()));
        createdField.setText(formatInstant(entry.getCreatedAt()));
        updatedField.setText(formatInstant(entry.getUpdatedAt()));
        translationField.setText(nullSafe(entry.getTranslationText()));
        correctedTextArea.setText(nullSafe(entry.getRawText()));
        correctedGlossArea.setText(nullSafe(entry.getGlossText()));
        approvedBox.setSelected(Boolean.TRUE.equals(entry.getIsCorrect()));
        descriptionArea.setText(nullSafe(entry.getDescription()));
        staleWarningLabel.setText(entry.isStale()
                ? "Warning: this approved correction is outdated because the linked raw entry changed after approval."
                : " ");

        Long linkedRawId = entry.getRawEntry() == null ? null : entry.getRawEntry().getId();
        selectRawEntry(linkedRawId);
        refreshLinkedRawPreview();
    }

    public CorrectedEntry toEntry() {
        CorrectedEntry out = entry == null ? new CorrectedEntry() : entry;

        out.setTranslationText(translationField.getText().trim());
        out.setRawText(correctedTextArea.getText());
        out.setGlossText(correctedGlossArea.getText());
        out.setDescription(descriptionArea.getText().trim());
        out.setIsCorrect(approvedBox.isSelected());

        RawEntryRef selected = (RawEntryRef) rawEntryComboBox.getSelectedItem();
        if (selected == null || selected.id() == null) {
            out.setRawEntry(null);
        } else {
            RawEntry linked = rawEntryService.getById(selected.id());
            if (linked == null) {
                throw new IllegalArgumentException("Raw entry #" + selected.id() + " does not exist");
            }
            out.setRawEntry(linked);
        }

        return out;
    }

    public void reloadRawEntryChoices() {
        List<RawEntry> rawEntries = rawEntryService.getAll();
        DefaultComboBoxModel<RawEntryRef> model = new DefaultComboBoxModel<>();

        model.addElement(new RawEntryRef(null, "(none)"));
        for (RawEntry raw : rawEntries) {
            model.addElement(new RawEntryRef(
                    raw.getId(),
                    buildLabel(raw)
            ));
        }

        rawEntryComboBox.setModel(model);
    }

    private void selectRawEntry(Long rawEntryId) {
        ComboBoxModel<RawEntryRef> model = rawEntryComboBox.getModel();
        if (rawEntryId == null) {
            rawEntryComboBox.setSelectedIndex(0);
            return;
        }

        for (int i = 0; i < model.getSize(); i++) {
            RawEntryRef item = model.getElementAt(i);
            if (rawEntryId.equals(item.id())) {
                rawEntryComboBox.setSelectedIndex(i);
                return;
            }
        }

        rawEntryComboBox.setSelectedIndex(0);
    }

    private void refreshLinkedRawPreview() {
        RawEntryRef selected = (RawEntryRef) rawEntryComboBox.getSelectedItem();
        if (selected == null || selected.id() == null) {
            rawTextArea.setText("");
            rawGlossArea.setText("");
            return;
        }

        RawEntry raw = rawEntryService.getById(selected.id());
        if (raw == null) {
            rawTextArea.setText("");
            rawGlossArea.setText("");
            return;
        }

        rawTextArea.setText(nullSafe(raw.getRawText()));
        rawGlossArea.setText(nullSafe(raw.getGlossText()));
    }

    private String buildLabel(RawEntry raw) {
        String preview = nullSafe(raw.getTranslationText());
        if (preview.isBlank()) {
            preview = nullSafe(raw.getRawText());
        }
        preview = preview.replace('\n', ' ').trim();
        if (preview.length() > 50) {
            preview = preview.substring(0, 47) + "...";
        }
        return "#" + raw.getId() + " — " + preview;
    }

    private static String formatInstant(Instant instant) {
        return instant == null ? "" : instant.toString();
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
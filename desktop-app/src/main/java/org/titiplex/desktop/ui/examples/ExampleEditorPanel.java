package org.titiplex.desktop.ui.examples;

import org.titiplex.desktop.domain.example.Example;
import org.titiplex.desktop.domain.example.ExampleSource;
import org.titiplex.desktop.domain.example.ExampleStatus;

import javax.swing.*;
import java.awt.*;
import java.time.Instant;

public final class ExampleEditorPanel extends JPanel {
    private final JTextField externalIdField = new JTextField();
    private final JTextField sourceNameField = new JTextField();
    private final JTextField sourceRefField = new JTextField();
    private final JTextArea surfaceArea = new JTextArea(4, 80);
    private final JTextArea normalizedArea = new JTextArea(4, 80);
    private final JTextArea glossArea = new JTextArea(4, 80);
    private final JTextArea translationArea = new JTextArea(4, 80);
    private final JTextArea notesArea = new JTextArea(4, 80);
    private final JComboBox<ExampleStatus> statusBox = new JComboBox<>(ExampleStatus.values());

    private Example currentExample;

    public ExampleEditorPanel() {
        setLayout(new BorderLayout(8, 8));

        JPanel header = new JPanel(new GridLayout(4, 1, 8, 8));
        header.setBorder(BorderFactory.createTitledBorder("Example metadata"));
        header.add(externalIdField);
        header.add(sourceNameField);
        header.add(sourceRefField);
        header.add(statusBox);

        surfaceArea.setBorder(BorderFactory.createTitledBorder("Surface"));
        normalizedArea.setBorder(BorderFactory.createTitledBorder("Normalized"));
        glossArea.setBorder(BorderFactory.createTitledBorder("Gloss"));
        translationArea.setBorder(BorderFactory.createTitledBorder("Translation"));
        notesArea.setBorder(BorderFactory.createTitledBorder("Notes"));

        JPanel center = new JPanel(new GridLayout(5, 1, 8, 8));
        center.add(new JScrollPane(surfaceArea));
        center.add(new JScrollPane(normalizedArea));
        center.add(new JScrollPane(glossArea));
        center.add(new JScrollPane(translationArea));
        center.add(new JScrollPane(notesArea));

        add(header, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
    }

    public void setExample(Example example) {
        this.currentExample = example;
        if (example == null) {
            externalIdField.setText("");
            sourceNameField.setText("");
            sourceRefField.setText("");
            surfaceArea.setText("");
            normalizedArea.setText("");
            glossArea.setText("");
            translationArea.setText("");
            notesArea.setText("");
            statusBox.setSelectedItem(ExampleStatus.RAW);
            return;
        }

        externalIdField.setText(example.externalId());
        sourceNameField.setText(example.source() == null ? "" : example.source().sourceName());
        sourceRefField.setText(example.source() == null ? "" : example.source().sourceRef());
        surfaceArea.setText(example.surfaceText());
        normalizedArea.setText(example.normalizedText());
        glossArea.setText(example.glossText());
        translationArea.setText(example.translationText());
        notesArea.setText(example.notes());
        statusBox.setSelectedItem(example.status());
    }

    public Example toExample() {
        Instant now = Instant.now();
        return new Example(
                currentExample == null ? null : currentExample.id(),
                externalIdField.getText().trim(),
                surfaceArea.getText(),
                normalizedArea.getText(),
                glossArea.getText(),
                translationArea.getText(),
                notesArea.getText(),
                new ExampleSource(sourceNameField.getText().trim(), sourceRefField.getText().trim()),
                (ExampleStatus) statusBox.getSelectedItem(),
                currentExample == null ? now : currentExample.createdAt(),
                now
        );
    }
}

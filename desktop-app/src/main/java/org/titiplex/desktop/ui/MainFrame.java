package org.titiplex.desktop.ui;

import org.titiplex.desktop.model.RuleRecord;
import org.titiplex.desktop.model.ValidationResult;
import org.titiplex.desktop.service.RuleCatalogService;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.time.Instant;

public final class MainFrame extends JFrame {
    private final RuleCatalogService service;
    private final RuleTableModel tableModel = new RuleTableModel();
    private final JTable table = new JTable(tableModel);
    private final JTextField idField = new JTextField();
    private final JTextField nameField = new JTextField();
    private final JCheckBox enabledField = new JCheckBox("Enabled");
    private final JTextArea yamlArea = new JTextArea();
    private final JTextArea outputArea = new JTextArea();

    private RuleRecord selected;

    public MainFrame(RuleCatalogService service) {
        super("Chuj NLP Rule Studio");
        this.service = service;
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null);
        initComponents();
        reloadRules();
    }

    private void initComponents() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton importButton = new JButton("Import YAML");
        JButton validateButton = new JButton("Validate rules");
        JButton exportYamlButton = new JButton("Export YAML");
        JButton exportJsonButton = new JButton("Export JSON");
        JButton saveButton = new JButton("Save edits");

        toolbar.add(importButton);
        toolbar.add(validateButton);
        toolbar.add(exportYamlButton);
        toolbar.add(exportJsonButton);
        toolbar.add(saveButton);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(table), createEditorPanel());
        splitPane.setDividerLocation(450);

        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        setLayout(new BorderLayout());
        add(toolbar, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        add(new JScrollPane(outputArea), BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                selected = tableModel.at(table.convertRowIndexToModel(table.getSelectedRow()));
                bind(selected);
            }
        });

        importButton.addActionListener(e -> chooseAndImport());
        validateButton.addActionListener(e -> validateRules());
        exportYamlButton.addActionListener(e -> chooseAndExportYaml());
        exportJsonButton.addActionListener(e -> chooseAndExportJson());
        saveButton.addActionListener(e -> saveCurrent());
    }

    private JPanel createEditorPanel() {
        JPanel form = new JPanel(new BorderLayout());
        JPanel fields = new JPanel(new GridLayout(3, 1, 8, 8));
        fields.add(labeled("Rule ID", idField));
        fields.add(labeled("Name", nameField));
        fields.add(enabledField);

        yamlArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        form.add(fields, BorderLayout.NORTH);
        form.add(new JScrollPane(yamlArea), BorderLayout.CENTER);
        return form;
    }

    private JPanel labeled(String label, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(label), BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private void chooseAndImport() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            int count = service.importYaml(chooser.getSelectedFile().toPath());
            reloadRules();
            write("Imported " + count + " rules from " + chooser.getSelectedFile().getName());
        }
    }

    private void validateRules() {
        ValidationResult result = service.validateAll();
        write(String.join("\n", result.messages()));
    }

    private void chooseAndExportYaml() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            service.exportYaml(chooser.getSelectedFile().toPath());
            write("YAML export completed: " + chooser.getSelectedFile());
        }
    }

    private void chooseAndExportJson() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            service.exportJson(chooser.getSelectedFile().toPath());
            write("JSON export completed: " + chooser.getSelectedFile());
        }
    }

    private void saveCurrent() {
        if (selected == null) {
            write("Select a rule to edit first.");
            return;
        }
        RuleRecord updated = new RuleRecord(
                selected.id(),
                idField.getText().trim(),
                nameField.getText().trim(),
                enabledField.isSelected(),
                yamlArea.getText(),
                selected.sourceFile(),
                Instant.now()
        );
        service.saveRule(updated);
        reloadRules();
        write("Saved rule " + updated.ruleId());
    }

    private void bind(RuleRecord record) {
        idField.setText(record.ruleId());
        nameField.setText(record.name());
        enabledField.setSelected(record.enabled());
        yamlArea.setText(record.yamlBody());
    }

    private void reloadRules() {
        tableModel.setRows(service.listRules());
    }

    private void write(String text) {
        outputArea.setText(text);
    }
}
package org.titiplex.app.ui.frame;

import org.titiplex.app.service.CorrectedEntryService;
import org.titiplex.app.service.RawEntryService;
import org.titiplex.app.service.RuleService;
import org.titiplex.app.ui.common.Dialogs;
import org.titiplex.app.ui.entry.EntryPanel;
import org.titiplex.app.ui.rule.RulePanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainFrame extends JFrame {
    private final JLabel statusLabel = new JLabel("Ready");
    private final RulePanel rulePanel;
    private final EntryPanel entryPanel;

    public MainFrame(
            RuleService ruleService,
            CorrectedEntryService correctedEntryService,
            RawEntryService rawEntryService
    ) {
        super("Chuj NLP Studio");

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 760));
        setSize(1280, 840);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        this.rulePanel = new RulePanel(ruleService, this::setStatus);
        this.entryPanel = new EntryPanel(correctedEntryService, rawEntryService, this::setStatus);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Rules", rulePanel);
        tabs.addTab("Entries", entryPanel);

        add(tabs, BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);
        setJMenuBar(buildMenuBar());
    }

    private JComponent buildStatusBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(6, 10, 6, 10));
        panel.add(statusLabel, BorderLayout.WEST);
        return panel;
    }

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");

        JMenuItem newRuleItem = new JMenuItem("New rule");
        newRuleItem.addActionListener(event -> {
            rulePanel.createNewRule();
            setStatus("New rule editor opened.");
        });

        JMenuItem newEntryItem = new JMenuItem("New corrected entry");
        newEntryItem.addActionListener(event -> {
            entryPanel.createNewEntry();
            setStatus("New entry editor opened.");
        });

        JMenuItem exportRulesItem = new JMenuItem("Export rules YAML");
        exportRulesItem.addActionListener(event -> rulePanel.exportYaml());

        JMenuItem refreshItem = new JMenuItem("Refresh all");
        refreshItem.addActionListener(event -> {
            rulePanel.refresh();
            entryPanel.refresh();
            setStatus("Data refreshed.");
        });

        JMenuItem quitItem = new JMenuItem("Quit");
        quitItem.addActionListener(event -> dispose());

        fileMenu.add(newRuleItem);
        fileMenu.add(newEntryItem);
        fileMenu.addSeparator();
        fileMenu.add(exportRulesItem);
        fileMenu.add(refreshItem);
        fileMenu.addSeparator();
        fileMenu.add(quitItem);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(event ->
                Dialogs.info(this, "Chuj NLP Studio\nDesktop editor for rules and annotated entries.")
        );
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        return menuBar;
    }

    private void setStatus(String status) {
        statusLabel.setText(status == null || status.isBlank() ? "Ready" : status);
    }
}
package org.titiplex.app.ui.frame;

import org.springframework.stereotype.Component;
import org.titiplex.app.service.*;
import org.titiplex.app.ui.common.Dialogs;
import org.titiplex.app.ui.entry.EntryPanel;
import org.titiplex.app.ui.raw.RawEntryPanel;
import org.titiplex.app.ui.rule.RulePanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

@Component
public class MainFrame extends JFrame {
    private final JLabel statusLabel = new JLabel("Ready");

    private final RulePanel rulePanel;
    private final EntryPanel entryPanel;
    private final RawEntryPanel rawEntryPanel;
    private final DesktopExportService exportService;

    public MainFrame(
            RuleService ruleService,
            CorrectedEntryService correctedEntryService,
            RawEntryService rawEntryService,
            CorpusImportService corpusImportService,
            AutoCorrectionService autoCorrectionService,
            DesktopExportService exportService
    ) {
        super("Chuj NLP Studio");

        this.exportService = exportService;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1180, 780));
        setSize(1320, 860);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        this.rulePanel = new RulePanel(ruleService, this::setStatus);
        this.entryPanel = new EntryPanel(correctedEntryService, rawEntryService, this::setStatus);
        this.rawEntryPanel = new RawEntryPanel(
                rawEntryService,
                corpusImportService,
                autoCorrectionService,
                this::setStatus
        );

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Rules", rulePanel);
        tabs.addTab("Raw entries", rawEntryPanel);
        tabs.addTab("Corrected entries", entryPanel);

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

        JMenuItem newRawEntryItem = new JMenuItem("New raw entry");
        newRawEntryItem.addActionListener(event -> {
            rawEntryPanel.createNewEntry();
            setStatus("New raw entry editor opened.");
        });

        JMenuItem exportDocxItem = new JMenuItem("Export corrected DOCX");
        exportDocxItem.addActionListener(event -> exportCorrectedDocx());

        JMenuItem exportStatsItem = new JMenuItem("Export stats TXT");
        exportStatsItem.addActionListener(event -> exportStats());

        JMenuItem refreshItem = new JMenuItem("Refresh all");
        refreshItem.addActionListener(event -> {
            rulePanel.refresh();
            rawEntryPanel.refresh();
            entryPanel.refresh();
            setStatus("Data refreshed.");
        });

        JMenuItem quitItem = new JMenuItem("Quit");
        quitItem.addActionListener(event -> dispose());

        fileMenu.add(newRuleItem);
        fileMenu.add(newRawEntryItem);
        fileMenu.addSeparator();
        fileMenu.add(exportDocxItem);
        fileMenu.add(exportStatsItem);
        fileMenu.addSeparator();
        fileMenu.add(refreshItem);
        fileMenu.addSeparator();
        fileMenu.add(quitItem);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(event ->
                Dialogs.info(this, "Chuj NLP Studio\nWave 1: rules, raw entries, corrected entries, imports and exports.")
        );
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        return menuBar;
    }

    private void exportCorrectedDocx() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("corrected-entries.docx"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try {
            exportService.exportCorrectedDocx(chooser.getSelectedFile().toPath());
            setStatus("Corrected DOCX exported.");
            Dialogs.info(this, "Corrected DOCX exported.");
        } catch (Exception exception) {
            Dialogs.error(this, "Failed to export corrected DOCX", exception);
        }
    }

    private void exportStats() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("corpus-stats.txt"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try {
            exportService.exportStats(chooser.getSelectedFile().toPath());
            setStatus("Stats exported.");
            Dialogs.info(this, "Stats exported.");
        } catch (Exception exception) {
            Dialogs.error(this, "Failed to export stats", exception);
        }
    }

    private void setStatus(String status) {
        statusLabel.setText(status == null || status.isBlank() ? "Ready" : status);
    }
}
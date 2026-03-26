package org.titiplex.app.ui.frame;

import org.springframework.stereotype.Component;
import org.titiplex.app.service.*;
import org.titiplex.app.ui.common.Dialogs;
import org.titiplex.app.ui.conllu.ConlluPanel;
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
    private final ConlluPanel conlluPanel;
    private final DesktopExportService exportService;
    private Runnable quitAction = this::dispose;

    public MainFrame(
            RuleService ruleService,
            CorrectedEntryService correctedEntryService,
            RawEntryService rawEntryService,
            CorpusImportService corpusImportService,
            AutoCorrectionService autoCorrectionService,
            DesktopExportService exportService,
            AnnotationConfigStateService annotationConfigStateService,
            ConlluPreviewService conlluPreviewService,
            AppRefreshCoordinator refreshCoordinator,
            CorrectedEntryStalenessService correctedEntryStalenessService
    ) {
        super("Chuj NLP Studio");

        this.exportService = exportService;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1180, 780));
        setSize(1320, 860);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        this.rulePanel = new RulePanel(
                ruleService,
                correctedEntryStalenessService,
                refreshCoordinator,
                this::setStatus
        );
        this.entryPanel = new EntryPanel(
                correctedEntryService,
                rawEntryService,
                autoCorrectionService,
                refreshCoordinator,
                this::setStatus
        );
        this.rawEntryPanel = new RawEntryPanel(
                rawEntryService,
                corpusImportService,
                autoCorrectionService,
                refreshCoordinator,
                this::setStatus
        );
        this.conlluPanel = new ConlluPanel(
                correctedEntryService,
                annotationConfigStateService,
                conlluPreviewService,
                refreshCoordinator,
                this::setStatus
        );

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Rules", rulePanel);
        tabs.addTab("Raw entries", rawEntryPanel);
        tabs.addTab("Corrected entries", entryPanel);
        tabs.addTab("CoNLL-U", conlluPanel);

        add(tabs, BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);
        setJMenuBar(buildMenuBar());
    }

    public void setQuitAction(Runnable quitAction) {
        this.quitAction = quitAction == null ? this::dispose : quitAction;
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
        JMenu importMenu = new JMenu("Import");
        JMenu exportMenu = new JMenu("Export");
        JMenu newMenu = new JMenu("New");
        JMenu helpMenu = new JMenu("Help");

        JMenuItem importCorpusItem = new JMenuItem("Corpus DOCX/TXT...");
        importCorpusItem.addActionListener(event -> importCorpus());

        JMenuItem importRulesItem = new JMenuItem("Rules YAML...");
        importRulesItem.addActionListener(event -> importRulesYaml());

        JMenuItem importAnnotationItem = new JMenuItem("Annotation YAML...");
        importAnnotationItem.addActionListener(event -> importAnnotationYaml());

        JMenuItem exportRulesItem = new JMenuItem("Rules YAML...");
        exportRulesItem.addActionListener(event -> exportRulesYaml());

        JMenuItem exportDocxItem = new JMenuItem("Corrected DOCX...");
        exportDocxItem.addActionListener(event -> exportCorrectedDocx());

        JMenuItem exportStatsItem = new JMenuItem("Stats TXT...");
        exportStatsItem.addActionListener(event -> exportStats());

        JMenuItem exportConlluSelectedItem = new JMenuItem("Selected CoNLL-U...");
        exportConlluSelectedItem.addActionListener(event -> exportSelectedConllu());

        JMenuItem exportConlluAllItem = new JMenuItem("All CoNLL-U...");
        exportConlluAllItem.addActionListener(event -> exportCorpusConllu());

        JMenuItem newRuleItem = new JMenuItem("Rule");
        newRuleItem.addActionListener(event -> {
            rulePanel.createNewRule();
            setStatus("New rule editor opened.");
        });

        JMenuItem newRawEntryItem = new JMenuItem("Raw entry");
        newRawEntryItem.addActionListener(event -> {
            rawEntryPanel.createNewEntry();
            setStatus("New raw entry editor opened.");
        });

        JMenuItem refreshItem = new JMenuItem("Refresh all");
        refreshItem.addActionListener(event -> {
            rulePanel.refresh();
            rawEntryPanel.refresh();
            entryPanel.refresh();
            conlluPanel.refresh();
            setStatus("Data refreshed.");
        });

        JMenuItem quitItem = new JMenuItem("Quit");
        quitItem.addActionListener(event -> quitAction.run());

        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(event ->
                Dialogs.info(this, "Chuj NLP Studio\nRules, raw entries, corrected entries, CoNLL-U preview/export.")
        );

        importMenu.add(importCorpusItem);
        importMenu.add(importRulesItem);
        importMenu.add(importAnnotationItem);

        exportMenu.add(exportRulesItem);
        exportMenu.add(exportDocxItem);
        exportMenu.add(exportStatsItem);
        exportMenu.addSeparator();
        exportMenu.add(exportConlluSelectedItem);
        exportMenu.add(exportConlluAllItem);

        newMenu.add(newRuleItem);
        newMenu.add(newRawEntryItem);

        fileMenu.add(refreshItem);
        fileMenu.addSeparator();
        fileMenu.add(quitItem);

        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(importMenu);
        menuBar.add(exportMenu);
        menuBar.add(newMenu);
        menuBar.add(helpMenu);
        return menuBar;
    }

    private void importCorpus() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("corpus.docx"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        rawEntryPanel.importFile(chooser.getSelectedFile().toPath());
    }

    private void importRulesYaml() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("rules.yaml"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        rulePanel.importYaml(chooser.getSelectedFile().toPath());
    }

    private void exportRulesYaml() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("rules-export.yaml"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        rulePanel.exportYaml(chooser.getSelectedFile().toPath());
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

    private void exportCorpusConllu() {
        conlluPanel.exportAllFromMenu();
    }

    private void setStatus(String status) {
        statusLabel.setText(status == null || status.isBlank() ? "Ready" : status);
    }

    private void importAnnotationYaml() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("annotation.yaml"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        conlluPanel.loadAnnotationConfigFromMenu(chooser.getSelectedFile().toPath());
    }

    private void exportSelectedConllu() {
        conlluPanel.exportSelectedFromMenu();
    }
}
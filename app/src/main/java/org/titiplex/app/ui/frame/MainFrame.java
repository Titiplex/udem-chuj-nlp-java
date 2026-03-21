package org.titiplex.app.ui.frame;

import org.titiplex.app.service.RuleService;
import org.titiplex.app.ui.common.Dialogs;
import org.titiplex.app.ui.rule.RulePanel;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private final JLabel statusLabel = new JLabel("Ready");

    public MainFrame(RuleService ruleService) {
        super("test");

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        RulePanel rulePanel = new RulePanel(ruleService);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Rules", rulePanel);

        add(tabs, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem exportProjectItem = new JMenuItem("Export project JSON");
        exportProjectItem.addActionListener(event -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            try {
//                projectExportService.exportProject(chooser.getSelectedFile().toPath());
                statusLabel.setText("Project exported.");
            } catch (Exception exception) {
                Dialogs.error(this, "Failed to export project", exception);
            }
        });
        fileMenu.add(exportProjectItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
    }
}

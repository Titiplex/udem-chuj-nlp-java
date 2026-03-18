package org.titiplex.desktop;

import com.formdev.flatlaf.FlatDarkLaf;
import org.titiplex.desktop.db.DatabaseManager;
import org.titiplex.desktop.db.RuleRepository;
import org.titiplex.desktop.service.RuleCatalogService;
import org.titiplex.desktop.ui.MainFrame;

import javax.swing.*;
import java.nio.file.Path;

public final class DesktopMain {

    public static void main(String[] args) {
        FlatDarkLaf.setup();

        Path dbPath = Path.of(System.getProperty("user.home"), ".chuj-rule-studio", "rules-db");
        dbPath.getParent().toFile().mkdirs();

        DatabaseManager databaseManager = new DatabaseManager(dbPath);
        databaseManager.init();

        RuleCatalogService service = new RuleCatalogService(new RuleRepository(databaseManager));

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(service);
            frame.setVisible(true);
        });
    }
}

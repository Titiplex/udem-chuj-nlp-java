package org.titiplex.desktop.bootstrap;

import com.formdev.flatlaf.FlatDarkLaf;
import org.titiplex.desktop.infra.json.ProjectJsonExporter;
import org.titiplex.desktop.infra.yaml.RuleYamlExporter;
import org.titiplex.desktop.infra.yaml.RuleYamlImporter;
import org.titiplex.desktop.persistence.jpa.*;
import org.titiplex.desktop.service.correction.CorrectionService;
import org.titiplex.desktop.service.correction.RuleApplicationService;
import org.titiplex.desktop.service.example.ExampleImportService;
import org.titiplex.desktop.service.example.ExampleQueryService;
import org.titiplex.desktop.service.project.ProjectExportService;
import org.titiplex.desktop.service.rule.RuleEditorService;
import org.titiplex.desktop.service.rule.RuleExportService;
import org.titiplex.desktop.service.rule.RuleImportService;
import org.titiplex.desktop.service.rule.RuleValidationService;
import org.titiplex.desktop.ui.frame.MainFrame;

import javax.swing.*;

public final class DesktopMain {
    private DesktopMain() {
    }

    public static void main(String[] args) {
        AppConfig config = AppConfig.defaultConfig();
        config.appHome().toFile().mkdirs();

        if (config.darkTheme()) {
            FlatDarkLaf.setup();
        }

        JpaBootstrap jpaBootstrap = new JpaBootstrap(config);
        jpaBootstrap.migrate();
        var entityManagerFactory = jpaBootstrap.buildEntityManagerFactory();

        var ruleRepository = new JpaRuleRepository(entityManagerFactory);
        var exampleRepository = new JpaExampleRepository(entityManagerFactory);
        var correctionRepository = new JpaCorrectionRepository(entityManagerFactory);
        var lexiconRepository = new JpaLexiconRepository(entityManagerFactory);

        var ruleYamlImporter = new RuleYamlImporter();
        var ruleYamlExporter = new RuleYamlExporter();
        var projectJsonExporter = new ProjectJsonExporter();

        var ruleImportService = new RuleImportService(ruleRepository, ruleYamlImporter);
        var ruleValidationService = new RuleValidationService(ruleRepository);
        var ruleExportService = new RuleExportService(ruleRepository, ruleValidationService, ruleYamlExporter);
        var ruleEditorService = new RuleEditorService(ruleRepository, ruleValidationService);

        var exampleImportService = new ExampleImportService(exampleRepository);
        var exampleQueryService = new ExampleQueryService(exampleRepository);

        var ruleApplicationService = new RuleApplicationService(ruleValidationService);
        var correctionService = new CorrectionService(correctionRepository, exampleRepository, ruleRepository);
        var projectExportService = new ProjectExportService(
                ruleRepository,
                exampleRepository,
                correctionRepository,
                lexiconRepository,
                projectJsonExporter
        );

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(
                    config,
                    ruleImportService,
                    ruleEditorService,
                    ruleValidationService,
                    ruleExportService,
                    exampleImportService,
                    exampleQueryService,
                    ruleApplicationService,
                    correctionService,
                    projectExportService
            );
            frame.setVisible(true);
        });

        Runtime.getRuntime().addShutdownHook(new Thread(entityManagerFactory::close));
    }
}

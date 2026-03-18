package org.titiplex.desktop.service.project;

import org.titiplex.desktop.infra.json.ProjectJsonExporter;
import org.titiplex.desktop.persistence.repository.CorrectionRepository;
import org.titiplex.desktop.persistence.repository.ExampleRepository;
import org.titiplex.desktop.persistence.repository.LexiconRepository;
import org.titiplex.desktop.persistence.repository.RuleRepository;

import java.nio.file.Path;

public final class ProjectExportService {
    private final RuleRepository ruleRepository;
    private final ExampleRepository exampleRepository;
    private final CorrectionRepository correctionRepository;
    private final LexiconRepository lexiconRepository;
    private final ProjectJsonExporter projectJsonExporter;

    public ProjectExportService(
            RuleRepository ruleRepository,
            ExampleRepository exampleRepository,
            CorrectionRepository correctionRepository,
            LexiconRepository lexiconRepository,
            ProjectJsonExporter projectJsonExporter
    ) {
        this.ruleRepository = ruleRepository;
        this.exampleRepository = exampleRepository;
        this.correctionRepository = correctionRepository;
        this.lexiconRepository = lexiconRepository;
        this.projectJsonExporter = projectJsonExporter;
    }

    public void exportProject(Path outputFile) {
        projectJsonExporter.export(
                ruleRepository.findAll(),
                exampleRepository.findAll(),
                correctionRepository.findAll(),
                lexiconRepository.findAll(),
                outputFile
        );
    }
}

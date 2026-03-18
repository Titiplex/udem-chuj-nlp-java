package org.titiplex.desktop.service.rule;

import org.titiplex.desktop.domain.rule.Rule;
import org.titiplex.desktop.domain.validation.ValidationRun;
import org.titiplex.desktop.infra.yaml.RuleYamlExporter;
import org.titiplex.desktop.persistence.repository.RuleRepository;

import java.nio.file.Path;
import java.util.List;

public final class RuleExportService {
    private final RuleRepository ruleRepository;
    private final RuleValidationService validationService;
    private final RuleYamlExporter exporter;

    public RuleExportService(
            RuleRepository ruleRepository,
            RuleValidationService validationService,
            RuleYamlExporter exporter
    ) {
        this.ruleRepository = ruleRepository;
        this.validationService = validationService;
        this.exporter = exporter;
    }

    public void exportYaml(Path outputPath) {
        ValidationRun validation = validationService.validateAll();
        if (!validation.ok()) {
            throw new IllegalStateException("Refusing to export invalid rules");
        }
        exporter.writeRules(ruleRepository.findAll(), outputPath);
    }

    public List<Rule> currentRules() {
        return ruleRepository.findAll();
    }
}

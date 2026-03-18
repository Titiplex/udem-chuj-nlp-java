package org.titiplex.desktop.service.rule;

import org.titiplex.desktop.domain.rule.Rule;
import org.titiplex.desktop.infra.yaml.RuleYamlImporter;
import org.titiplex.desktop.persistence.repository.RuleRepository;

import java.nio.file.Path;
import java.util.List;

public final class RuleImportService {
    private final RuleRepository ruleRepository;
    private final RuleYamlImporter importer;

    public RuleImportService(RuleRepository ruleRepository, RuleYamlImporter importer) {
        this.ruleRepository = ruleRepository;
        this.importer = importer;
    }

    public int importYaml(Path path) {
        List<Rule> rules = importer.readRules(path);
        ruleRepository.saveAll(rules);
        return rules.size();
    }
}

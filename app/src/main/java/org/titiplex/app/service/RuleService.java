package org.titiplex.app.service;

import org.springframework.stereotype.Service;
import org.titiplex.app.domain.validation.ValidationRun;
import org.titiplex.app.infra.yaml.YamlEI;
import org.titiplex.app.persistence.entity.Rule;
import org.titiplex.app.persistence.repository.RuleRepository;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public final class RuleService {
    private final RuleRepository ruleRepository;
    private final RuleValidationService validationService;
    private final YamlEI exporter = new YamlEI();

    public RuleService(
            RuleRepository ruleRepository,
            RuleValidationService validationService
    ) {
        this.ruleRepository = ruleRepository;
        this.validationService = validationService;
    }

    public List<Rule> getAll() {
        return ruleRepository.findAll();
    }

    public Optional<Rule> findById(Long id) {
        return ruleRepository.findById(id);
    }

    public void delete(Long id) {
        ruleRepository.deleteById(id);
    }

    public Rule save(Rule rule) {
        ValidationRun validation = validationService.validateRule(rule);
        if (!validation.ok()) {
            throw new IllegalStateException(validation.summary());
        }
        return ruleRepository.save(rule);
    }

    public void saveAll(Collection<Rule> rules) {
        ruleRepository.saveAll(rules);
    }

    public ValidationRun validate(Rule rule) {
        return validationService.validateRule(rule);
    }

    public ValidationRun validateAll() {
        return validationService.validateAll();
    }

    public void exportYaml(Path outputPath) {
        ValidationRun validation = validationService.validateAll();
        if (!validation.ok()) {
            throw new IllegalStateException("Refusing to export invalid rules");
        }
        exporter.writeRules(getAll(), outputPath);
    }

    public int importYaml(Path path) {
        List<Rule> rules = exporter.readRules(path);
        for (Rule rule : rules) {
            ValidationRun validation = validationService.validateRule(rule);
            if (!validation.ok()) {
                throw new IllegalStateException(
                        "Invalid imported rule '" + rule.getStableId() + "': " + validation.summary()
                );
            }
        }
        ruleRepository.saveAll(rules);
        return rules.size();
    }
}
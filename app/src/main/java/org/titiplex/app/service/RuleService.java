package org.titiplex.app.service;

import org.springframework.stereotype.Service;
import org.titiplex.app.domain.validation.ValidationRun;
import org.titiplex.app.infra.yaml.YamlEI;
import org.titiplex.app.persistence.entity.Rule;
import org.titiplex.app.persistence.entity.RuleKind;
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

    public List<Rule> getAllByKind(RuleKind kind) {
        return ruleRepository.findAllByKindOrderByStableIdAsc(kind);
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
        if (rule.getKind() == null) {
            rule.setKind(RuleKind.CORRECTION);
        }
        return ruleRepository.save(rule);
    }

    public void saveAll(Collection<Rule> rules) {
        for (Rule rule : rules) {
            if (rule.getKind() == null) {
                rule.setKind(RuleKind.CORRECTION);
            }
        }
        ruleRepository.saveAll(rules);
    }

    public ValidationRun validate(Rule rule) {
        return validationService.validateRule(rule);
    }

    public ValidationRun validateAll() {
        return validationService.validateAll();
    }

    public void exportYaml(Path outputPath, RuleKind kind) {
        List<Rule> rules = getAllByKind(kind);
        exporter.writeRules(rules, outputPath, kind);
    }

    public int importYaml(Path path, RuleKind kind) {
        List<Rule> rules = exporter.readRules(path, kind);
        for (Rule rule : rules) {
            rule.setKind(kind);
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
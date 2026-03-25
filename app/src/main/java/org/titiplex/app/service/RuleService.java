package org.titiplex.app.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.titiplex.app.domain.validation.ValidationRun;
import org.titiplex.app.infra.yaml.YamlEI;
import org.titiplex.app.persistence.entity.Rule;
import org.titiplex.app.persistence.entity.RuleKind;
import org.titiplex.app.persistence.repository.RuleRepository;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RuleService {
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

    @Transactional(readOnly = true)
    public List<Rule> getAll() {
        return ruleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Rule> getAllByKind(RuleKind kind) {
        return ruleRepository.findAllByKindOrderByStableIdAsc(kind);
    }

    @Transactional(readOnly = true)
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
            ValidationRun validation = validationService.validateRule(rule);
            if (!validation.ok()) {
                throw new IllegalStateException(validation.summary());
            }
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
        List<Rule> importedRules = exporter.readRules(path, kind);
        List<Rule> toSave = new ArrayList<>();

        for (Rule importedRule : importedRules) {
            importedRule.setKind(kind);
            ValidationRun validation = validationService.validateRule(importedRule);
            if (!validation.ok()) {
                throw new IllegalStateException(
                        "Invalid imported rule '" + importedRule.getStableId() + "': " + validation.summary()
                );
            }
            Rule target = ruleRepository.findByStableId(importedRule.getStableId())
                    .map(existing -> mergeImportedRule(existing, importedRule))
                    .orElse(importedRule);

            target.setKind(kind);
            target.setSourceFile(path.getFileName().toString());
            toSave.add(target);
        }
        ruleRepository.saveAll(toSave);
        return toSave.size();
    }

    private Rule mergeImportedRule(Rule existing, Rule importedRule) {
        existing.setName(importedRule.getName());
        existing.setDescription(importedRule.getDescription());
        existing.setYamlBody(importedRule.getYamlBody());
        existing.setEnabled(importedRule.isEnabled());
        existing.setKind(importedRule.getKind());
        existing.setSourceFile(importedRule.getSourceFile());
        existing.setVersionNo(Math.max(1, existing.getVersionNo()) + 1);
        return existing;
    }
}
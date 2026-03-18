package org.titiplex.desktop.service.rule;

import org.titiplex.desktop.domain.rule.Rule;
import org.titiplex.desktop.domain.validation.ValidationRun;
import org.titiplex.desktop.persistence.repository.RuleRepository;

import java.util.List;
import java.util.Optional;

public final class RuleEditorService {
    private final RuleRepository ruleRepository;
    private final RuleValidationService validationService;

    public RuleEditorService(RuleRepository ruleRepository, RuleValidationService validationService) {
        this.ruleRepository = ruleRepository;
        this.validationService = validationService;
    }

    public List<Rule> listRules() {
        return ruleRepository.findAll();
    }

    public Optional<Rule> findById(Long id) {
        return ruleRepository.findById(id);
    }

    public Rule save(Rule rule) {
        ValidationRun validation = validationService.validateRule(rule);
        if (!validation.ok()) {
            throw new IllegalStateException(validation.summary());
        }
        return ruleRepository.save(rule);
    }

    public void delete(Long id) {
        ruleRepository.deleteById(id);
    }
}

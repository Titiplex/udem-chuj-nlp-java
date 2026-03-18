package org.titiplex.desktop.persistence.repository;

import org.titiplex.desktop.domain.rule.Rule;
import org.titiplex.desktop.domain.rule.RuleId;

import java.util.List;
import java.util.Optional;

public interface RuleRepository {
    List<Rule> findAll();

    Optional<Rule> findById(Long id);

    Optional<Rule> findByRuleId(RuleId ruleId);

    Rule save(Rule rule);

    void saveAll(List<Rule> rules);

    void deleteById(Long id);
}

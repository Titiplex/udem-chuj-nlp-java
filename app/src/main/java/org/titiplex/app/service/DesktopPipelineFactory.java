package org.titiplex.app.service;

import org.springframework.stereotype.Service;
import org.titiplex.app.persistence.entity.Rule;
import org.titiplex.app.persistence.repository.RuleRepository;
import org.titiplex.rules.CorrectionRule;
import org.titiplex.rules.PythonStyleYamlRuleLoader;
import org.titiplex.rules.RuleEngine;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class DesktopPipelineFactory {
    private final RuleRepository ruleRepository;

    public DesktopPipelineFactory(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    public RuleEngine createRuleEngine() {
        PythonStyleYamlRuleLoader loader = new PythonStyleYamlRuleLoader();
        List<CorrectionRule> compiledRules = new ArrayList<>();

        for (Rule rule : ruleRepository.findAll()) {
            if (!rule.isEnabled()) {
                continue;
            }
            if (rule.getYamlBody() == null || rule.getYamlBody().isBlank()) {
                continue;
            }

            ByteArrayInputStream in = new ByteArrayInputStream(
                    rule.getYamlBody().getBytes(StandardCharsets.UTF_8)
            );
            compiledRules.addAll(loader.load(in));
        }

        return new RuleEngine(compiledRules);
    }
}
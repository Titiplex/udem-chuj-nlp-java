package org.titiplex.desktop.service.rule;

import org.titiplex.desktop.domain.rule.Rule;
import org.titiplex.desktop.domain.validation.ValidationMessage;
import org.titiplex.desktop.domain.validation.ValidationRun;
import org.titiplex.desktop.persistence.repository.RuleRepository;
import org.titiplex.rules.PythonStyleYamlRuleLoader;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class RuleValidationService {
    private final RuleRepository ruleRepository;

    public RuleValidationService(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    public ValidationRun validateAll() {
        Instant start = Instant.now();
        List<ValidationMessage> messages = new ArrayList<>();
        Set<String> seenIds = new HashSet<>();

        for (Rule rule : ruleRepository.findAll()) {
            validateRule(rule, messages, seenIds);
        }

        boolean ok = messages.stream().noneMatch(msg -> "ERROR".equals(msg.severity()));
        String summary = ok
                ? "Validation successful. " + ruleRepository.findAll().size() + " rules checked."
                : "Validation failed with " + messages.size() + " issue(s).";

        return new ValidationRun(null, start, Instant.now(), ok, summary, List.copyOf(messages));
    }

    public ValidationRun validateRule(Rule rule) {
        Instant start = Instant.now();
        List<ValidationMessage> messages = new ArrayList<>();
        validateRule(rule, messages, new HashSet<>());
        boolean ok = messages.stream().noneMatch(msg -> "ERROR".equals(msg.severity()));
        String summary = ok ? "Rule is valid" : "Rule has " + messages.size() + " issue(s)";
        return new ValidationRun(null, start, Instant.now(), ok, summary, List.copyOf(messages));
    }

    private void validateRule(Rule rule, List<ValidationMessage> messages, Set<String> seenIds) {
        if (rule.ruleId() == null || rule.ruleId().value().isBlank()) {
            messages.add(new ValidationMessage("ERROR", "Missing rule id", rule.id(), null));
            return;
        }

        if (!seenIds.add(rule.ruleId().value())) {
            messages.add(new ValidationMessage("ERROR", "Duplicate rule id: " + rule.ruleId().value(), rule.id(), null));
        }

        if (rule.yamlBody() == null || rule.yamlBody().isBlank()) {
            messages.add(new ValidationMessage("ERROR", "Empty YAML body", rule.id(), null));
            return;
        }

        try (var stream = new ByteArrayInputStream(rule.yamlBody().getBytes(StandardCharsets.UTF_8))) {
            int generatedCorrections = new PythonStyleYamlRuleLoader().load(stream).size();
            if (generatedCorrections == 0) {
                messages.add(new ValidationMessage("WARN", "Rule does not generate executable corrections", rule.id(), null));
            }
        } catch (Exception exception) {
            messages.add(new ValidationMessage("ERROR", "Validation error: " + exception.getMessage(), rule.id(), null));
        }
    }
}

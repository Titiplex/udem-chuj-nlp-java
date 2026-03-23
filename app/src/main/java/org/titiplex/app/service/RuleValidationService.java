package org.titiplex.app.service;

import org.springframework.stereotype.Service;
import org.titiplex.app.domain.validation.ValidationMessage;
import org.titiplex.app.domain.validation.ValidationRun;
import org.titiplex.app.persistence.entity.Rule;
import org.titiplex.app.persistence.entity.RuleKind;
import org.titiplex.app.persistence.repository.RuleRepository;
import org.titiplex.conllu.AnnotationConfig;
import org.titiplex.conllu.AnnotationConfigLoader;
import org.titiplex.rules.PythonStyleYamlRuleLoader;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
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
        if (rule.getStableId() == null || rule.getStableId().isBlank()) {
            messages.add(new ValidationMessage("ERROR", "Missing rule id", rule.getId(), null));
            return;
        }

        if (!seenIds.add(rule.getStableId())) {
            messages.add(new ValidationMessage("ERROR", "Duplicate rule id: " + rule.getStableId(), rule.getId(), null));
        }

        if (rule.getYamlBody() == null || rule.getYamlBody().isBlank()) {
            messages.add(new ValidationMessage("ERROR", "Empty YAML body", rule.getId(), null));
            return;
        }

        try (var stream = new ByteArrayInputStream(rule.getYamlBody().getBytes(StandardCharsets.UTF_8))) {
            if (rule.getKind() == RuleKind.CONLLU) {
                AnnotationConfig config = new AnnotationConfigLoader().load(stream);
                if (config.rules().isEmpty()) {
                    messages.add(new ValidationMessage("WARN", "Annotation config contains no rules", rule.getId(), null));
                }
            } else {
                int generatedCorrections = new PythonStyleYamlRuleLoader().load(stream).size();
                if (generatedCorrections == 0) {
                    messages.add(new ValidationMessage("WARN", "Rule does not generate executable corrections", rule.getId(), null));
                }
            }
        } catch (Exception exception) {
            messages.add(new ValidationMessage("ERROR", "Validation error: " + exception.getMessage(), rule.getId(), null));
        }
    }
}
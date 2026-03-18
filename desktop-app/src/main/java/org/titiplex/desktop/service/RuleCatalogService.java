package org.titiplex.desktop.service;

import org.titiplex.desktop.db.RuleRepository;
import org.titiplex.desktop.model.RuleRecord;
import org.titiplex.desktop.model.ValidationResult;
import org.titiplex.rules.PythonStyleYamlRuleLoader;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class RuleCatalogService {
    private final RuleRepository repository;
    private final Yaml yaml = new Yaml();

    public RuleCatalogService(RuleRepository repository) {
        this.repository = repository;
    }

    @SuppressWarnings("unchecked")
    public int importYaml(Path path) {
        try (InputStream input = Files.newInputStream(path)) {
            Map<String, Object> root = yaml.load(input);
            if (root == null) {
                repository.replaceAll(List.of());
                return 0;
            }
            List<Map<String, Object>> rules = (List<Map<String, Object>>) root.getOrDefault("rules", List.of());
            List<RuleRecord> records = new ArrayList<>();
            for (Map<String, Object> rule : rules) {
                String id = string(rule.getOrDefault("id", rule.getOrDefault("name", "unnamed_rule")));
                String name = string(rule.getOrDefault("name", id));
                Map<String, Object> singleRuleRoot = new LinkedHashMap<>();
                singleRuleRoot.put("rules", List.of(rule));
                records.add(new RuleRecord(null, id, name, true, yaml.dump(singleRuleRoot), path.getFileName().toString(), Instant.now()));
            }
            repository.replaceAll(records);
            return records.size();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to import YAML", e);
        }
    }

    public List<RuleRecord> listRules() {
        return repository.findAll();
    }

    public void saveRule(RuleRecord rule) {
        repository.save(rule);
    }

    public ValidationResult validateAll() {
        List<RuleRecord> rules = repository.findAll();
        List<String> messages = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        int validCount = 0;

        for (RuleRecord rule : rules) {
            if (rule.ruleId() == null || rule.ruleId().isBlank()) {
                messages.add("Missing rule_id for DB row id=" + rule.id());
                continue;
            }
            if (!ids.add(rule.ruleId())) {
                messages.add("Duplicate rule id: " + rule.ruleId());
            }

            try (InputStream stream = new java.io.ByteArrayInputStream(rule.yamlBody().getBytes())) {
                int size = new PythonStyleYamlRuleLoader().load(stream).size();
                if (size == 0) {
                    messages.add("Rule " + rule.ruleId() + " does not generate executable corrections.");
                } else {
                    validCount++;
                }
            } catch (Exception ex) {
                messages.add("Rule " + rule.ruleId() + " failed validation: " + ex.getMessage());
            }
        }

        if (messages.isEmpty()) {
            messages.add("Validation successful. " + validCount + " rules are executable.");
        }
        return new ValidationResult(messages.size() == 1 && messages.getFirst().startsWith("Validation successful"), messages);
    }

    public void exportYaml(Path outputFile) {
        ValidationResult validationResult = validateAll();
        if (!validationResult.ok()) {
            throw new IllegalStateException("Fix validation errors before export: " + String.join(" | ", validationResult.messages()));
        }
        List<Map<String, Object>> exportedRules = new ArrayList<>();
        for (RuleRecord record : repository.findAll()) {
            exportedRules.add(extractSingleRule(record.yamlBody()));
        }
        Map<String, Object> root = new HashMap<>();
        root.put("rules", exportedRules);
        try (OutputStream output = Files.newOutputStream(outputFile)) {
            output.write(yaml.dump(root).getBytes());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to export YAML", e);
        }
    }

    public void exportJson(Path outputFile) {
        ValidationResult validationResult = validateAll();
        if (!validationResult.ok()) {
            throw new IllegalStateException("Fix validation errors before export: " + String.join(" | ", validationResult.messages()));
        }

        StringBuilder builder = new StringBuilder();
        builder.append("{\n  \"rules\": [\n");
        List<RuleRecord> rules = repository.findAll();
        for (int i = 0; i < rules.size(); i++) {
            RuleRecord rule = rules.get(i);
            builder.append("    {\"ruleId\": \"").append(escape(rule.ruleId()))
                    .append("\", \"name\": \"").append(escape(rule.name()))
                    .append("\", \"enabled\": ").append(rule.enabled())
                    .append("}");
            if (i < rules.size() - 1) {
                builder.append(",");
            }
            builder.append("\n");
        }
        builder.append("  ]\n}\n");

        try {
            Files.writeString(outputFile, builder.toString());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to export JSON", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractSingleRule(String yamlBody) {
        Map<String, Object> root = yaml.load(yamlBody);
        if (root == null) {
            return Map.of();
        }
        List<Map<String, Object>> rules = (List<Map<String, Object>>) root.getOrDefault("rules", List.of());
        return rules.isEmpty() ? Map.of() : rules.getFirst();
    }

    private static String string(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

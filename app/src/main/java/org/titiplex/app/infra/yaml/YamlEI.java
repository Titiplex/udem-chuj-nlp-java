package org.titiplex.app.infra.yaml;

import org.titiplex.app.domain.rule.RuleId;
import org.titiplex.app.domain.rule.RuleVersion;
import org.titiplex.app.persistence.entity.Rule;
import org.titiplex.app.persistence.entity.RuleKind;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class YamlEI {
    private final Yaml yaml = new Yaml();

    @SuppressWarnings("unchecked")
    public void writeRules(List<Rule> rules, Path outputPath, RuleKind kind) {
        Map<String, Object> output = new LinkedHashMap<>();

        if (kind == RuleKind.CONLLU) {
            List<Map<String, Object>> mergedRules = new ArrayList<>();

            for (Rule rule : rules) {
                Map<String, Object> root = loadYamlMap(rule.getYamlBody());
                if (root == null) {
                    continue;
                }

                mergeMapSection(output, "def", root.get("def"));
                mergeMapSection(output, "gloss_map", root.get("gloss_map"));
                mergeMapSection(output, "lexicons", root.get("lexicons"));
                mergeMapSection(output, "extractors", root.get("extractors"));
                mergeListOrScalarSection(output, "extractors_file", root.get("extractors_file"));
                mergeListOrScalarSection(output, "rules_file", root.get("rules_file"));

                List<Map<String, Object>> rawRules =
                        (List<Map<String, Object>>) root.getOrDefault("rules", List.of());
                mergedRules.addAll(rawRules);
            }

            output.put("rules", mergedRules);
        } else {
            List<Map<String, Object>> exportedRules = new ArrayList<>();

            for (Rule rule : rules) {
                Map<String, Object> root = loadYamlMap(rule.getYamlBody());
                if (root == null) {
                    continue;
                }

                List<Map<String, Object>> rawRules =
                        (List<Map<String, Object>>) root.getOrDefault("rules", List.of());
                exportedRules.addAll(rawRules);
            }

            output.put("rules", exportedRules);
        }

        try (OutputStream stream = Files.newOutputStream(outputPath)) {
            stream.write(yaml.dump(output).getBytes(StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to export rules to YAML", exception);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Rule> readRules(Path path, RuleKind kind) {
        try (InputStream input = Files.newInputStream(path)) {
            Map<String, Object> root = yaml.load(input);
            if (root == null) {
                return List.of();
            }

            List<Map<String, Object>> rules =
                    (List<Map<String, Object>>) root.getOrDefault("rules", List.of());
            List<Rule> out = new ArrayList<>();

            Map<String, Object> sharedRoot = new LinkedHashMap<>(root);
            sharedRoot.remove("rules");

            for (Map<String, Object> rawRule : rules) {
                String stableId = string(rawRule.getOrDefault("id", rawRule.getOrDefault("name", "unnamed_rule")));
                String name = string(rawRule.getOrDefault("name", stableId));
                String description = string(rawRule.get("description"));

                Map<String, Object> singleRuleRoot = new LinkedHashMap<>();
                if (kind == RuleKind.CONLLU) {
                    singleRuleRoot.putAll(deepCopy(sharedRoot));
                }
                singleRuleRoot.put("rules", List.of(rawRule));

                out.add(new Rule(
                        null,
                        new RuleId(stableId).toString(),
                        name,
                        kind,
                        true,
                        yaml.dump(singleRuleRoot),
                        path.getFileName().toString(),
                        description,
                        new RuleVersion(1).value(),
                        Instant.now(),
                        Instant.now()
                ));
            }

            return out;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read YAML rules from " + path, exception);
        }
    }

    private Map<String, Object> deepCopy(Map<String, Object> input) {
        if (input == null || input.isEmpty()) {
            return new LinkedHashMap<>();
        }
        return yaml.load(yaml.dump(input));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadYamlMap(String body) {
        Object loaded = yaml.load(body);
        if (loaded instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void mergeMapSection(Map<String, Object> target, String key, Object raw) {
        if (!(raw instanceof Map<?, ?> sourceMap) || sourceMap.isEmpty()) {
            return;
        }

        Map<String, Object> targetMap = (Map<String, Object>) target.computeIfAbsent(key, ignored -> new LinkedHashMap<>());
        for (Map.Entry<?, ?> entry : sourceMap.entrySet()) {
            targetMap.put(String.valueOf(entry.getKey()), entry.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private void mergeListOrScalarSection(Map<String, Object> target, String key, Object raw) {
        if (raw == null) {
            return;
        }

        List<Object> targetList = (List<Object>) target.computeIfAbsent(key, ignored -> new ArrayList<>());

        if (raw instanceof List<?> list) {
            for (Object item : list) {
                if (!targetList.contains(item)) {
                    targetList.add(item);
                }
            }
        } else if (!targetList.contains(raw)) {
            targetList.add(raw);
        }
    }

    private static String string(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
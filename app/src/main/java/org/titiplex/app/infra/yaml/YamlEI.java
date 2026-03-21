package org.titiplex.app.infra.yaml;

import org.titiplex.app.domain.rule.RuleId;
import org.titiplex.app.domain.rule.RuleVersion;
import org.titiplex.app.persistence.entity.Rule;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

public class YamlEI {
    private final Yaml yaml = new Yaml();

    @SuppressWarnings("unchecked")
    public void writeRules(List<Rule> rules, Path outputPath) {
        List<Map<String, Object>> exportedRules = new ArrayList<>();
        for (Rule rule : rules) {
            Map<String, Object> root = yaml.load(rule.getYamlBody());
            if (root == null) {
                continue;
            }
            List<Map<String, Object>> rawRules = (List<Map<String, Object>>) root.getOrDefault("rules", List.of());
            if (!rawRules.isEmpty()) {
                exportedRules.add(rawRules.getFirst());
            }
        }

        Map<String, Object> output = new HashMap<>();
        output.put("rules", exportedRules);

        try (OutputStream stream = Files.newOutputStream(outputPath)) {
            stream.write(yaml.dump(output).getBytes());
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to export rules to YAML", exception);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Rule> readRules(Path path) {
        try (InputStream input = Files.newInputStream(path)) {
            Map<String, Object> root = yaml.load(input);
            if (root == null) {
                return List.of();
            }

            List<Map<String, Object>> rules = (List<Map<String, Object>>) root.getOrDefault("rules", List.of());
            List<Rule> out = new ArrayList<>();

            for (Map<String, Object> rawRule : rules) {
                String stableId = string(rawRule.getOrDefault("id", rawRule.getOrDefault("name", "unnamed_rule")));
                String name = string(rawRule.getOrDefault("name", stableId));
                String description = string(rawRule.get("description"));

                Map<String, Object> singleRuleRoot = new LinkedHashMap<>();
                singleRuleRoot.put("rules", List.of(rawRule));

                out.add(new Rule(
                        null,
                        new RuleId(stableId).toString(),
                        name,
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

    private static String string(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}

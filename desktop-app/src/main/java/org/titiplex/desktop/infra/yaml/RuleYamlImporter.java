package org.titiplex.desktop.infra.yaml;

import org.titiplex.desktop.domain.rule.Rule;
import org.titiplex.desktop.domain.rule.RuleId;
import org.titiplex.desktop.domain.rule.RuleVersion;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RuleYamlImporter {
    private final Yaml yaml = new Yaml();

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
                        new RuleId(stableId),
                        name,
                        true,
                        yaml.dump(singleRuleRoot),
                        path.getFileName().toString(),
                        description,
                        new RuleVersion(1),
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

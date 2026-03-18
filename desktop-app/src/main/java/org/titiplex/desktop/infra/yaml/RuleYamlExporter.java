package org.titiplex.desktop.infra.yaml;

import org.titiplex.desktop.domain.rule.Rule;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RuleYamlExporter {
    private final Yaml yaml = new Yaml();

    @SuppressWarnings("unchecked")
    public void writeRules(List<Rule> rules, Path outputPath) {
        List<Map<String, Object>> exportedRules = new ArrayList<>();
        for (Rule rule : rules) {
            Map<String, Object> root = yaml.load(rule.yamlBody());
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
}

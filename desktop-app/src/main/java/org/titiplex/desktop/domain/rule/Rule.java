package org.titiplex.desktop.domain.rule;

import java.time.Instant;

public record Rule(
        Long id,
        RuleId ruleId,
        String name,
        boolean enabled,
        String yamlBody,
        String sourceFile,
        String description,
        RuleVersion version,
        Instant createdAt,
        Instant updatedAt
) {
    public Rule withYamlBody(String newYamlBody) {
        return new Rule(id, ruleId, name, enabled, newYamlBody, sourceFile, description, version, createdAt, updatedAt);
    }
}

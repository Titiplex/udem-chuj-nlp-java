package org.titiplex.desktop.model;

import java.time.Instant;

public record RuleRecord(
        Long id,
        String ruleId,
        String name,
        boolean enabled,
        String yamlBody,
        String sourceFile,
        Instant updatedAt
) {
}

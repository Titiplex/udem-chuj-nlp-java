package org.titiplex.app.domain.validation;

public record ValidationMessage(
        String severity,
        String message,
        Long ruleId,
        Long exampleId
) {
}
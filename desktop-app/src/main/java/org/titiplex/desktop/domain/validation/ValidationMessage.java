package org.titiplex.desktop.domain.validation;

public record ValidationMessage(
        String severity,
        String message,
        Long ruleId,
        Long exampleId
) {
}

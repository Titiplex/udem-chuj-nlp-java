package org.titiplex.desktop.domain.correction;

import java.time.Instant;

public record Correction(
        Long id,
        Long exampleId,
        Long ruleId,
        String beforePayload,
        String afterPayload,
        CorrectionDecision decision,
        CorrectionOrigin origin,
        String comment,
        Instant createdAt
) {
}

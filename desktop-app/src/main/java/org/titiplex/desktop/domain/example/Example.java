package org.titiplex.desktop.domain.example;

import java.time.Instant;

public record Example(
        Long id,
        String externalId,
        String surfaceText,
        String normalizedText,
        String glossText,
        String translationText,
        String notes,
        ExampleSource source,
        ExampleStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}

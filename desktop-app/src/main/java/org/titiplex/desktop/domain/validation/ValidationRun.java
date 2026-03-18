package org.titiplex.desktop.domain.validation;

import java.time.Instant;
import java.util.List;

public record ValidationRun(
        Long id,
        Instant startedAt,
        Instant finishedAt,
        boolean ok,
        String summary,
        List<ValidationMessage> messages
) {
}

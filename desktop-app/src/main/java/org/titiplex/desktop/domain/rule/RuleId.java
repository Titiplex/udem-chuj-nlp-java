package org.titiplex.desktop.domain.rule;

import java.util.Objects;

public record RuleId(String value) {
    public RuleId {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("rule id cannot be blank");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}

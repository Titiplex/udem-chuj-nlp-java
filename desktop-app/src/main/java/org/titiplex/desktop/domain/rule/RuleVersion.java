package org.titiplex.desktop.domain.rule;

public record RuleVersion(int value) {
    public RuleVersion {
        if (value < 1) {
            throw new IllegalArgumentException("version must be >= 1");
        }
    }

    public RuleVersion next() {
        return new RuleVersion(value + 1);
    }
}

package org.titiplex.rules;

public interface CorrectionRule {
    String id();

    void apply(RuleContext context);
}

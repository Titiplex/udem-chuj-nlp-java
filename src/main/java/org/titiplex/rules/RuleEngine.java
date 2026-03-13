package org.titiplex.rules;

import org.titiplex.model.AlignedToken;

import java.util.List;

public final class RuleEngine {
    private final List<CorrectionRule> rules;

    public RuleEngine(List<CorrectionRule> rules) {
        this.rules = List.copyOf(rules);
    }

    public List<AlignedToken> apply(List<AlignedToken> input) {
        RuleContext context = new RuleContext(input);
        for (CorrectionRule rule : rules) {
            rule.apply(context);
        }
        return context.alignedTokens();
    }
}

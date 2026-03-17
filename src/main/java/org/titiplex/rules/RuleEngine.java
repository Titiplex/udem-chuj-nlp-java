package org.titiplex.rules;

import org.titiplex.model.AlignedToken;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class RuleEngine {
    private final List<CorrectionRule> rules;
    private final Map<String, Set<String>> lexicons;

    public RuleEngine(List<CorrectionRule> rules) {
        this(rules, Map.of());
    }

    public RuleEngine(List<CorrectionRule> rules, Map<String, Set<String>> lexicons) {
        this.rules = List.copyOf(rules);
        this.lexicons = lexicons == null ? Map.of() : lexicons;
    }

    public List<AlignedToken> apply(List<AlignedToken> input) {
        RuleContext context = new RuleContext(input, lexicons);
        for (CorrectionRule rule : rules) {
            rule.apply(context);
        }
        return context.alignedTokens();
    }
}
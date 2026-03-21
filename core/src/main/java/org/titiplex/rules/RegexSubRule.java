package org.titiplex.rules;

import org.titiplex.model.AlignedToken;

import java.util.List;
import java.util.regex.Pattern;

public final class RegexSubRule implements CorrectionRule {
    private final String id;
    private final MatchSpec spec;
    private final String scope;
    private final Pattern pattern;
    private final String replacement;

    public RegexSubRule(String id, MatchSpec spec, String scope, Pattern pattern, String replacement) {
        this.id = id;
        this.spec = spec;
        this.scope = scope;
        this.pattern = pattern;
        this.replacement = replacement;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void apply(RuleContext context) {
        for (int i = 0; i < context.size(); i++) {
            int target = TokenPatternMatcher.resolveTargetIndex(context.alignedTokens(), i, spec, context);
            if (target < 0) {
                continue;
            }
            AlignedToken token = context.get(target);
            List<String> newChuj = token.chujSegments();
            List<String> newGloss = token.glossSegments();
            if ("chuj".equals(scope) || "both".equals(scope)) {
                String rewritten = pattern.matcher(String.join("-", token.chujSegments())).replaceAll(replacement);
                newChuj = rewritten.isBlank() ? List.of() : List.of(rewritten.split("-"));
            }
            if ("gloss".equals(scope) || "both".equals(scope)) {
                String rewritten = pattern.matcher(String.join("-", token.glossSegments())).replaceAll(replacement);
                newGloss = rewritten.isBlank() ? List.of() : List.of(rewritten.split("-"));
            }
            context.replace(target, context.rebuildToken(newChuj, newGloss));
        }
    }
}

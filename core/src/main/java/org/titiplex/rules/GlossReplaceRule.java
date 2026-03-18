package org.titiplex.rules;

import org.titiplex.model.AlignedToken;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class GlossReplaceRule implements CorrectionRule {
    private final String id;
    private final Pattern pattern;
    private final String replacement;
    private final boolean ignoreCase;

    public GlossReplaceRule(String id, String regex, String replacement, boolean ignoreCase) {
        this.id = id;
        this.pattern = Pattern.compile(regex, ignoreCase ? Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE : 0);
        this.replacement = replacement;
        this.ignoreCase = ignoreCase;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void apply(RuleContext context) {
        List<AlignedToken> snapshot = context.alignedTokens();
        for (int i = 0; i < snapshot.size(); i++) {
            AlignedToken token = snapshot.get(i);
            List<String> rewrittenGloss = new ArrayList<>();
            for (String segment : token.glossSegments()) {
                rewrittenGloss.add(pattern.matcher(segment).replaceAll(replacement));
            }
            String glossSurface = String.join("-", rewrittenGloss);
            context.replace(i, AlignedToken.of(
                    token.chujSurface(),
                    glossSurface,
                    token.chujSegments(),
                    rewrittenGloss
            ));
        }
    }
}

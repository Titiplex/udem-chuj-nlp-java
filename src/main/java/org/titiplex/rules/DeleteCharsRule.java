package org.titiplex.rules;

import org.titiplex.align.Tokenizer;
import org.titiplex.model.AlignedToken;

import java.util.List;

public final class DeleteCharsRule implements CorrectionRule {
    private final String id;
    private final List<String> chars;

    public DeleteCharsRule(String id, List<String> chars) {
        this.id = id;
        this.chars = List.copyOf(chars);
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
            String chuj = token.chujSurface();
            String gloss = token.glossSurface();
            for (String c : chars) {
                chuj = chuj.replace(c, "");
                gloss = gloss.replace(c, "");
            }
            context.replace(i, AlignedToken.of(chuj, gloss,
                    Tokenizer.tokenizeWord(chuj),
                    Tokenizer.tokenizeWord(gloss)));
        }
    }
}

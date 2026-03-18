package org.titiplex.rules;

import org.titiplex.model.AlignedToken;

import java.util.ArrayList;
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
        for (int i = 0; i < context.size(); i++) {
            AlignedToken tok = context.get(i);
            List<String> newChuj = cleanList(tok.chujSegments());
            List<String> newGloss = cleanList(tok.glossSegments());

            if (newChuj.equals(tok.chujSegments()) && newGloss.equals(tok.glossSegments())) {
                continue;
            }

            context.replace(i, context.rebuildToken(newChuj, newGloss));
            if (newChuj.isEmpty() && !newGloss.isEmpty() && i < context.size() - 1) {
                // Move orphaned glosses to the next token
                AlignedToken next = context.get(i + 1);
                List<String> mergedGloss = new ArrayList<>(newGloss);
                mergedGloss.addAll(next.glossSegments());
                context.replace(i + 1, context.rebuildToken(next.chujSegments(), mergedGloss));
                // Clear the current token's glosses since they've been moved
                context.replace(i, context.rebuildToken(newChuj, List.of()));
            }

        }
    }

    private List<String> cleanList(List<String> parts) {
        List<String> out = new ArrayList<>();
        for (String part : parts) {
            String cleaned = part;
            for (String c : chars) {
                cleaned = cleaned.replace(c, "");
            }
            if (!cleaned.isBlank()) {
                out.add(cleaned);
            }
        }
        return out;
    }
}

package org.titiplex.rules;

import org.titiplex.model.AlignedToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class DeletePartsRule implements CorrectionRule {
    private final String id;
    private final Set<String> parts;

    public DeletePartsRule(String id, List<String> parts) {
        this.id = id;
        this.parts = Set.copyOf(parts);
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void apply(RuleContext context) {
        for (int i = 0; i < context.size(); i++) {
            AlignedToken tok = context.get(i);
            List<String> newChuj = new ArrayList<>();
            List<String> newGloss = new ArrayList<>();
            int max = Math.max(tok.chujSegments().size(), tok.glossSegments().size());
            for (int idx = 0; idx < max; idx++) {
                String ch = idx < tok.chujSegments().size() ? tok.chujSegments().get(idx) : null;
                String gl = idx < tok.glossSegments().size() ? tok.glossSegments().get(idx) : null;

                boolean dropCh = ch != null && parts.contains(ch);
                boolean dropGl = gl != null && parts.contains(gl);
                if (!dropCh && ch != null && !ch.isBlank()) {
                    newChuj.add(ch);
                }
                if (!dropGl && !dropCh && gl != null && !gl.isBlank()) {
                    newGloss.add(gl);
                }
            }
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
}

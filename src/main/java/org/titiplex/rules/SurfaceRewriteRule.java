package org.titiplex.rules;

import org.titiplex.model.AlignedToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class SurfaceRewriteRule implements CorrectionRule {
    private final String id;
    private final MatchSpec spec;
    private final Map<String, String> surfaceMap;
    private final Map<String, String> glossMap;

    public SurfaceRewriteRule(String id, MatchSpec spec, Map<String, String> surfaceMap, Map<String, String> glossMap) {
        this.id = id;
        this.spec = spec;
        this.surfaceMap = surfaceMap;
        this.glossMap = glossMap;
    }

    @Override public String id() { return id; }

    @Override
    public void apply(RuleContext context) {
        for (int i = 0; i < context.size(); i++) {
            if (!TokenPatternMatcher.matchesAt(context.alignedTokens(), i, spec)) continue;
            AlignedToken tok = context.get(i);
            List<String> newChuj = new ArrayList<>(tok.chujSegments());
            boolean modified = false;
            
            for (int k = 0; k < newChuj.size(); k++) {
                String repl = surfaceMap.get(norm(newChuj.get(k)));
                if (repl != null) {
                    newChuj.set(k, repl);
                    modified = true;
                }
            }
            
            // Try to match the reconstructed surface from segments
            String reconstructedSurface = String.join("-", tok.chujSegments());
            String surfaceRepl = surfaceMap.get(norm(reconstructedSurface));
            if (surfaceRepl != null) {
                newChuj = List.of(surfaceRepl.split("-"));
                modified = true;
            }
            
            List<String> newGloss = new ArrayList<>(tok.glossSegments());
            for (int k = 0; k < newGloss.size(); k++) {
                String repl = glossMap.get(norm(newGloss.get(k)));
                if (repl != null) {
                    newGloss.set(k, repl);
                    modified = true;
                }
            }
            
            if (modified) {
                context.replace(i, context.rebuildToken(newChuj, newGloss));
            }
        }
    }

    private String norm(String s) { return s == null ? "" : s.toLowerCase(Locale.ROOT); }
}

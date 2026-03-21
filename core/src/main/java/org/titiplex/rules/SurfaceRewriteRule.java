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

    public SurfaceRewriteRule(String id,
                              MatchSpec spec,
                              Map<String, String> surfaceMap,
                              Map<String, String> glossMap) {
        this.id = id;
        this.spec = spec;
        this.surfaceMap = surfaceMap;
        this.glossMap = glossMap;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void apply(RuleContext context) {
        for (int i = 0; i < context.size(); i++) {
            if (!TokenPatternMatcher.matchesAt(context.alignedTokens(), i, spec, context)) {
                continue;
            }

            AlignedToken tok = context.get(i);

            List<String> newChuj = new ArrayList<>(tok.chujSegments());
            List<String> newGloss = new ArrayList<>(tok.glossSegments());
            boolean modified = false;

            // 1) Rewrite Chuj segments individually
            for (int k = 0; k < newChuj.size(); k++) {
                String repl = surfaceMap.get(norm(newChuj.get(k)));
                if (repl != null) {
                    newChuj.set(k, repl);
                    modified = true;
                }
            }

            // 2) Rewrite full Chuj surface if present in mapping
            String reconstructedChujSurface = String.join("-", tok.chujSegments());
            String chujSurfaceRepl = surfaceMap.get(norm(reconstructedChujSurface));
            if (chujSurfaceRepl != null) {
                newChuj = splitSurface(chujSurfaceRepl);
                modified = true;
            }

            // 3) Rewrite gloss segments individually
            for (int k = 0; k < newGloss.size(); k++) {
                String repl = glossMap.get(norm(newGloss.get(k)));
                if (repl != null) {
                    newGloss.set(k, repl);
                    modified = true;
                }
            }

            // 4) Rewrite full gloss surface if present in mapping
            String reconstructedGlossSurface = String.join("-", tok.glossSegments());
            String glossSurfaceRepl = glossMap.get(norm(reconstructedGlossSurface));
            if (glossSurfaceRepl != null) {
                newGloss = splitSurface(glossSurfaceRepl);
                modified = true;
            }

            if (modified) {
                context.replace(i, context.rebuildToken(newChuj, newGloss));
            }
        }
    }

    private static List<String> splitSurface(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return List.of(value.split("-"));
    }

    private String norm(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT);
    }
}
package org.titiplex.conllu;

import org.titiplex.model.AlignedToken;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public final class AnnotationRule {
    private final String name;
    private final String scope;
    private final int priority;
    private final Pattern regex;
    private final Set<String> inList;
    private final boolean onGloss;
    private final String lexiconRef;
    private final String upos;
    private final Map<String, String> feats;
    private final Map<String, String> featsTemplate;
    private final List<Map<String, String>> matchExtracts;
    private final List<Map<String, String>> setExtracts;
    private final List<String> require;
    private final List<String> forbid;

    public AnnotationRule(
            String name,
            String scope,
            int priority,
            Pattern regex,
            Set<String> inList,
            boolean onGloss,
            String lexiconRef,
            String upos,
            Map<String, String> feats,
            Map<String, String> featsTemplate,
            List<Map<String, String>> matchExtracts,
            List<Map<String, String>> setExtracts,
            List<String> require,
            List<String> forbid
    ) {
        this.name = name == null ? "" : name;
        this.scope = scope == null || scope.isBlank() ? "token" : scope;
        this.priority = priority;
        this.regex = regex;
        this.inList = inList == null ? Set.of() : Set.copyOf(inList);
        this.onGloss = onGloss;
        this.lexiconRef = lexiconRef;
        this.upos = upos == null ? "" : upos;
        this.feats = feats == null ? Map.of() : Map.copyOf(feats);
        this.featsTemplate = featsTemplate == null ? Map.of() : Map.copyOf(featsTemplate);
        this.matchExtracts = matchExtracts == null ? List.of() : List.copyOf(matchExtracts);
        this.setExtracts = setExtracts == null ? List.of() : List.copyOf(setExtracts);
        this.require = require == null ? List.of() : List.copyOf(require);
        this.forbid = forbid == null ? List.of() : List.copyOf(forbid);
    }

    public boolean matches(AlignedToken tok, AnnotationConfig config, Map<String, Object> ctx) {
        for (Map<String, String> ex : matchExtracts) {
            if ("scan_agreement".equalsIgnoreCase(ex.get("type"))) {
                config.applyExtractor(ex.getOrDefault("extractor", "agreement_verbs"), tok, null, ctx);
            }
        }
        for (String key : require) {
            if (TemplateResolver.resolvePath(ctx, key).isBlank()) return false;
        }
        for (String key : forbid) {
            if (!TemplateResolver.resolvePath(ctx, key).isBlank()) return false;
        }
        if (!lexiconRef.isBlank()) {
            boolean matched = false;
            for (String g : tok.glossSegments()) {
                if (config.lexiconRegistry().contains(lexiconRef, g)) {
                    matched = true;
                    break;
                }
            }
            if (!matched) return false;
        }

        List<String> parts = onGloss ? tok.glossSegments() : tok.chujSegments();
        String surface = onGloss ? tok.glossSurface() : tok.chujSurface();

        if ("morpheme".equalsIgnoreCase(scope)) {
            for (String part : parts) {
                if (matchesValue(part)) return true;
            }
            return regex == null && inList.isEmpty() && (lexiconRef != null || !matchExtracts.isEmpty());
        }

        if (matchesValue(surface)) return true;
        for (String part : parts) {
            if (matchesValue(part)) return true;
        }
        return regex == null && inList.isEmpty() && (lexiconRef != null || !matchExtracts.isEmpty() || !require.isEmpty());
    }

    private boolean matchesValue(String value) {
        if (value == null) return false;
        if (regex != null && regex.matcher(value).find()) return true;
        if (inList.isEmpty()) return false;
        String norm = value.toLowerCase(Locale.ROOT);
        return inList.stream().map(v -> v.toLowerCase(Locale.ROOT)).anyMatch(norm::equals);
    }

    public void apply(ConlluLine line, AlignedToken tok, AnnotationConfig config, Map<String, Object> ctx) {
        if (!upos.isBlank() && "_".equals(line.upos())) line.setUpos(upos);
        line.putAllFeats(feats, false);

        for (Map<String, String> ex : setExtracts) {
            if ("scan_agreement".equalsIgnoreCase(ex.get("type"))) {

                config.applyExtractor(
                        (String) ex.getOrDefault("extractor", "agreement_verbs"),
                        (String) ex.get("into"),
                        tok,
                        line,
                        ctx
                );
            }
        }
        for (var e : featsTemplate.entrySet()) {
            String resolved = TemplateResolver.render(e.getValue(), ctx);
            if (!resolved.isBlank()) line.putFeat(e.getKey(), resolved, false);
        }
    }


    public int priority() {
        return priority;
    }

    public String scope() {
        return scope;
    }

    public String name() {
        return name;
    }

}

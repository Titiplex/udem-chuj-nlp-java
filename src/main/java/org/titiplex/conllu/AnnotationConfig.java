package org.titiplex.conllu;

import org.titiplex.model.AlignedToken;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AnnotationConfig {
    private final GlossMapper glossMapper = new GlossMapper();
    private final List<AnnotationRule> rules = new ArrayList<>();
    private final Map<String, ExtractorDef> extractors = new LinkedHashMap<>();
    private final Set<String> posDefinitions = new LinkedHashSet<>();
    private final Set<String> featDefinitions = new LinkedHashSet<>();
    private final LexiconRegistry lexiconRegistry = new LexiconRegistry();

    public GlossMapper glossMapper() {
        return glossMapper;
    }

    public List<AnnotationRule> rules() {
        return rules;
    }

    public Map<String, ExtractorDef> extractors() {
        return extractors;
    }

    public Set<String> posDefinitions() {
        return posDefinitions;
    }

    public Set<String> featDefinitions() {
        return featDefinitions;
    }

    public LexiconRegistry lexiconRegistry() {
        return lexiconRegistry;
    }

    public void applyExtractor(String name, AlignedToken token, ConlluLine line, Map<String, Object> ctx) {
        ExtractorDef ex = extractors.get(name);
        if (ex == null) return;
        Map<String, Object> intoMap = ex.extract(token);
        if (intoMap.isEmpty()) return;
        ctx.put(name, intoMap);
        if (line == null) return;
        for (RoutingRule rr : ex.routing()) {
            if (!ConditionEvaluator.evaluate(rr.when(), intoMap)) continue;
            Map<String, String> feats = new LinkedHashMap<>();
            for (var e : rr.set().entrySet()) {
                String resolved = TemplateResolver.resolvePath(intoMap, e.getValue());
                if (!resolved.isBlank()) feats.put(e.getKey(), resolved);
            }
            line.putAllFeats(feats, false);
            break;
        }
    }


    public record ExtractorDef(
            String name,
            Pattern tagPattern,
            List<RoutingRule> routing
    ) {
        public Map<String, Object> extract(AlignedToken token) {
            Map<String, Object> out = new LinkedHashMap<>();
            for (String gloss : token.glossSegments()) {
                Matcher m = tagPattern.matcher(gloss.toUpperCase(Locale.ROOT));
                if (!m.matches()) continue;

                String series = m.group("series");
                String person = m.group("person");
                String number = m.group("number");

                Map<String, String> values = new LinkedHashMap<>();
                values.put("person", person);
                values.put("number", number != null && !number.isBlank() ? "Plur" : "Sing");
                out.put(series, values);
            }
            return out;
        }
    }

    public record RoutingRule(String when, Map<String, String> set) {
    }
}

package org.titiplex.conllu;

import org.titiplex.rules.RuleYamlSupport;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

public final class AnnotationConfigLoader {
    public AnnotationConfig load(Path yamlPath) throws IOException {
        try (InputStream in = Files.newInputStream(yamlPath)) {
            return load(in, yamlPath.getParent());
        }
    }

    public AnnotationConfig load(InputStream inputStream) {
        return load(inputStream, null);
    }

    @SuppressWarnings("unchecked")
    public AnnotationConfig load(InputStream inputStream, Path baseDir) {
        AnnotationConfig config = new AnnotationConfig();
        Yaml yaml = new Yaml();
        Map<String, Object> data = yaml.load(inputStream);
        if (data == null) return config;

        Map<String, Object> lexicons = RuleYamlSupport.map(data.get("lexicons"));
        for (var e : lexicons.entrySet()) {
            String name = e.getKey();
            Set<String> values = loadLexiconValues(e.getValue(), baseDir);
            config.lexiconRegistry().put(name, values);
        }

        Map<String, Object> def = RuleYamlSupport.map(data.get("def"));
        config.posDefinitions().addAll(RuleYamlSupport.stringList(def.get("pos")));
        config.featDefinitions().addAll(RuleYamlSupport.stringList(def.get("feats")));

        Map<String, Object> glossMap = RuleYamlSupport.map(data.get("gloss_map"));
        for (Map<String, Object> pos : RuleYamlSupport.mapList(glossMap.get("pos"))) {
            for (var entry : pos.entrySet()) {
                if (entry.getValue() != null) config.glossMapper().putPos(entry.getKey(), entry.getValue().toString());
            }
        }
        for (Map<String, Object> feat : RuleYamlSupport.mapList(glossMap.get("feats"))) {
            for (var entry : feat.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof List<?> pair && pair.size() >= 2) {
                    config.glossMapper().putFeat(entry.getKey(), Map.of(pair.get(0).toString(), pair.get(1).toString()));
                } else {
                    config.glossMapper().putFeat(entry.getKey(), castStringMap(RuleYamlSupport.map(value)));
                }
            }
        }

        Map<String, Object> extractors = RuleYamlSupport.map(data.get("extractors"));
        for (var e : extractors.entrySet()) {
            Map<String, Object> ex = RuleYamlSupport.map(e.getValue());
            List<AnnotationConfig.RoutingRule> routingRules = new ArrayList<>();
            Object routingObj = ex.get("routing");
            if (routingObj instanceof List<?> list) {
                for (Object item : list) {
                    Map<String, Object> rr = RuleYamlSupport.map(item);
                    routingRules.add(new AnnotationConfig.RoutingRule(
                            RuleYamlSupport.string(rr.get("when"), ""),
                            castStringMap(RuleYamlSupport.map(rr.get("set")))
                    ));
                }
            }
            Map<String, Object> tagSchema = RuleYamlSupport.map(ex.get("tag_schema"));
            Pattern tagPattern = buildTagPattern(tagSchema);
            config.extractors().put(e.getKey(), new AnnotationConfig.ExtractorDef(e.getKey(), tagPattern, routingRules));
        }

        List<Map<String, Object>> rules = (List<Map<String, Object>>) data.getOrDefault("rules", List.of());
        for (Map<String, Object> rawRule : rules) {
            Map<String, Object> match = RuleYamlSupport.map(rawRule.get("match"));
            String scope = RuleYamlSupport.string(rawRule.get("scope"), "token");
            int priority = RuleYamlSupport.intValue(rawRule.get("priority"), 0);

            boolean onGloss = false;
            Pattern regex = null;
            Set<String> inList = new LinkedHashSet<>();
            List<Map<String, String>> matchExtracts = new ArrayList<>();
            List<String> require = new ArrayList<>();
            List<String> forbid = new ArrayList<>();

            Map<String, Object> effectiveMatch = match;
            if (match.containsKey("gloss")) {
                onGloss = true;
                Object glossObj = match.get("gloss");
                if (glossObj instanceof String gs) {
                    inList.add(gs);
                } else {
                    effectiveMatch = RuleYamlSupport.map(glossObj);
                }
            }
            // Backward-compatible:
            // - old configs may still use in_list
            // - new configs should use in_lexicon
            inList.addAll(normalizeStringList(effectiveMatch.get("in_list"), baseDir));
            String regexText = RuleYamlSupport.string(effectiveMatch.get("regex"), "");
            if (!regexText.isBlank()) regex = Pattern.compile(regexText);
            require.addAll(RuleYamlSupport.stringList(effectiveMatch.get("require")));
            forbid.addAll(RuleYamlSupport.stringList(effectiveMatch.get("forbid")));
            matchExtracts.addAll(extractList(effectiveMatch.get("extract")));

            Map<String, Object> set = RuleYamlSupport.map(rawRule.get("set"));
            List<Map<String, String>> setExtracts = extractList(set.get("extract"));

            config.rules().add(new AnnotationRule(
                    RuleYamlSupport.string(rawRule.get("name"), ""),
                    scope,
                    priority,
                    regex,
                    inList,
                    onGloss,
                    RuleYamlSupport.string(effectiveMatch.get("in_lexicon"), ""),
                    RuleYamlSupport.string(set.get("upos"), ""),
                    castStringMap(RuleYamlSupport.map(set.get("feats"))),
                    castStringMap(RuleYamlSupport.map(set.get("feats_template"))),
                    matchExtracts,
                    setExtracts,
                    require,
                    forbid
            ));
        }
        config.rules().sort(Comparator.comparingInt(AnnotationRule::priority).reversed());
        return config;
    }

    private static List<Map<String, String>> extractList(Object raw) {
        List<Map<String, String>> out = new ArrayList<>();
        if (raw instanceof Map<?, ?> m) {
            out.add(castStringMap(RuleYamlSupport.map(m)));
            return out;
        }
        if (raw instanceof List<?> list) {
            for (Object o : list) out.add(castStringMap(RuleYamlSupport.map(o)));
        }
        return out;
    }

    private static Set<String> normalizeStringList(Object raw, Path baseDir) {
        Set<String> out = new LinkedHashSet<>();
        if (raw == null) return out;
        if (raw instanceof String s) {
            loadListOrLiteral(out, s, baseDir);
            return out;
        }
        for (String s : RuleYamlSupport.stringList(raw)) loadListOrLiteral(out, s, baseDir);
        return out;
    }

    private static void loadListOrLiteral(Set<String> out, String value, Path baseDir) {
        if (value == null || value.isBlank()) return;

        Path candidate = resolveCandidate(baseDir, value);
        if (candidate != null) {
            try {
                out.addAll(Files.readAllLines(candidate).stream()
                        .map(String::trim)
                        .filter(s -> !s.isBlank())
                        .toList());
                return;
            } catch (IOException ignored) {
            }
        }

        out.add(value);
    }

    private static Map<String, String> castStringMap(Map<String, Object> raw) {
        Map<String, String> out = new LinkedHashMap<>();
        for (var e : raw.entrySet()) {
            if (e.getValue() != null) out.put(e.getKey(), e.getValue().toString());
        }
        return out;
    }

    private static Pattern buildTagPattern(Map<String, Object> tagSchema) {
        Map<String, Object> series = RuleYamlSupport.map(tagSchema.get("series"));
        Map<String, Object> values = RuleYamlSupport.map(tagSchema.get("values"));

        String seriesAlt = String.join("|", series.keySet());
        List<String> persons = RuleYamlSupport.stringList(values.get("person"));
        String personAlt = String.join("|", persons);

        Map<String, Object> number = RuleYamlSupport.map(values.get("number"));
        String suffix = RuleYamlSupport.string(number.get("suffix"), "PL");

        String regex =
                "^(?<series>(" + seriesAlt + "))" +
                        "(?<person>(" + personAlt + "))" +
                        "(?<number>" + Pattern.quote(suffix) + ")?$";
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    private static Set<String> loadLexiconValues(Object raw, Path baseDir) {
        Set<String> out = new LinkedHashSet<>();
        if (raw == null) return out;

        if (raw instanceof String s) {
            loadLexiconValueOrPath(out, s, baseDir);
            return out;
        }
        for (String s : RuleYamlSupport.stringList(raw)) {
            loadLexiconValueOrPath(out, s, baseDir);
        }
        return out;
    }

    private static void loadLexiconValueOrPath(Set<String> out, String value, Path baseDir) {
        if (value == null || value.isBlank()) return;

        Path candidate = resolveCandidate(baseDir, value);
        if (candidate != null) {
            try {
                out.addAll(LexiconFileLoader.load(candidate));
                return;
            } catch (IOException ignored) {
            }
        }

        out.add(value);
    }

    private static Path resolveCandidate(Path baseDir, String value) {
        if (baseDir == null) return null;

        Path direct = baseDir.resolve(value).normalize();
        if (Files.exists(direct)) return direct;

        Path parent = baseDir.getParent();
        if (parent != null) {
            Path sibling = parent.resolve(value).normalize();
            if (Files.exists(sibling)) return sibling;
        }

        return null;
    }
}

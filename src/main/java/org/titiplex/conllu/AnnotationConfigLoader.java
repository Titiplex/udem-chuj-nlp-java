package org.titiplex.conllu;

import org.titiplex.rules.RuleYamlSupport;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;

public final class AnnotationConfigLoader {

    @SuppressWarnings("unchecked")
    public AnnotationConfig load(InputStream inputStream) {
        AnnotationConfig config = new AnnotationConfig();
        Yaml yaml = new Yaml();
        Map<String, Object> data = yaml.load(inputStream);
        if (data == null) {
            return config;
        }

        Map<String, Object> glossMap = RuleYamlSupport.map(data.get("gloss_map"));
        for (Map<String, Object> pos : RuleYamlSupport.mapList(glossMap.get("pos"))) {
            for (var entry : pos.entrySet()) {
                config.glossMapper().putPos(entry.getKey(), entry.getValue().toString());
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
        for (String name : extractors.keySet()) {
            config.extractors().put(name, new AnnotationConfig.ExtractorDef(name));
        }

        List<Map<String, Object>> rules = (List<Map<String, Object>>) data.getOrDefault("rules", List.of());
        for (Map<String, Object> rawRule : rules) {
            Map<String, Object> match = RuleYamlSupport.map(rawRule.get("match"));
            String scope = RuleYamlSupport.string(rawRule.get("scope"), "token");

            boolean onGloss = false;
            String glossSpecial = null;
            Pattern regex = null;
            Set<String> inList = new LinkedHashSet<>();

            if (match.containsKey("gloss")) {
                onGloss = true;
                Object glossObj = match.get("gloss");
                if (glossObj instanceof String gs && "spanish_verbs".equalsIgnoreCase(gs)) {
                    glossSpecial = gs;
                } else {
                    Map<String, Object> glossMapMatch = RuleYamlSupport.map(glossObj);
                    if (!glossMapMatch.isEmpty()) {
                        inList.addAll(normalizeStringList(glossMapMatch.get("in_list")));
                        String regexText = RuleYamlSupport.string(glossMapMatch.get("regex"), "");
                        if (!regexText.isBlank()) {
                            regex = Pattern.compile(regexText);
                        }
                        if (inList.contains("spanish_verbs")) {
                            inList.remove("spanish_verbs");
                            glossSpecial = "spanish_verbs";
                        }
                    } else {
                        inList.addAll(normalizeStringList(glossObj));
                    }
                }
            } else {
                inList.addAll(normalizeStringList(match.get("in_list")));
                String regexText = RuleYamlSupport.string(match.get("regex"), "");
                if (!regexText.isBlank()) {
                    regex = Pattern.compile(regexText);
                }
            }

            Map<String, Object> set = RuleYamlSupport.map(rawRule.get("set"));
            List<Map<String, String>> extractsList = new ArrayList<>();
            Object extractObj = set.get("extract");
            if (extractObj instanceof List<?> l) {
                for (Object o : l) {
                    extractsList.add(castStringMap(RuleYamlSupport.map(o)));
                }
            }

            config.rules().add(new AnnotationRule(
                    RuleYamlSupport.string(rawRule.get("name"), ""),
                    scope,
                    regex,
                    inList,
                    onGloss,
                    glossSpecial,
                    RuleYamlSupport.string(set.get("upos"), ""),
                    castStringMap(RuleYamlSupport.map(set.get("feats"))),
                    extractsList
            ));
        }

        return config;
    }

    private static Set<String> normalizeStringList(Object raw) {
        Set<String> out = new LinkedHashSet<>();
        if (raw == null) {
            return out;
        }
        if (raw instanceof String s) {
            out.add(s);
            return out;
        }
        out.addAll(RuleYamlSupport.stringList(raw));
        return out;
    }

    private Map<String, String> castStringMap(Map<String, Object> raw) {
        Map<String, String> out = new LinkedHashMap<>();
        for (var e : raw.entrySet()) {
            if (e.getValue() != null) {
                out.put(e.getKey(), e.getValue().toString());
            }
        }
        return out;
    }
}

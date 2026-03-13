package org.titiplex.io;

import org.titiplex.rules.CorrectionRule;
import org.titiplex.rules.DeleteCharsRule;
import org.titiplex.rules.GlossReplaceRule;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class YamlRuleLoader {
    @SuppressWarnings("unchecked")
    public List<CorrectionRule> load(InputStream inputStream) {
        Yaml yaml = new Yaml();
        Map<String, Object> data = yaml.load(inputStream);
        List<Map<String, Object>> rawRules = (List<Map<String, Object>>) data.getOrDefault("rules", List.of());

        List<CorrectionRule> rules = new ArrayList<>();
        for (Map<String, Object> rawRule : rawRules) {
            String id = Objects.toString(rawRule.getOrDefault("id", rawRule.getOrDefault("name", "unnamed_rule")));
            Map<String, Object> rewrite = (Map<String, Object>) rawRule.get("rewrite");
            if (rewrite == null) {
                continue;
            }

            Map<String, Object> delete = (Map<String, Object>) rewrite.get("delete");
            if (delete != null && "chars".equals(delete.get("type"))) {
                List<String> chars = (List<String>) delete.getOrDefault("chars", List.of());
                rules.add(new DeleteCharsRule(id, chars));
                continue;
            }

            Map<String, Object> replace = (Map<String, Object>) rewrite.get("replace_gloss");
            if (replace != null) {
                String regex = Objects.toString(replace.getOrDefault("regex", ""));
                String replacement = Objects.toString(replace.getOrDefault("replacement", ""));
                boolean ignoreCase = Boolean.TRUE.equals(replace.get("ignore_case"));
                rules.add(new GlossReplaceRule(id, regex, replacement, ignoreCase));
            }
        }
        return rules;
    }
}

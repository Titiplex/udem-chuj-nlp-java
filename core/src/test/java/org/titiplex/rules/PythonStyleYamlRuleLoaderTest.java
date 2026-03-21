package org.titiplex.rules;

import org.junit.jupiter.api.Test;
import org.titiplex.model.AlignedToken;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("SequencedCollectionMethodCanBeUsed")
class PythonStyleYamlRuleLoaderTest {

    @Test
    void loadsDeleteAndRewriteRulesAndAppliesThem() {
        String yaml = """
                rules:
                  - id: strip_apostrophe
                    rewrite:
                      delete:
                        type: chars
                        chars: ["'"]
                  - id: normalize_surface
                    rewrite:
                      before: [ixim-na]
                      after: [ixim]
                  - id: normalize_gloss
                    rewrite:
                      gloss:
                        before: [N, n]
                        after: [NOUN]
                """;

        PythonStyleYamlRuleLoader loader = new PythonStyleYamlRuleLoader();
        RuleEngine engine = new RuleEngine(loader.load(
                new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8))
        ));

        List<AlignedToken> input = List.of(
                AlignedToken.of("ixim-na'", "N", List.of("ixim", "na'"), List.of("N"))
        );

        List<AlignedToken> out = engine.apply(input);
        assertEquals(1, out.size());
        assertEquals("ixim", out.get(0).chujSurface());
        assertEquals("NOUN", out.get(0).glossSurface());
    }

    @Test
    void loadsRegexSubRule() {
        String yaml = """
                rules:
                  - id: regex_fix
                    rewrite:
                      regex_sub:
                        scope: chuj
                        pattern: "x"
                        repl: "xh"
                """;

        PythonStyleYamlRuleLoader loader = new PythonStyleYamlRuleLoader();
        RuleEngine engine = new RuleEngine(loader.load(
                new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8))
        ));

        List<AlignedToken> input = List.of(
                AlignedToken.of("xol", "NOUN", List.of("xol"), List.of("NOUN"))
        );

        List<AlignedToken> out = engine.apply(input);
        assertEquals("xhol", out.get(0).chujSurface());
    }
}

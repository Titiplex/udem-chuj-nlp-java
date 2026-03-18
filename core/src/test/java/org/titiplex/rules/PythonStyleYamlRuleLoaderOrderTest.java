package org.titiplex.rules;

import org.junit.jupiter.api.Test;
import org.titiplex.model.AlignedToken;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PythonStyleYamlRuleLoaderOrderTest {

    @Test
    void honorsTopLevelMergeBeforeRewriteWhenYamlDeclaresItThatWay() {
        String yaml = """
                rules:
                  - id: tik_tik
                    merge:
                      match:
                        tokens:
                          - ["tik", "tik"]
                    rewrite:
                      match:
                        tokens:
                          isword: ["tik-tik"]
                      before: ["tik-tik"]
                      after: ["tiktik"]
                      gloss:
                        before: ["PROX-PROX"]
                        after: ["INTJ"]
                """;

        List<CorrectionRule> rules = new PythonStyleYamlRuleLoader()
                .load(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));
        RuleEngine engine = new RuleEngine(rules);
        List<AlignedToken> out = engine.apply(List.of(
                AlignedToken.of("tik", "PROX", List.of("tik"), List.of("PROX")),
                AlignedToken.of("tik", "PROX", List.of("tik"), List.of("PROX"))
        ));

        assertEquals(1, out.size());
        assertEquals("tiktik", out.getFirst().chujSurface());
        assertEquals("INTJ", out.getFirst().glossSurface());
    }
}

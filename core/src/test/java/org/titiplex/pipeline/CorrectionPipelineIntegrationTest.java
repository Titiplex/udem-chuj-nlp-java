package org.titiplex.pipeline;

import org.junit.jupiter.api.Test;
import org.titiplex.align.TokenAligner;
import org.titiplex.model.CorrectedBlock;
import org.titiplex.model.RawBlock;
import org.titiplex.rules.PythonStyleYamlRuleLoader;
import org.titiplex.rules.RuleEngine;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CorrectionPipelineIntegrationTest {

    @Test
    void processBuildsCorrectedBlockFromRawBlock() {
        String yaml = """
                rules:
                  - id: normalize_gloss
                    rewrite:
                      gloss:
                        before: [vi]
                        after: [VERB]
                """;

        RuleEngine engine = new RuleEngine(new PythonStyleYamlRuleLoader().load(
                new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8))
        ));

        CorrectionPipeline pipeline = new CorrectionPipeline(new TokenAligner(), engine);
        RawBlock raw = new RawBlock(7, "7 ix naq", "A1 vi", "I go");

        CorrectedBlock corrected = pipeline.process(raw);

        assertEquals(7, corrected.id());
        assertEquals("ix naq", corrected.chujText());
        assertEquals("A1 VERB", corrected.glossText());
        assertEquals("I go", corrected.translation());
        assertEquals(2, corrected.alignedTokens().size());
    }
}

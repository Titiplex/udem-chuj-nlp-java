package org.titiplex.conllu;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnnotationConfigLoaderTest {

    @Test
    void loadsGlossMapExtractorAndRules() {
        String yaml = """
                gloss_map:
                  pos:
                    - verb: VERB
                    - noun: NOUN
                  feats:
                    - pl: [Number, Plur]
                    - pfv:
                        Aspect: Perf
                extractors:
                  agreement_verbs: {}
                rules:
                  - name: mark_spanish_verbs
                    scope: token
                    match:
                      gloss: spanish_verbs
                    set:
                      upos: VERB
                      feats:
                        Foreign: Yes
                      extract:
                        - name: agreement_verbs
                """;

        AnnotationConfig config = new AnnotationConfigLoader().load(
                new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8))
        );

        assertEquals("VERB", config.glossMapper().resolvePos("verb"));
        assertEquals(Map.of("Number", "Plur"), config.glossMapper().resolveFeats("pl"));
        assertTrue(config.extractors().containsKey("agreement_verbs"));
        assertEquals(1, config.rules().size());
    }
}

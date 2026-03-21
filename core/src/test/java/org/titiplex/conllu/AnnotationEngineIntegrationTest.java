package org.titiplex.conllu;

import org.junit.jupiter.api.Test;
import org.titiplex.model.AlignedToken;
import org.titiplex.model.CorrectedBlock;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

final class AnnotationEngineIntegrationTest {

    private AnnotationConfig loadConfig() throws Exception {
        AnnotationConfigLoader loader = new AnnotationConfigLoader();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("annotation/test-annotation.yaml")) {
            assertNotNull(in);
            Path baseDir = Path.of(Objects.requireNonNull(getClass().getClassLoader().getResource("annotation")).toURI());
            return loader.load(in, baseDir);
        }
    }

    @Test
    void annotationUsesLexiconAndGlossMap() throws Exception {
        AnnotationConfig config = loadConfig();
        AnnotationEngine engine = new AnnotationEngine(config);

        CorrectedBlock block = new CorrectedBlock(
                1,
                "form",
                "ganar",
                "translation",
                List.of(
                        AlignedToken.of("to", "PREP", List.of("to"), List.of("PREP")),
                        AlignedToken.of("ganar", "ganar", List.of("ganar"), List.of("ganar"))
                )
        );

        ConlluEntry entry = engine.annotate(block);

        assertEquals(2, entry.lines().size());
        assertEquals("ADP", entry.lines().get(0).upos());
        assertEquals("VERB", entry.lines().get(1).upos());
    }
}

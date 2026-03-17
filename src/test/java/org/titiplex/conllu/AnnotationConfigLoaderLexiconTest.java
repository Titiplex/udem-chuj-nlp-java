package org.titiplex.conllu;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

final class AnnotationConfigLoaderLexiconTest {

    @Test
    void loadsExternalLexiconsFromYaml() throws Exception {
        AnnotationConfigLoader loader = new AnnotationConfigLoader();

        try (InputStream in = getClass().getClassLoader().getResourceAsStream("annotation/test-annotation.yaml")) {
            assertNotNull(in);

            Path baseDir = Path.of(Objects.requireNonNull(getClass().getClassLoader().getResource("annotation")).toURI());
            AnnotationConfig config = loader.load(in, baseDir);

            assertTrue(config.lexiconRegistry().contains("spanish_verbs", "ganar"));
            assertTrue(config.lexiconRegistry().contains("spanish_verbs", "pasar"));
            assertTrue(config.lexiconRegistry().contains("spanish_words", "malo"));
            assertTrue(config.lexiconRegistry().contains("spanish_words", "todavia"));
            assertFalse(config.lexiconRegistry().contains("spanish_verbs", "malo"));
        }
    }
}

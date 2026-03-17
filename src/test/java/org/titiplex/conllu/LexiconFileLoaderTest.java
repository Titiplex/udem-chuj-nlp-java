package org.titiplex.conllu;

import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

final class LexiconFileLoaderTest {

    private Path resourcePath(String resource) throws URISyntaxException {
        return Path.of(Objects.requireNonNull(getClass().getClassLoader().getResource(resource)).toURI());
    }

    @Test
    void loadCsvLexicon() throws Exception {
        Set<String> values = LexiconFileLoader.load(resourcePath("lexicons/test_verbs.csv"));

        assertTrue(values.contains("ganar"));
        assertTrue(values.contains("pasar"));
        assertTrue(values.contains("venir"));
        assertFalse(values.contains("lemma"));
    }

    @Test
    void loadJsonLexicon() throws Exception {
        Set<String> values = LexiconFileLoader.load(resourcePath("lexicons/test_words.json"));

        assertTrue(values.contains("malo"));
        assertTrue(values.contains("hasta"));
        assertTrue(values.contains("ganar"));
    }
}

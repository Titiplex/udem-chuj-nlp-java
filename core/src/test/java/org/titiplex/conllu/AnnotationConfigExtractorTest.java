package org.titiplex.conllu;

import org.junit.jupiter.api.Test;
import org.titiplex.model.AlignedToken;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

final class AnnotationConfigExtractorTest {

    private AnnotationConfig loadConfig() throws Exception {
        AnnotationConfigLoader loader = new AnnotationConfigLoader();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("annotation/test-annotation.yaml")) {
            assertNotNull(in);
            Path baseDir = Path.of(getClass().getClassLoader().getResource("annotation").toURI());
            return loader.load(in, baseDir);
        }
    }

    @Test
    void extractorParsesAgreementTagsGenerically() throws Exception {
        AnnotationConfig config = loadConfig();

        AlignedToken token = AlignedToken.of(
                "ix-onh-bat",
                "PFV-B1PL-ir",
                List.of("ix", "onh", "bat"),
                List.of("PFV", "B1PL", "ir")
        );

        ConlluLine line = ConlluLine.basic("1", "ix-onh-bat");
        Map<String, Object> ctx = new LinkedHashMap<>();

        config.applyExtractor("agreement_verbs", token, line, ctx);

        @SuppressWarnings("unchecked")
        Map<String, Object> ab = (Map<String, Object>) ctx.get("agreement_verbs");
        assertNotNull(ab);
        assertTrue(ab.containsKey("B"));

        @SuppressWarnings("unchecked")
        Map<String, String> b = (Map<String, String>) ab.get("B");
        assertEquals("1", b.get("person"));
        assertEquals("Plur", b.get("number"));
    }
}

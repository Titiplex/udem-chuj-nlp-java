package org.titiplex.app.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class AnnotationConfigStateServiceTest {

    private final AnnotationConfigStateService service = new AnnotationConfigStateService();

    @Test
    void loadStoresPathAndParsedConfig(@TempDir Path tempDir) throws Exception {
        Path yaml = tempDir.resolve("annotation.yaml");
        Files.writeString(yaml, """
                def:
                  pos: [ "VERB" ]
                  feats: [ "Person" ]
                rules:
                  - id: test.rule
                    when:
                      gloss: [ "A1" ]
                    set:
                      upos: VERB
                """);

        service.load(yaml);

        assertThat(service.getCurrentPath()).isEqualTo(yaml);
        assertThat(service.getCurrentConfig()).isNotNull();
    }

    @Test
    void resetClearsState(@TempDir Path tempDir) throws Exception {
        Path yaml = tempDir.resolve("annotation.yaml");
        Files.writeString(yaml, "rules: []\n");

        service.load(yaml);
        service.reset();

        assertThat(service.getCurrentPath()).isNull();
        assertThat(service.getCurrentConfig()).isNotNull();
        assertThat(service.getCurrentConfig().rules()).isEmpty();
    }
}

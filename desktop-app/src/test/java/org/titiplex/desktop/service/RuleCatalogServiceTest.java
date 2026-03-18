package org.titiplex.desktop.service;

import org.junit.jupiter.api.Test;
import org.titiplex.desktop.db.DatabaseManager;
import org.titiplex.desktop.db.RuleRepository;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleCatalogServiceTest {

    @Test
    void importsAndValidatesRules() throws Exception {
        Path tempDir = Files.createTempDirectory("rule-studio-test");
        DatabaseManager db = new DatabaseManager(tempDir.resolve("rules-db"));
        db.init();

        RuleCatalogService service = new RuleCatalogService(new RuleRepository(db));
        int count = service.importYaml(Path.of("/core/src/test/resources/sample-correction.yaml"));

        assertTrue(count > 0);
        assertEquals(count, service.listRules().size());
        assertTrue(service.validateAll().messages().getFirst().startsWith("Validation successful"));
    }
}
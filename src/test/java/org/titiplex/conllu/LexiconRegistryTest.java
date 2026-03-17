package org.titiplex.conllu;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

final class LexiconRegistryTest {

    @Test
    void registryNormalizesCase() {
        LexiconRegistry registry = new LexiconRegistry();
        registry.put("verbs", Set.of("Ganar", "Pasar"));

        assertTrue(registry.contains("verbs", "ganar"));
        assertTrue(registry.contains("verbs", "GANAR"));
        assertTrue(registry.contains("verbs", "pasar"));
        assertFalse(registry.contains("verbs", "venir"));
    }
}

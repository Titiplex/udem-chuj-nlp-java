package org.titiplex.app.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.titiplex.app.persistence.entity.CorrectedEntry;
import org.titiplex.app.persistence.entity.RawEntry;
import org.titiplex.app.persistence.repository.CorrectedEntryRepository;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CorrectedEntryServiceTest {

    @Mock
    private CorrectedEntryRepository repository;

    @Mock
    private RulesetFingerprintService rulesetFingerprintService;

    @InjectMocks
    private CorrectedEntryService service;

    @Test
    void saveRejectsSecondCorrectedEntryForSameRawEntry() {
        RawEntry rawEntry = new RawEntry();
        rawEntry.setId(11L);

        CorrectedEntry incoming = new CorrectedEntry();
        incoming.setRawEntry(rawEntry);

        CorrectedEntry existing = new CorrectedEntry();
        existing.setId(99L);
        existing.setRawEntry(rawEntry);

        when(repository.findByRawEntryId(11L)).thenReturn(Optional.of(existing));

        assertThrows(IllegalStateException.class, () -> service.save(incoming));
    }

    @Test
    void saveAllowsUpdatingAlreadyLinkedCorrectedEntry() {
        RawEntry rawEntry = new RawEntry();
        rawEntry.setId(11L);

        CorrectedEntry incoming = new CorrectedEntry();
        incoming.setId(99L);
        incoming.setRawEntry(rawEntry);

        CorrectedEntry existing = new CorrectedEntry();
        existing.setId(99L);
        existing.setRawEntry(rawEntry);

        when(repository.findByRawEntryId(11L)).thenReturn(Optional.of(existing));
        when(repository.saveAndFlush(incoming)).thenReturn(incoming);

        CorrectedEntry saved = assertDoesNotThrow(() -> service.save(incoming));

        assertEquals(99L, saved.getId());
        assertNotEquals(Boolean.TRUE, saved.getIsCorrect());
    }

    @Test
    void saveApprovedEntryCapturesCurrentRulesetFingerprint() {
        RawEntry rawEntry = new RawEntry();
        rawEntry.setId(11L);
        rawEntry.setUpdatedAt(Instant.parse("2026-03-26T12:00:00Z"));

        CorrectedEntry incoming = new CorrectedEntry();
        incoming.setId(99L);
        incoming.setRawEntry(rawEntry);
        incoming.setIsCorrect(true);

        when(repository.findByRawEntryId(11L)).thenReturn(Optional.of(incoming));
        when(rulesetFingerprintService.currentCorrectionRulesetFingerprint()).thenReturn("fingerprint-1");
        when(repository.saveAndFlush(incoming)).thenReturn(incoming);

        CorrectedEntry saved = assertDoesNotThrow(() -> service.save(incoming));

        assertEquals("fingerprint-1", saved.getApprovedRulesFingerprint());
        assertFalse(saved.isStale());
        assertEquals(rawEntry.getUpdatedAt(), saved.getApprovedRawUpdatedAt());
    }
}
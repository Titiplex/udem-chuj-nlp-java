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
        assertFalse(saved.isStale());
    }

    @Test
    void saveApprovedEntryStoresApprovalSnapshotAndClearsStale() {
        Instant rawUpdatedAt = Instant.parse("2026-03-25T12:00:00Z");
        RawEntry rawEntry = new RawEntry();
        rawEntry.setId(11L);
        rawEntry.setUpdatedAt(rawUpdatedAt);

        CorrectedEntry incoming = new CorrectedEntry();
        incoming.setId(99L);
        incoming.setRawEntry(rawEntry);
        incoming.setIsCorrect(true);
        incoming.setStale(true);

        when(repository.findByRawEntryId(11L)).thenReturn(Optional.of(incoming));
        when(repository.saveAndFlush(incoming)).thenReturn(incoming);

        CorrectedEntry saved = service.save(incoming);

        assertTrue(saved.getIsCorrect());
        assertFalse(saved.isStale());
        assertEquals(rawUpdatedAt, saved.getApprovedRawUpdatedAt());
    }

    @Test
    void saveDraftClearsApprovalSnapshotAndStaleFlag() {
        RawEntry rawEntry = new RawEntry();
        rawEntry.setId(11L);

        CorrectedEntry incoming = new CorrectedEntry();
        incoming.setId(99L);
        incoming.setRawEntry(rawEntry);
        incoming.setIsCorrect(false);
        incoming.setStale(true);
        incoming.setApprovedRawUpdatedAt(Instant.parse("2026-03-25T12:00:00Z"));

        when(repository.findByRawEntryId(11L)).thenReturn(Optional.of(incoming));
        when(repository.saveAndFlush(incoming)).thenReturn(incoming);

        CorrectedEntry saved = service.save(incoming);

        assertFalse(saved.getIsCorrect());
        assertFalse(saved.isStale());
        assertNull(saved.getApprovedRawUpdatedAt());
    }
}
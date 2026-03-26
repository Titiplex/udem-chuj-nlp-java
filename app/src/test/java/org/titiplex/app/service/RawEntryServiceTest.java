package org.titiplex.app.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.titiplex.app.persistence.entity.CorrectedEntry;
import org.titiplex.app.persistence.entity.RawEntry;
import org.titiplex.app.persistence.repository.CorrectedEntryRepository;
import org.titiplex.app.persistence.repository.RawEntryRepository;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RawEntryServiceTest {

    @Mock
    private RawEntryRepository rawEntryRepository;

    @Mock
    private CorrectedEntryRepository correctedEntryRepository;

    @InjectMocks
    private RawEntryService rawEntryService;

    @Test
    void deleteRejectsRawEntryWhenCorrectedEntryIsStillLinked() {
        CorrectedEntry correctedEntry = new CorrectedEntry();
        correctedEntry.setId(42L);

        when(correctedEntryRepository.findByRawEntryId(7L)).thenReturn(Optional.of(correctedEntry));

        assertThrows(IllegalStateException.class, () -> rawEntryService.delete(7L));

        verify(rawEntryRepository, never()).deleteById(anyLong());
    }

    @Test
    void saveMarksLinkedApprovedCorrectionAsStaleWhenRawMovedAfterApproval() {
        Instant approvedAt = Instant.parse("2026-03-25T12:00:00Z");
        Instant rawUpdatedAt = Instant.parse("2026-03-26T12:00:00Z");

        RawEntry rawEntry = new RawEntry();
        rawEntry.setId(7L);
        rawEntry.setUpdatedAt(rawUpdatedAt);

        CorrectedEntry correctedEntry = new CorrectedEntry();
        correctedEntry.setId(42L);
        correctedEntry.setIsCorrect(true);
        correctedEntry.setApprovedRawUpdatedAt(approvedAt);
        correctedEntry.setStale(false);

        when(rawEntryRepository.saveAndFlush(rawEntry)).thenReturn(rawEntry);
        when(correctedEntryRepository.findByRawEntryId(7L)).thenReturn(Optional.of(correctedEntry));
        when(correctedEntryRepository.save(correctedEntry)).thenReturn(correctedEntry);

        rawEntryService.save(rawEntry);

        verify(correctedEntryRepository).save(correctedEntry);
    }
}
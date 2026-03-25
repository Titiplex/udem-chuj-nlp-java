package org.titiplex.app.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.titiplex.app.persistence.entity.CorrectedEntry;
import org.titiplex.app.persistence.repository.CorrectedEntryRepository;
import org.titiplex.app.persistence.repository.RawEntryRepository;

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
}
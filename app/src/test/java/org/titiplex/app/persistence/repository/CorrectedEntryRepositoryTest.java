package org.titiplex.app.persistence.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.titiplex.app.persistence.entity.CorrectedEntry;
import org.titiplex.app.persistence.entity.RawEntry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class CorrectedEntryRepositoryTest {

    @Autowired
    private CorrectedEntryRepository correctedEntryRepository;

    @Autowired
    private RawEntryRepository rawEntryRepository;

    @Test
    void findByRawEntryIdReturnsLinkedCorrectedEntry() {
        RawEntry raw = new RawEntry();
        raw.setRawText("ha");
        raw.setGlossText("A1");
        raw.setTranslationText("one");
        raw = rawEntryRepository.saveAndFlush(raw);

        CorrectedEntry corrected = new CorrectedEntry();
        corrected.setRawEntry(raw);
        corrected.setRawText("ha");
        corrected.setGlossText("A1");
        corrected.setTranslationText("one");
        corrected.setIsCorrect(false);
        correctedEntryRepository.saveAndFlush(corrected);

        assertThat(correctedEntryRepository.findByRawEntryId(raw.getId()))
                .isPresent()
                .get()
                .extracting(CorrectedEntry::getRawEntry)
                .extracting(RawEntry::getId)
                .isEqualTo(raw.getId());
    }

    @Test
    void duplicateCorrectedEntryForSameRawEntryIsRejected() {
        RawEntry raw = new RawEntry();
        raw.setRawText("ha");
        raw.setGlossText("A1");
        raw.setTranslationText("one");
        raw = rawEntryRepository.saveAndFlush(raw);

        CorrectedEntry first = new CorrectedEntry();
        first.setRawEntry(raw);
        first.setRawText("ha");
        first.setGlossText("A1");
        first.setTranslationText("one");
        correctedEntryRepository.saveAndFlush(first);

        CorrectedEntry duplicate = new CorrectedEntry();
        duplicate.setRawEntry(raw);
        duplicate.setRawText("ha");
        duplicate.setGlossText("A1");
        duplicate.setTranslationText("one");

        assertThatThrownBy(() -> correctedEntryRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}

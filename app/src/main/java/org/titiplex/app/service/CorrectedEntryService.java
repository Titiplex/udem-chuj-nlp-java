package org.titiplex.app.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.titiplex.app.persistence.entity.CorrectedEntry;
import org.titiplex.app.persistence.entity.RawEntry;
import org.titiplex.app.persistence.repository.CorrectedEntryRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CorrectedEntryService {
    private final CorrectedEntryRepository repository;

    public CorrectedEntryService(CorrectedEntryRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<CorrectedEntry> getAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<CorrectedEntry> findById(Long id) {
        return repository.findById(id);
    }

    public CorrectedEntry save(CorrectedEntry entry) {
        validateRawEntryLinkUniqueness(entry);
        if (entry.getIsCorrect() == null) {
            entry.setIsCorrect(false);
        }
        return repository.save(entry);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void validateRawEntryLinkUniqueness(CorrectedEntry entry) {
        RawEntry rawEntry = entry.getRawEntry();
        if (rawEntry == null || rawEntry.getId() == null) {
            return;
        }

        CorrectedEntry existing = repository.findByRawEntryId(rawEntry.getId()).orElse(null);
        if (existing == null) {
            return;
        }

        Long currentId = entry.getId();
        if (currentId == null || !currentId.equals(existing.getId())) {
            throw new IllegalStateException(
                    "Raw entry #" + rawEntry.getId() + " is already linked to corrected entry #" + existing.getId() + "."
            );
        }
    }
}

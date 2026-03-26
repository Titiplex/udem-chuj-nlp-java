package org.titiplex.app.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.titiplex.app.persistence.entity.CorrectedEntry;
import org.titiplex.app.persistence.entity.RawEntry;
import org.titiplex.app.persistence.repository.CorrectedEntryRepository;
import org.titiplex.app.persistence.repository.RawEntryRepository;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class RawEntryService {
    private final RawEntryRepository repository;
    private final CorrectedEntryRepository correctedEntryRepository;

    public RawEntryService(
            RawEntryRepository repository,
            CorrectedEntryRepository correctedEntryRepository
    ) {
        this.repository = repository;
        this.correctedEntryRepository = correctedEntryRepository;
    }

    @Transactional(readOnly = true)
    public List<RawEntry> getAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public RawEntry getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public RawEntry save(RawEntry entry) {
        RawEntry saved = repository.saveAndFlush(entry);
        markLinkedApprovedCorrectionAsStaleIfNeeded(saved);
        return saved;
    }

    public void delete(Long id) {
        CorrectedEntry linked = correctedEntryRepository.findByRawEntryId(id).orElse(null);
        if (linked != null) {
            throw new IllegalStateException(
                    "Cannot delete raw entry #" + id + " because corrected entry #" + linked.getId() + " is still linked to it."
            );
        }
        repository.deleteById(id);
    }

    private void markLinkedApprovedCorrectionAsStaleIfNeeded(RawEntry saved) {
        if (saved.getId() == null) {
            return;
        }

        CorrectedEntry linked = correctedEntryRepository.findByRawEntryId(saved.getId()).orElse(null);
        if (linked == null || !Boolean.TRUE.equals(linked.getIsCorrect())) {
            return;
        }

        if (!hasRawMovedSinceApproval(saved.getUpdatedAt(), linked.getApprovedRawUpdatedAt())
                || linked.isStaleDueToRaw()) {
            return;
        }

        linked.setStale(true);
        linked.setStaleDueToRaw(true);
        correctedEntryRepository.save(linked);
    }

    private static boolean hasRawMovedSinceApproval(Instant rawUpdatedAt, Instant approvedRawUpdatedAt) {
        if (approvedRawUpdatedAt == null) {
            return true;
        }
        return rawUpdatedAt != null && rawUpdatedAt.isAfter(approvedRawUpdatedAt);
    }
}
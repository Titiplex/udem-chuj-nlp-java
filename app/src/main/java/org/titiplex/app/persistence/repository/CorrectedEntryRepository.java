package org.titiplex.app.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.titiplex.app.persistence.entity.CorrectedEntry;

import java.util.Optional;

public interface CorrectedEntryRepository extends JpaRepository<CorrectedEntry, Long> {
    Optional<CorrectedEntry> findByRawEntryId(Long rawEntryId);
}
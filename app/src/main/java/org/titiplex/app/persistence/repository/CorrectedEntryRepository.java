package org.titiplex.app.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.titiplex.app.persistence.entity.CorrectedEntry;

public interface CorrectedEntryRepository extends JpaRepository<CorrectedEntry, Long> {
}

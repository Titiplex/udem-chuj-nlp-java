package org.titiplex.app.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.titiplex.app.persistence.entity.RawEntry;

public interface RawEntryRepository extends JpaRepository<RawEntry, Long> {
}

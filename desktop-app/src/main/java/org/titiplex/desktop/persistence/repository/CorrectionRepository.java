package org.titiplex.desktop.persistence.repository;

import org.titiplex.desktop.domain.correction.Correction;

import java.util.List;
import java.util.Optional;

public interface CorrectionRepository {
    List<Correction> findAll();

    List<Correction> findByExampleId(Long exampleId);

    Optional<Correction> findById(Long id);

    Correction save(Correction correction);

    void deleteById(Long id);
}

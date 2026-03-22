package org.titiplex.app.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.titiplex.app.persistence.entity.CorrectedEntry;
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
        return repository.save(entry);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
package org.titiplex.app.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.titiplex.app.persistence.entity.RawEntry;
import org.titiplex.app.persistence.repository.RawEntryRepository;

import java.util.List;

@Service
@Transactional
public class RawEntryService {
    private final RawEntryRepository repository;

    public RawEntryService(RawEntryRepository repository) {
        this.repository = repository;
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
        return repository.save(entry);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
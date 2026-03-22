package org.titiplex.app.service;

import org.springframework.stereotype.Service;
import org.titiplex.app.persistence.entity.RawEntry;
import org.titiplex.app.persistence.repository.RawEntryRepository;

import java.util.List;

@Service
public class RawEntryService {
    private final RawEntryRepository repository;
    public RawEntryService(RawEntryRepository repository) {
        this.repository = repository;
    }
    public List<RawEntry> getAll() {
        return repository.findAll();
    }

    public RawEntry getById(Long id) {
        return repository.findById(id).orElse(null);
    }
}

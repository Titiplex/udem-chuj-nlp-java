package org.titiplex.app.service;

import org.springframework.stereotype.Service;
import org.titiplex.app.persistence.entity.CorrectedEntry;
import org.titiplex.app.persistence.repository.CorrectedEntryRepository;

import java.util.List;

@Service
public class CorrectedEntryService {
    private final CorrectedEntryRepository repository;
    public CorrectedEntryService(CorrectedEntryRepository repository) {
        this.repository = repository;
    }
    public List<CorrectedEntry> getAll() {
        return repository.findAll();
    }
}

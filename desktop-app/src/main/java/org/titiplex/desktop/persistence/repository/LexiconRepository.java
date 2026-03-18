package org.titiplex.desktop.persistence.repository;

import org.titiplex.desktop.domain.lexicon.LexiconEntry;

import java.util.List;

public interface LexiconRepository {
    List<LexiconEntry> findAll();

    List<LexiconEntry> findByLexiconName(String lexiconName);

    LexiconEntry save(LexiconEntry entry);
}

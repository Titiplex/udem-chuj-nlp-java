package org.titiplex.desktop.persistence.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.titiplex.desktop.domain.lexicon.LexiconEntry;
import org.titiplex.desktop.persistence.entity.LexiconEntryEntity;
import org.titiplex.desktop.persistence.repository.LexiconRepository;

import java.util.List;

public final class JpaLexiconRepository implements LexiconRepository {
    private final EntityManagerFactory entityManagerFactory;

    public JpaLexiconRepository(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public List<LexiconEntry> findAll() {
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            return em.createQuery("select l from LexiconEntryEntity l order by l.lexiconName, l.entryKey", LexiconEntryEntity.class)
                    .getResultList()
                    .stream()
                    .map(this::toDomain)
                    .toList();
        }
    }

    @Override
    public List<LexiconEntry> findByLexiconName(String lexiconName) {
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            return em.createQuery("""
                            select l from LexiconEntryEntity l
                            where l.lexiconName = :lexiconName
                            order by l.entryKey
                            """, LexiconEntryEntity.class)
                    .setParameter("lexiconName", lexiconName)
                    .getResultList()
                    .stream()
                    .map(this::toDomain)
                    .toList();
        }
    }

    @Override
    public LexiconEntry save(LexiconEntry entry) {
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            em.getTransaction().begin();
            LexiconEntryEntity managed;
            if (entry.id() == null) {
                managed = toEntity(entry);
                em.persist(managed);
            } else {
                managed = em.find(LexiconEntryEntity.class, entry.id());
                if (managed == null) {
                    managed = toEntity(entry);
                    em.persist(managed);
                } else {
                    managed.setLexiconName(entry.lexiconName());
                    managed.setEntryKey(entry.entryKey());
                    managed.setEntryValue(entry.entryValue());
                    managed.setMetadataJson(entry.metadataJson());
                }
            }
            em.getTransaction().commit();
            return toDomain(managed);
        }
    }

    private LexiconEntry toDomain(LexiconEntryEntity entity) {
        return new LexiconEntry(
                entity.getId(),
                entity.getLexiconName(),
                entity.getEntryKey(),
                entity.getEntryValue(),
                entity.getMetadataJson()
        );
    }

    private LexiconEntryEntity toEntity(LexiconEntry entry) {
        LexiconEntryEntity entity = new LexiconEntryEntity();
        entity.setLexiconName(entry.lexiconName());
        entity.setEntryKey(entry.entryKey());
        entity.setEntryValue(entry.entryValue());
        entity.setMetadataJson(entry.metadataJson());
        return entity;
    }
}

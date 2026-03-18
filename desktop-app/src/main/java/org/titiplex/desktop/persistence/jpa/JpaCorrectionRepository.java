package org.titiplex.desktop.persistence.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.titiplex.desktop.domain.correction.Correction;
import org.titiplex.desktop.persistence.entity.CorrectionEntity;
import org.titiplex.desktop.persistence.entity.ExampleEntity;
import org.titiplex.desktop.persistence.entity.RuleEntity;
import org.titiplex.desktop.persistence.mapper.CorrectionMapper;
import org.titiplex.desktop.persistence.repository.CorrectionRepository;

import java.util.List;
import java.util.Optional;

public final class JpaCorrectionRepository implements CorrectionRepository {
    private final EntityManagerFactory entityManagerFactory;

    public JpaCorrectionRepository(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public List<Correction> findAll() {
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            return em.createQuery("select c from CorrectionEntity c order by c.id desc", CorrectionEntity.class)
                    .getResultList()
                    .stream()
                    .map(CorrectionMapper::toDomain)
                    .toList();
        }
    }

    @Override
    public List<Correction> findByExampleId(Long exampleId) {
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            return em.createQuery("""
                            select c from CorrectionEntity c
                            where c.example.id = :exampleId
                            order by c.id desc
                            """, CorrectionEntity.class)
                    .setParameter("exampleId", exampleId)
                    .getResultList()
                    .stream()
                    .map(CorrectionMapper::toDomain)
                    .toList();
        }
    }

    @Override
    public Optional<Correction> findById(Long id) {
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            CorrectionEntity entity = em.find(CorrectionEntity.class, id);
            return entity == null ? Optional.empty() : Optional.of(CorrectionMapper.toDomain(entity));
        }
    }

    @Override
    public Correction save(Correction correction) {
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            em.getTransaction().begin();

            ExampleEntity exampleEntity = em.getReference(ExampleEntity.class, correction.exampleId());
            RuleEntity ruleEntity = correction.ruleId() == null ? null : em.getReference(RuleEntity.class, correction.ruleId());

            CorrectionEntity managed;
            if (correction.id() == null) {
                managed = CorrectionMapper.toEntity(correction, exampleEntity, ruleEntity);
                em.persist(managed);
            } else {
                managed = em.find(CorrectionEntity.class, correction.id());
                if (managed == null) {
                    managed = CorrectionMapper.toEntity(correction, exampleEntity, ruleEntity);
                    em.persist(managed);
                } else {
                    managed.setExample(exampleEntity);
                    managed.setRule(ruleEntity);
                    managed.setBeforePayload(correction.beforePayload());
                    managed.setAfterPayload(correction.afterPayload());
                    managed.setDecision(correction.decision());
                    managed.setOrigin(correction.origin());
                    managed.setComment(correction.comment());
                }
            }

            em.getTransaction().commit();
            return CorrectionMapper.toDomain(managed);
        }
    }

    @Override
    public void deleteById(Long id) {
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            em.getTransaction().begin();
            CorrectionEntity entity = em.find(CorrectionEntity.class, id);
            if (entity != null) {
                em.remove(entity);
            }
            em.getTransaction().commit();
        }
    }
}

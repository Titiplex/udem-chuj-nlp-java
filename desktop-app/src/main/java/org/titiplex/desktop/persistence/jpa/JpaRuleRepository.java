package org.titiplex.desktop.persistence.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.titiplex.desktop.domain.rule.Rule;
import org.titiplex.desktop.domain.rule.RuleId;
import org.titiplex.desktop.persistence.entity.RuleEntity;
import org.titiplex.desktop.persistence.mapper.RuleMapper;
import org.titiplex.desktop.persistence.repository.RuleRepository;

import java.util.List;
import java.util.Optional;

public final class JpaRuleRepository implements RuleRepository {
    private final EntityManagerFactory entityManagerFactory;

    public JpaRuleRepository(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public List<Rule> findAll() {
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            return em.createQuery("select r from RuleEntity r order by r.stableId", RuleEntity.class)
                    .getResultList()
                    .stream()
                    .map(RuleMapper::toDomain)
                    .toList();
        }
    }

    @Override
    public Optional<Rule> findById(Long id) {
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            RuleEntity entity = em.find(RuleEntity.class, id);
            return entity == null ? Optional.empty() : Optional.of(RuleMapper.toDomain(entity));
        }
    }

    @Override
    public Optional<Rule> findByRuleId(RuleId ruleId) {
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            List<RuleEntity> result = em.createQuery(
                            "select r from RuleEntity r where r.stableId = :stableId", RuleEntity.class)
                    .setParameter("stableId", ruleId.value())
                    .setMaxResults(1)
                    .getResultList();
            return result.stream().findFirst().map(RuleMapper::toDomain);
        }
    }

    @Override
    public Rule save(Rule rule) {
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            em.getTransaction().begin();
            RuleEntity managed;
            if (rule.id() == null) {
                managed = RuleMapper.toEntity(rule);
                em.persist(managed);
            } else {
                managed = em.find(RuleEntity.class, rule.id());
                if (managed == null) {
                    managed = RuleMapper.toEntity(rule);
                    em.persist(managed);
                } else {
                    RuleMapper.copyIntoEntity(rule, managed);
                }
            }
            em.getTransaction().commit();
            return RuleMapper.toDomain(managed);
        }
    }

    @Override
    public void saveAll(List<Rule> rules) {
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            em.getTransaction().begin();
            for (Rule rule : rules) {
                if (rule.id() == null) {
                    em.persist(RuleMapper.toEntity(rule));
                } else {
                    RuleEntity managed = em.find(RuleEntity.class, rule.id());
                    if (managed == null) {
                        em.persist(RuleMapper.toEntity(rule));
                    } else {
                        RuleMapper.copyIntoEntity(rule, managed);
                    }
                }
            }
            em.getTransaction().commit();
        }
    }

    @Override
    public void deleteById(Long id) {
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            em.getTransaction().begin();
            RuleEntity entity = em.find(RuleEntity.class, id);
            if (entity != null) {
                em.remove(entity);
            }
            em.getTransaction().commit();
        }
    }
}

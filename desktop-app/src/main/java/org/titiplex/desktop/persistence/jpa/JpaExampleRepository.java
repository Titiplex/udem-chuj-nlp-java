package org.titiplex.desktop.persistence.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.titiplex.desktop.domain.example.Example;
import org.titiplex.desktop.persistence.entity.ExampleEntity;
import org.titiplex.desktop.persistence.mapper.ExampleMapper;
import org.titiplex.desktop.persistence.repository.ExampleRepository;

import java.util.List;
import java.util.Optional;

public final class JpaExampleRepository implements ExampleRepository {
    private final EntityManagerFactory entityManagerFactory;

    public JpaExampleRepository(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public List<Example> findAll() {
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            return em.createQuery("select e from ExampleEntity e order by e.id desc", ExampleEntity.class)
                    .getResultList()
                    .stream()
                    .map(ExampleMapper::toDomain)
                    .toList();
        }
    }

    @Override
    public List<Example> search(String term) {
        String pattern = "%" + (term == null ? "" : term.toLowerCase()) + "%";
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            return em.createQuery("""
                            select e
                            from ExampleEntity e
                            where lower(coalesce(e.surfaceText, '')) like :pattern
                               or lower(coalesce(e.glossText, '')) like :pattern
                               or lower(coalesce(e.translationText, '')) like :pattern
                            order by e.id desc
                            """, ExampleEntity.class)
                    .setParameter("pattern", pattern)
                    .getResultList()
                    .stream()
                    .map(ExampleMapper::toDomain)
                    .toList();
        }
    }

    @Override
    public Optional<Example> findById(Long id) {
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            ExampleEntity entity = em.find(ExampleEntity.class, id);
            return entity == null ? Optional.empty() : Optional.of(ExampleMapper.toDomain(entity));
        }
    }

    @Override
    public Example save(Example example) {
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            em.getTransaction().begin();
            ExampleEntity managed;
            if (example.id() == null) {
                managed = ExampleMapper.toEntity(example);
                em.persist(managed);
            } else {
                managed = em.find(ExampleEntity.class, example.id());
                if (managed == null) {
                    managed = ExampleMapper.toEntity(example);
                    em.persist(managed);
                } else {
                    ExampleMapper.copyIntoEntity(example, managed);
                }
            }
            em.getTransaction().commit();
            return ExampleMapper.toDomain(managed);
        }
    }

    @Override
    public void saveAll(List<Example> examples) {
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            em.getTransaction().begin();
            for (Example example : examples) {
                if (example.id() == null) {
                    em.persist(ExampleMapper.toEntity(example));
                } else {
                    ExampleEntity managed = em.find(ExampleEntity.class, example.id());
                    if (managed == null) {
                        em.persist(ExampleMapper.toEntity(example));
                    } else {
                        ExampleMapper.copyIntoEntity(example, managed);
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
            ExampleEntity entity = em.find(ExampleEntity.class, id);
            if (entity != null) {
                em.remove(entity);
            }
            em.getTransaction().commit();
        }
    }
}

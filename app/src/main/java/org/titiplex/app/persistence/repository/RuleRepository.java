package org.titiplex.app.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.titiplex.app.persistence.entity.Rule;
import org.titiplex.app.persistence.entity.RuleKind;

import java.util.List;
import java.util.Optional;

public interface RuleRepository extends JpaRepository<Rule, Long> {
    List<Rule> findAllByKindOrderByStableIdAsc(RuleKind kind);

    List<Rule> findAllByEnabledTrueAndKindOrderByStableIdAsc(RuleKind kind);

    Optional<Rule> findByStableId(String stableId);
}

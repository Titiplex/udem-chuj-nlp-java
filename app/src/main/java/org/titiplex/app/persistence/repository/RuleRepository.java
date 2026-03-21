package org.titiplex.app.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.titiplex.app.persistence.entity.Rule;

public interface RuleRepository extends JpaRepository<Rule, Long> {
}

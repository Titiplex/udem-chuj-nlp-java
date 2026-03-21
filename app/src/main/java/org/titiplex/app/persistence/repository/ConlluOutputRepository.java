package org.titiplex.app.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.titiplex.app.persistence.entity.ConlluOutput;

public interface ConlluOutputRepository extends JpaRepository<ConlluOutput, Long> {
}

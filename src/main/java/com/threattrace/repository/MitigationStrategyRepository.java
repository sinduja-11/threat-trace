package com.threattrace.repository;

import com.threattrace.domain.MitigationStrategy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MitigationStrategyRepository extends JpaRepository<MitigationStrategy, Long> {
    Optional<MitigationStrategy> findByMitigationCode(String mitigationCode);
}

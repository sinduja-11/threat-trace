package com.threattrace.repository;

import com.threattrace.domain.AttackPattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AttackPatternRepository extends JpaRepository<AttackPattern, Long> {
    Optional<AttackPattern> findByPatternCode(String patternCode);
}

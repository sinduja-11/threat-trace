package com.threattrace.repository;

import com.threattrace.domain.InvestigationCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvestigationCaseRepository extends JpaRepository<InvestigationCase, Long> {
    Optional<InvestigationCase> findByCaseNumber(String caseNumber);
    List<InvestigationCase> findByStatus(String status);
}

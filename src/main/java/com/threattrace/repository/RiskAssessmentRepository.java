package com.threattrace.repository;

import com.threattrace.domain.RiskAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, Long> {
    List<RiskAssessment> findByInvestigationCaseIdOrderByAssessmentTimeDesc(Long caseId);
}

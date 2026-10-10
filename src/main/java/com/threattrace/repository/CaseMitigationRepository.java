package com.threattrace.repository;

import com.threattrace.domain.CaseMitigation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaseMitigationRepository extends JpaRepository<CaseMitigation, Long> {
    List<CaseMitigation> findByInvestigationCaseIdOrderByRecommendationScoreDesc(Long caseId);
}

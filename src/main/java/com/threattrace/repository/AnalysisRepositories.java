package com.threattrace.repository;

import com.threattrace.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KillChainStageRepository extends JpaRepository<KillChainStage, Long> {
    Optional<KillChainStage> findByStageName(String stageName);
    List<KillChainStage> findAllByOrderByStageOrderAsc();
}

@Repository
public interface EventStageMappingRepository extends JpaRepository<EventStageMapping, Long> {
    List<EventStageMapping> findByEventLogInvestigationCaseId(Long caseId);
    List<EventStageMapping> findByEventLogId(Long eventId);
}

@Repository
public interface AttackPatternRepository extends JpaRepository<AttackPattern, Long> {
    Optional<AttackPattern> findByPatternCode(String patternCode);
}

@Repository
public interface ThreatActorProfileRepository extends JpaRepository<ThreatActorProfile, Long> {
    Optional<ThreatActorProfile> findByActorCodename(String actorCodename);
}

@Repository
public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, Long> {
    List<RiskAssessment> findByInvestigationCaseIdOrderByAssessmentTimeDesc(Long caseId);
}

@Repository
public interface MitigationStrategyRepository extends JpaRepository<MitigationStrategy, Long> {
    Optional<MitigationStrategy> findByMitigationCode(String mitigationCode);
}

@Repository
public interface CaseMitigationRepository extends JpaRepository<CaseMitigation, Long> {
    List<CaseMitigation> findByInvestigationCaseIdOrderByRecommendationScoreDesc(Long caseId);
}

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByInvestigationCaseId(Long caseId);
}

@Repository
public interface AnalystNoteRepository extends JpaRepository<AnalystNote, Long> {
    List<AnalystNote> findByInvestigationCaseIdOrderByCreatedAtDesc(Long caseId);
}

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findTop50ByOrderByTimestampDesc();
}

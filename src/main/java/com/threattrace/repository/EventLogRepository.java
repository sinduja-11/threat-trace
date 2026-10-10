package com.threattrace.repository;

import com.threattrace.domain.EventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventLogRepository extends JpaRepository<EventLog, Long> {
    List<EventLog> findByInvestigationCaseIdOrderByEventTimeAsc(Long caseId);

    Optional<EventLog> findByEventCode(String eventCode);

    List<EventLog> findByInvestigationCaseIdAndParentEventIsNull(Long caseId);

    List<EventLog> findByParentEventId(Long parentEventId);

    @Query("SELECT e FROM EventLog e WHERE e.investigationCase.id = :caseId " +
           "AND (:eventType IS NULL OR e.eventType = :eventType) " +
           "AND (:severity IS NULL OR e.severity = :severity) " +
           "ORDER BY e.eventTime ASC")
    List<EventLog> filterEvents(@Param("caseId") Long caseId,
                                @Param("eventType") String eventType,
                                @Param("severity") String severity);
}

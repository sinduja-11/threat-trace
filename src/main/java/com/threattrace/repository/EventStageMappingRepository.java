package com.threattrace.repository;

import com.threattrace.domain.EventStageMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventStageMappingRepository extends JpaRepository<EventStageMapping, Long> {
    List<EventStageMapping> findByEventLogInvestigationCaseId(Long caseId);
    List<EventStageMapping> findByEventLogId(Long eventId);
}

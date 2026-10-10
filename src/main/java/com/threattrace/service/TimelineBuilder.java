package com.threattrace.service;

import com.threattrace.domain.EventLog;
import com.threattrace.repository.EventLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TimelineBuilder {

    private final EventLogRepository eventLogRepository;

    public TimelineBuilder(EventLogRepository eventLogRepository) {
        this.eventLogRepository = eventLogRepository;
    }

    public List<EventLog> getTimeline(Long caseId, String eventType, String severity, String hostname, LocalDateTime startTime, LocalDateTime endTime) {
        List<EventLog> events = eventLogRepository.findByInvestigationCaseIdOrderByEventTimeAsc(caseId);

        return events.stream()
                .filter(e -> eventType == null || eventType.trim().isEmpty() || e.getEventType().equalsIgnoreCase(eventType))
                .filter(e -> severity == null || severity.trim().isEmpty() || e.getSeverity().equalsIgnoreCase(severity))
                .filter(e -> hostname == null || hostname.trim().isEmpty() || e.getDevice().getHostname().equalsIgnoreCase(hostname))
                .filter(e -> startTime == null || !e.getEventTime().isBefore(startTime))
                .filter(e -> endTime == null || !e.getEventTime().isAfter(endTime))
                .collect(Collectors.toList());
    }
}

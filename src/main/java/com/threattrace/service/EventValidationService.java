package com.threattrace.service;

import com.threattrace.domain.EventLog;
import com.threattrace.repository.EventLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class EventValidationService {

    private final EventLogRepository eventLogRepository;
    private static final List<DateTimeFormatter> FORMATTERS = List.of(
            DateTimeFormatter.ISO_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    );

    public EventValidationService(EventLogRepository eventLogRepository) {
        this.eventLogRepository = eventLogRepository;
    }

    public LocalDateTime parseTimestamp(String timestampStr) {
        if (timestampStr == null || timestampStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Event timestamp is required and missing.");
        }
        String cleanStr = timestampStr.trim();
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                return LocalDateTime.parse(cleanStr, formatter);
            } catch (DateTimeParseException ignored) {}
        }
        throw new IllegalArgumentException("Invalid date-time format: " + timestampStr + ". Supported formats: ISO-8601 or yyyy-MM-dd HH:mm:ss");
    }

    public List<String> validateRawRecord(Map<String, String> row, Set<String> processedCodesInBatch) {
        List<String> errors = new ArrayList<>();

        String eventCode = row.get("event_code");
        if (eventCode == null || eventCode.trim().isEmpty()) {
            errors.add("Missing required field 'event_code'");
        } else {
            if (processedCodesInBatch.contains(eventCode)) {
                errors.add("Duplicate event_code in current import batch: " + eventCode);
            }
            if (eventLogRepository.findByEventCode(eventCode).isPresent()) {
                errors.add("Duplicate event_code already exists in database: " + eventCode);
            }
        }

        String eventType = row.get("event_type");
        if (eventType == null || eventType.trim().isEmpty()) {
            errors.add("Missing required field 'event_type'");
        }

        String eventTime = row.get("event_time");
        if (eventTime == null || eventTime.trim().isEmpty()) {
            errors.add("Missing required field 'event_time'");
        } else {
            try {
                parseTimestamp(eventTime);
            } catch (Exception e) {
                errors.add("Timestamp validation failed: " + e.getMessage());
            }
        }

        String hostname = row.get("hostname");
        if (hostname == null || hostname.trim().isEmpty()) {
            errors.add("Missing required field 'hostname' / 'device_id'");
        }

        return errors;
    }
}

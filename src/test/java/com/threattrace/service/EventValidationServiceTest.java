package com.threattrace.service;

import com.threattrace.repository.EventLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class EventValidationServiceTest {

    private EventValidationService validationService;
    private EventLogRepository eventLogRepository;

    @BeforeEach
    void setUp() {
        eventLogRepository = Mockito.mock(EventLogRepository.class);
        validationService = new EventValidationService(eventLogRepository);
    }

    @Test
    void testParseTimestamp_ValidISO() {
        LocalDateTime dt = validationService.parseTimestamp("2026-10-09T09:15:00");
        assertNotNull(dt);
        assertEquals(2026, dt.getYear());
        assertEquals(10, dt.getMonthValue());
        assertEquals(9, dt.getDayOfMonth());
    }

    @Test
    void testParseTimestamp_InvalidFormat_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            validationService.parseTimestamp("INVALID_DATE_TIME");
        });
    }

    @Test
    void testValidateRawRecord_MissingFields() {
        Map<String, String> row = new HashMap<>();
        row.put("event_type", "PROCESS");
        // Missing event_code, event_time, hostname

        Set<String> batchCodes = new HashSet<>();
        List<String> errors = validationService.validateRawRecord(row, batchCodes);

        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("event_code")));
        assertTrue(errors.stream().anyMatch(e -> e.contains("event_time")));
        assertTrue(errors.stream().anyMatch(e -> e.contains("hostname")));
    }
}

package com.threattrace.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.threattrace.domain.*;
import com.threattrace.dto.DTOs.RiskScoreBreakdownDTO;
import com.threattrace.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class RiskCalculatorTest {

    private RiskCalculator riskCalculator;
    private EventLogRepository eventLogRepository;
    private InvestigationCaseRepository caseRepository;
    private EventStageMappingRepository mappingRepository;
    private RiskAssessmentRepository assessmentRepository;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        eventLogRepository = Mockito.mock(EventLogRepository.class);
        caseRepository = Mockito.mock(InvestigationCaseRepository.class);
        mappingRepository = Mockito.mock(EventStageMappingRepository.class);
        assessmentRepository = Mockito.mock(RiskAssessmentRepository.class);
        objectMapper = new ObjectMapper();

        riskCalculator = new RiskCalculator(eventLogRepository, caseRepository, mappingRepository, assessmentRepository, objectMapper);
    }

    @Test
    void testCalculateRisk_EmptyCase_ReturnsZero() {
        InvestigationCase c = new InvestigationCase("CASE-1", "Empty Case", "No logs", null);
        when(caseRepository.findById(1L)).thenReturn(Optional.of(c));
        when(eventLogRepository.findByInvestigationCaseIdOrderByEventTimeAsc(1L)).thenReturn(Collections.emptyList());

        RiskScoreBreakdownDTO breakdown = riskCalculator.calculateAndPersistRiskScore(1L);

        assertNotNull(breakdown);
        assertEquals(0.0, breakdown.getTotalScore());
    }

    @Test
    void testCalculateRisk_WithHighSeverityAndStages() {
        InvestigationCase c = new InvestigationCase("CASE-2", "Attack Case", "Critical", null);
        when(caseRepository.findById(2L)).thenReturn(Optional.of(c));

        Asset asset = new Asset("DC1", "DOMAIN_CONTROLLER", 10, "IT", "10.0.0.1");
        EndpointDevice device = new EndpointDevice(asset, "dc01.corp", "10.0.0.1", "Win 2022");

        EventLog e1 = new EventLog();
        e1.setId(100L);
        e1.setDevice(device);
        e1.setSeverity("CRITICAL");
        e1.setEventType("PROCESS");
        e1.setEventTime(LocalDateTime.now());

        when(eventLogRepository.findByInvestigationCaseIdOrderByEventTimeAsc(2L)).thenReturn(List.of(e1));
        when(mappingRepository.findByEventLogInvestigationCaseId(2L)).thenReturn(Collections.emptyList());

        RiskScoreBreakdownDTO breakdown = riskCalculator.calculateAndPersistRiskScore(2L);

        assertNotNull(breakdown);
        assertTrue(breakdown.getTotalScore() > 0.0);
        assertEquals(25.0, breakdown.getAssetCriticalityScore()); // 10 * 2.5
        assertEquals(25.0, breakdown.getMaxSeverityWeight());
    }
}

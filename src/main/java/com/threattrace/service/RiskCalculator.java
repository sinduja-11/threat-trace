package com.threattrace.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.threattrace.domain.*;
import com.threattrace.dto.DTOs.RiskScoreBreakdownDTO;
import com.threattrace.exception.CustomExceptions.ResourceNotFoundException;
import com.threattrace.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class RiskCalculator {

    private final EventLogRepository eventLogRepository;
    private final InvestigationCaseRepository caseRepository;
    private final EventStageMappingRepository mappingRepository;
    private final RiskAssessmentRepository assessmentRepository;
    private final ObjectMapper objectMapper;

    public RiskCalculator(EventLogRepository eventLogRepository,
                          InvestigationCaseRepository caseRepository,
                          EventStageMappingRepository mappingRepository,
                          RiskAssessmentRepository assessmentRepository,
                          ObjectMapper objectMapper) {
        this.eventLogRepository = eventLogRepository;
        this.caseRepository = caseRepository;
        this.mappingRepository = mappingRepository;
        this.assessmentRepository = assessmentRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public RiskScoreBreakdownDTO calculateAndPersistRiskScore(Long caseId) {
        InvestigationCase investigationCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Case not found: " + caseId));

        List<EventLog> events = eventLogRepository.findByInvestigationCaseIdOrderByEventTimeAsc(caseId);
        List<EventStageMapping> mappings = mappingRepository.findByEventLogInvestigationCaseId(caseId);

        RiskScoreBreakdownDTO breakdown = new RiskScoreBreakdownDTO();

        if (events.isEmpty()) {
            breakdown.setTotalScore(0.0);
            breakdown.getExplanations().add("No events imported for this case. Risk score is 0.0.");
            investigationCase.setRiskScore(0.0);
            caseRepository.save(investigationCase);
            return breakdown;
        }

        // Factor 1: Asset Criticality Score (Max criticality of affected devices)
        int maxAssetCriticality = events.stream()
                .mapToInt(e -> e.getDevice().getAsset().getCriticalityScore())
                .max()
                .orElse(1);
        double assetScoreComponent = maxAssetCriticality * 2.5; // Up to 25.0 points
        breakdown.setAssetCriticalityScore(assetScoreComponent);
        breakdown.getExplanations().add(String.format("Asset Criticality Factor: Target asset max criticality rating is %d/10 (+%.1f pts)", maxAssetCriticality, assetScoreComponent));

        // Factor 2: Event Severity Weight
        double maxSeverityWeight = 0.0;
        for (EventLog e : events) {
            double weight = switch (e.getSeverity().toUpperCase()) {
                case "CRITICAL" -> 25.0;
                case "HIGH" -> 18.0;
                case "MEDIUM" -> 10.0;
                default -> 4.0;
            };
            if (weight > maxSeverityWeight) maxSeverityWeight = weight;
        }
        breakdown.setMaxSeverityWeight(maxSeverityWeight);
        breakdown.getExplanations().add(String.format("Severity Factor: Highest single event severity detected (+%.1f pts)", maxSeverityWeight));

        // Factor 3: Cyber Kill Chain Stage Progression
        long distinctStagesCount = mappings.stream()
                .map(m -> m.getKillChainStage().getStageName())
                .distinct()
                .count();
        double killChainComponent = distinctStagesCount * 7.5; // Up to 52.5 pts for 7 stages
        breakdown.setKillChainProgressionScore(killChainComponent);
        breakdown.getExplanations().add(String.format("Kill Chain Progression: %d distinct attack stages observed (+%.1f pts)", distinctStagesCount, killChainComponent));

        // Factor 4: Suspicious Process Chain Multiplier
        boolean hasSuspiciousChain = events.stream().anyMatch(e ->
                e.getParentEvent() != null &&
                "PROCESS".equalsIgnoreCase(e.getEventType()) &&
                ("powershell.exe".equalsIgnoreCase(e.getProcessName()) || "cmd.exe".equalsIgnoreCase(e.getProcessName())) &&
                ("WINWORD.EXE".equalsIgnoreCase(e.getParentEvent().getProcessName()) || "EXCEL.EXE".equalsIgnoreCase(e.getParentEvent().getProcessName()))
        );

        double multiplier = hasSuspiciousChain ? 1.25 : 1.0;
        breakdown.setSuspiciousChainMultiplier(multiplier);
        if (hasSuspiciousChain) {
            breakdown.getExplanations().add("Suspicious Process Ancestry Multiplier: Document macro spawned shell execution (x1.25 multiplier applied)");
        }

        // Calculate Raw Total & Apply Cap
        double rawTotal = (assetScoreComponent + maxSeverityWeight + killChainComponent) * multiplier;
        double finalScore = Math.min(100.0, Math.round(rawTotal * 10.0) / 10.0);

        breakdown.setTotalScore(finalScore);
        breakdown.setTotalEventCount(events.size());

        // Update Case Risk Score
        investigationCase.setRiskScore(finalScore);
        if (finalScore >= 75.0) investigationCase.setSeverityLevel("CRITICAL");
        else if (finalScore >= 50.0) investigationCase.setSeverityLevel("HIGH");
        else if (finalScore >= 25.0) investigationCase.setSeverityLevel("MEDIUM");
        else investigationCase.setSeverityLevel("LOW");
        caseRepository.save(investigationCase);

        // Save Risk Assessment record
        try {
            String jsonBreakdown = objectMapper.writeValueAsString(breakdown);
            RiskAssessment assessment = new RiskAssessment(investigationCase, finalScore, breakdown.getAlgorithmVersion(), jsonBreakdown);
            assessmentRepository.save(assessment);
        } catch (Exception ignored) {}

        return breakdown;
    }
}

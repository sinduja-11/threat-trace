package com.threattrace.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.threattrace.domain.*;
import com.threattrace.dto.DTOs.AttackDNASummaryDTO;
import com.threattrace.dto.DTOs.ProcessNodeDTO;
import com.threattrace.dto.DTOs.RiskScoreBreakdownDTO;
import com.threattrace.exception.CustomExceptions.ResourceNotFoundException;
import com.threattrace.repository.*;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ReportService {

    private final InvestigationCaseRepository caseRepository;
    private final EventLogRepository eventLogRepository;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final ProcessLineageBuilder lineageBuilder;
    private final KillChainMapper killChainMapper;
    private final AttackDNAAnalyzer attackDNAAnalyzer;
    private final RiskCalculator riskCalculator;
    private final ObjectMapper objectMapper;

    public ReportService(InvestigationCaseRepository caseRepository,
                         EventLogRepository eventLogRepository,
                         UserRepository userRepository,
                         ReportRepository reportRepository,
                         ProcessLineageBuilder lineageBuilder,
                         KillChainMapper killChainMapper,
                         AttackDNAAnalyzer attackDNAAnalyzer,
                         RiskCalculator riskCalculator,
                         ObjectMapper objectMapper) {
        this.caseRepository = caseRepository;
        this.eventLogRepository = eventLogRepository;
        this.userRepository = userRepository;
        this.reportRepository = reportRepository;
        this.lineageBuilder = lineageBuilder;
        this.killChainMapper = killChainMapper;
        this.attackDNAAnalyzer = attackDNAAnalyzer;
        this.riskCalculator = riskCalculator;
        this.objectMapper = objectMapper;
    }

    public Report exportCaseReport(Long caseId, Long userId, String format) {
        InvestigationCase investigationCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Case not found: " + caseId));
        User analyst = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        String fmt = format != null ? format.toUpperCase() : "TEXT";
        List<EventLog> events = eventLogRepository.findByInvestigationCaseIdOrderByEventTimeAsc(caseId);
        List<ProcessNodeDTO> lineageTree = lineageBuilder.buildProcessLineageTree(caseId);
        RiskScoreBreakdownDTO risk = riskCalculator.calculateAndPersistRiskScore(caseId);
        AttackDNASummaryDTO dna = attackDNAAnalyzer.generateAttackDNA(caseId);

        File reportsDir = new File("reports");
        if (!reportsDir.exists()) reportsDir.mkdirs();

        String fileName = "reports/" + investigationCase.getCaseNumber() + "_Report_" + System.currentTimeMillis() + "." + fmt.toLowerCase();

        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            if ("JSON".equalsIgnoreCase(fmt)) {
                Map<String, Object> reportMap = new LinkedHashMap<>();
                reportMap.put("caseNumber", investigationCase.getCaseNumber());
                reportMap.put("title", investigationCase.getTitle());
                reportMap.put("status", investigationCase.getStatus());
                reportMap.put("severity", investigationCase.getSeverityLevel());
                reportMap.put("riskScore", risk.getTotalScore());
                reportMap.put("assignedAnalyst", analyst.getFullName());
                reportMap.put("riskBreakdown", risk);
                reportMap.put("attackDNA", dna);
                reportMap.put("processTree", lineageTree);
                reportMap.put("totalEvents", events.size());
                objectMapper.writeValue(writer, reportMap);
            } else if ("CSV".equalsIgnoreCase(fmt)) {
                writer.println("EventID,EventCode,Timestamp,EventType,Severity,Host,ProcessName,ParentEventID,Target,Payload");
                for (EventLog e : events) {
                    writer.printf("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%s,\"%s\",\"%s\"\n",
                            e.getId(), e.getEventCode(), e.getEventTime(), e.getEventType(), e.getSeverity(),
                            e.getDevice().getHostname(), e.getProcessName(),
                            e.getParentEvent() != null ? e.getParentEvent().getId() : "",
                            e.getTarget(), e.getRawPayload() != null ? e.getRawPayload().replace("\"", "'") : "");
                }
            } else {
                // Formatting Text Report
                writer.println("================================================================================");
                writer.println("                      THREAT TRACE FORENSIC INCIDENT REPORT                      ");
                writer.println("================================================================================");
                writer.println("Case Number   : " + investigationCase.getCaseNumber());
                writer.println("Case Title    : " + investigationCase.getTitle());
                writer.println("Severity      : " + investigationCase.getSeverityLevel());
                writer.println("Risk Score    : " + risk.getTotalScore() + " / 100.0");
                writer.println("Lead Analyst  : " + analyst.getFullName());
                writer.println("Generated At  : " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                writer.println("--------------------------------------------------------------------------------");
                writer.println("RISK SCORE BREAKDOWN:");
                for (String exp : risk.getExplanations()) {
                    writer.println(" - " + exp);
                }
                writer.println("--------------------------------------------------------------------------------");
                writer.println("ATTACK DNA SUMMARY:");
                writer.println(" Event Types Sequence  : " + String.join(" -> ", dna.getEventTypeSequence()));
                writer.println(" Observed Kill Chain   : " + String.join(" -> ", dna.getObservedKillChainStages()));
                writer.println(" Lead Threat Actor Match: " + dna.getLeadThreatActorMatch() + " (" + dna.getSimilarityScorePercentage() + "% similarity)");
                writer.println("================================================================================");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to write report file: " + e.getMessage());
        }

        Report report = new Report(investigationCase, analyst, fmt, fileName);
        return reportRepository.save(report);
    }
}

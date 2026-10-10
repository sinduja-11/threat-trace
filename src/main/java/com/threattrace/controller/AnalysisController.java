package com.threattrace.controller;

import com.threattrace.domain.*;
import com.threattrace.dto.DTOs.AttackDNASummaryDTO;
import com.threattrace.dto.DTOs.RiskScoreBreakdownDTO;
import com.threattrace.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analysis")
@CrossOrigin(origins = "*")
public class AnalysisController {

    private final KillChainMapper killChainMapper;
    private final AttackDNAAnalyzer dnaAnalyzer;
    private final RiskCalculator riskCalculator;
    private final MitigationAdvisor mitigationAdvisor;
    private final AnalystNoteService noteService;

    public AnalysisController(KillChainMapper killChainMapper,
                              AttackDNAAnalyzer dnaAnalyzer,
                              RiskCalculator riskCalculator,
                              MitigationAdvisor mitigationAdvisor,
                              AnalystNoteService noteService) {
        this.killChainMapper = killChainMapper;
        this.dnaAnalyzer = dnaAnalyzer;
        this.riskCalculator = riskCalculator;
        this.mitigationAdvisor = mitigationAdvisor;
        this.noteService = noteService;
    }

    @PostMapping("/kill-chain/{caseId}")
    public ResponseEntity<List<EventStageMapping>> mapKillChain(@PathVariable Long caseId) {
        return ResponseEntity.ok(killChainMapper.mapCaseEventsToKillChain(caseId));
    }

    @GetMapping("/dna/{caseId}")
    public ResponseEntity<AttackDNASummaryDTO> getAttackDNA(@PathVariable Long caseId) {
        return ResponseEntity.ok(dnaAnalyzer.generateAttackDNA(caseId));
    }

    @PostMapping("/risk/{caseId}")
    public ResponseEntity<RiskScoreBreakdownDTO> calculateRisk(@PathVariable Long caseId) {
        return ResponseEntity.ok(riskCalculator.calculateAndPersistRiskScore(caseId));
    }

    @GetMapping("/mitigations/{caseId}")
    public ResponseEntity<List<CaseMitigation>> getMitigations(
            @PathVariable Long caseId,
            @RequestParam(required = false, defaultValue = "MAX_RISK_REDUCTION") String strategy) {
        return ResponseEntity.ok(mitigationAdvisor.generateMitigationRecommendations(caseId, strategy));
    }

    @PostMapping("/notes/{caseId}")
    public ResponseEntity<AnalystNote> addNote(
            @PathVariable Long caseId,
            @RequestBody Map<String, Object> body) {
        String noteText = (String) body.get("noteText");
        Boolean isFalsePositive = (Boolean) body.get("isFalsePositive");
        return ResponseEntity.ok(noteService.addNote(caseId, 1L, noteText, isFalsePositive));
    }

    @GetMapping("/notes/{caseId}")
    public ResponseEntity<List<AnalystNote>> getNotes(@PathVariable Long caseId) {
        return ResponseEntity.ok(noteService.getNotesForCase(caseId));
    }
}

package com.threattrace.controller;

import com.threattrace.domain.*;
import com.threattrace.dto.DTOs.*;
import com.threattrace.repository.InvestigationCaseRepository;
import com.threattrace.repository.UserRepository;
import com.threattrace.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cases")
@CrossOrigin(origins = "*")
public class CaseController {

    private final InvestigationCaseRepository caseRepository;
    private final UserRepository userRepository;

    public CaseController(InvestigationCaseRepository caseRepository, UserRepository userRepository) {
        this.caseRepository = caseRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<InvestigationCase>> getAllCases() {
        return ResponseEntity.ok(caseRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvestigationCase> getCaseById(@PathVariable Long id) {
        return caseRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<InvestigationCase> createCase(@RequestBody Map<String, String> body) {
        String caseNumber = body.getOrDefault("caseNumber", "CASE-2026-" + System.currentTimeMillis() % 10000);
        String title = body.getOrDefault("title", "New Incident Investigation");
        String description = body.getOrDefault("description", "Imported incident log analysis");

        User analyst = userRepository.findById(1L).orElse(null);
        InvestigationCase c = new InvestigationCase(caseNumber, title, description, analyst);
        return ResponseEntity.ok(caseRepository.save(c));
    }
}

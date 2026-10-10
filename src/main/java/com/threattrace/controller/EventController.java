package com.threattrace.controller;

import com.threattrace.domain.EventLog;
import com.threattrace.dto.DTOs.ImportResultDTO;
import com.threattrace.dto.DTOs.ProcessNodeDTO;
import com.threattrace.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class EventController {

    private final EventImportService importService;
    private final TimelineBuilder timelineBuilder;
    private final ProcessLineageBuilder lineageBuilder;

    public EventController(EventImportService importService, TimelineBuilder timelineBuilder, ProcessLineageBuilder lineageBuilder) {
        this.importService = importService;
        this.timelineBuilder = timelineBuilder;
        this.lineageBuilder = lineageBuilder;
    }

    @PostMapping("/import/csv/{caseId}")
    public ResponseEntity<ImportResultDTO> importCsv(@PathVariable Long caseId, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(importService.importCsvLogs(caseId, file));
    }

    @PostMapping("/import/json/{caseId}")
    public ResponseEntity<ImportResultDTO> importJson(@PathVariable Long caseId, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(importService.importJsonLogs(caseId, file));
    }

    @GetMapping("/timeline/{caseId}")
    public ResponseEntity<List<EventLog>> getTimeline(
            @PathVariable Long caseId,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String hostname) {
        return ResponseEntity.ok(timelineBuilder.getTimeline(caseId, eventType, severity, hostname, null, null));
    }

    @GetMapping("/process-tree/{caseId}")
    public ResponseEntity<List<ProcessNodeDTO>> getProcessTree(@PathVariable Long caseId) {
        return ResponseEntity.ok(lineageBuilder.buildProcessLineageTree(caseId));
    }
}

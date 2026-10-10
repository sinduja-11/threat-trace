package com.threattrace.controller;

import com.threattrace.domain.Report;
import com.threattrace.service.ReportService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/export/{caseId}")
    public ResponseEntity<Report> exportReport(
            @PathVariable Long caseId,
            @RequestParam(defaultValue = "1") Long userId,
            @RequestParam(defaultValue = "TEXT") String format) {
        return ResponseEntity.ok(reportService.exportCaseReport(caseId, userId, format));
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadReport(@RequestParam String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}

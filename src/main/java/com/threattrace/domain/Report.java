package com.threattrace.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "case_id", nullable = false)
    private InvestigationCase investigationCase;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "generated_by", nullable = false)
    private User generatedBy;

    @Column(name = "report_format", nullable = false, length = 20)
    private String reportFormat; // JSON, CSV, TEXT

    @Column(name = "file_path", nullable = false, length = 255)
    private String filePath;

    @Column(name = "generated_at", updatable = false)
    private LocalDateTime generatedAt;

    public Report() {}

    public Report(InvestigationCase investigationCase, User generatedBy, String reportFormat, String filePath) {
        this.investigationCase = investigationCase;
        this.generatedBy = generatedBy;
        this.reportFormat = reportFormat;
        this.filePath = filePath;
    }

    @PrePersist
    protected void onCreate() {
        if (generatedAt == null) generatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public InvestigationCase getInvestigationCase() { return investigationCase; }
    public void setInvestigationCase(InvestigationCase investigationCase) { this.investigationCase = investigationCase; }

    public User getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(User generatedBy) { this.generatedBy = generatedBy; }

    public String getReportFormat() { return reportFormat; }
    public void setReportFormat(String reportFormat) { this.reportFormat = reportFormat; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
}

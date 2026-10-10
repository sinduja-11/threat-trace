package com.threattrace.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "risk_assessments")
public class RiskAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assessment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "case_id", nullable = false)
    private InvestigationCase investigationCase;

    @Column(name = "total_score", nullable = false)
    private Double totalScore;

    @Column(name = "assessment_time", updatable = false)
    private LocalDateTime assessmentTime;

    @Column(name = "algorithm_version", nullable = false, length = 20)
    private String algorithmVersion = "v1.0.0";

    @Column(name = "factor_breakdown_json", columnDefinition = "TEXT", nullable = false)
    private String factorBreakdownJson;

    public RiskAssessment() {}

    public RiskAssessment(InvestigationCase investigationCase, Double totalScore, String algorithmVersion, String factorBreakdownJson) {
        this.investigationCase = investigationCase;
        this.totalScore = totalScore;
        this.algorithmVersion = algorithmVersion;
        this.factorBreakdownJson = factorBreakdownJson;
    }

    @PrePersist
    protected void onCreate() {
        if (assessmentTime == null) assessmentTime = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public InvestigationCase getInvestigationCase() { return investigationCase; }
    public void setInvestigationCase(InvestigationCase investigationCase) { this.investigationCase = investigationCase; }

    public Double getTotalScore() { return totalScore; }
    public void setTotalScore(Double totalScore) { this.totalScore = totalScore; }

    public LocalDateTime getAssessmentTime() { return assessmentTime; }
    public void setAssessmentTime(LocalDateTime assessmentTime) { this.assessmentTime = assessmentTime; }

    public String getAlgorithmVersion() { return algorithmVersion; }
    public void setAlgorithmVersion(String algorithmVersion) { this.algorithmVersion = algorithmVersion; }

    public String getFactorBreakdownJson() { return factorBreakdownJson; }
    public void setFactorBreakdownJson(String factorBreakdownJson) { this.factorBreakdownJson = factorBreakdownJson; }
}

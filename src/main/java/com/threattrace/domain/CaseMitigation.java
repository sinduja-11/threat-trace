package com.threattrace.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "case_mitigations")
public class CaseMitigation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "case_mitigation_id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "case_id", nullable = false)
    private InvestigationCase investigationCase;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "mitigation_id", nullable = false)
    private MitigationStrategy mitigationStrategy;

    @Column(name = "recommendation_score", nullable = false)
    private Double recommendationScore;

    @Column(nullable = false, length = 20)
    private String status = "RECOMMENDED"; // RECOMMENDED, ACCEPTED, DISMISSED, APPLIED

    @Column(name = "analyst_rationale", columnDefinition = "TEXT")
    private String analystRationale;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public CaseMitigation() {}

    public CaseMitigation(InvestigationCase investigationCase, MitigationStrategy mitigationStrategy, Double recommendationScore, String analystRationale) {
        this.investigationCase = investigationCase;
        this.mitigationStrategy = mitigationStrategy;
        this.recommendationScore = recommendationScore;
        this.analystRationale = analystRationale;
    }

    @PrePersist
    @PreUpdate
    protected void onSave() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public InvestigationCase getInvestigationCase() { return investigationCase; }
    public void setInvestigationCase(InvestigationCase investigationCase) { this.investigationCase = investigationCase; }

    public MitigationStrategy getMitigationStrategy() { return mitigationStrategy; }
    public void setMitigationStrategy(MitigationStrategy mitigationStrategy) { this.mitigationStrategy = mitigationStrategy; }

    public Double getRecommendationScore() { return recommendationScore; }
    public void setRecommendationScore(Double recommendationScore) { this.recommendationScore = recommendationScore; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAnalystRationale() { return analystRationale; }
    public void setAnalystRationale(String analystRationale) { this.analystRationale = analystRationale; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

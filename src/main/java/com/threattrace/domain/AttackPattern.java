package com.threattrace.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attack_patterns")
public class AttackPattern {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pattern_id")
    private Long id;

    @Column(name = "pattern_code", nullable = false, unique = true, length = 50)
    private String patternCode;

    @Column(name = "pattern_name", nullable = false, length = 100)
    private String patternName;

    @Column(name = "rule_expression", columnDefinition = "TEXT", nullable = false)
    private String ruleExpression;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "stage_id", nullable = false)
    private KillChainStage stage;

    @Column(name = "severity_weight", nullable = false)
    private Double severityWeight = 1.0;

    public AttackPattern() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPatternCode() { return patternCode; }
    public void setPatternCode(String patternCode) { this.patternCode = patternCode; }

    public String getPatternName() { return patternName; }
    public void setPatternName(String patternName) { this.patternName = patternName; }

    public String getRuleExpression() { return ruleExpression; }
    public void setRuleExpression(String ruleExpression) { this.ruleExpression = ruleExpression; }

    public KillChainStage getStage() { return stage; }
    public void setStage(KillChainStage stage) { this.stage = stage; }

    public Double getSeverityWeight() { return severityWeight; }
    public void setSeverityWeight(Double severityWeight) { this.severityWeight = severityWeight; }
}

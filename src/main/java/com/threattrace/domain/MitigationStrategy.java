package com.threattrace.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "mitigation_strategies")
public class MitigationStrategy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mitigation_id")
    private Long id;

    @Column(name = "mitigation_code", nullable = false, unique = true, length = 50)
    private String mitigationCode;

    @Column(name = "action_name", nullable = false, length = 150)
    private String actionName;

    @Column(name = "effort_level", nullable = false, length = 20)
    private String effortLevel; // LOW, MEDIUM, HIGH

    @Column(name = "business_impact", nullable = false, length = 20)
    private String businessImpact; // LOW, MEDIUM, HIGH

    @Column(nullable = false, length = 50)
    private String category; // NETWORK, PROCESS, ACCOUNT, POLICY

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    public MitigationStrategy() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMitigationCode() { return mitigationCode; }
    public void setMitigationCode(String mitigationCode) { this.mitigationCode = mitigationCode; }

    public String getActionName() { return actionName; }
    public void setActionName(String actionName) { this.actionName = actionName; }

    public String getEffortLevel() { return effortLevel; }
    public void setEffortLevel(String effortLevel) { this.effortLevel = effortLevel; }

    public String getBusinessImpact() { return businessImpact; }
    public void setBusinessImpact(String businessImpact) { this.businessImpact = businessImpact; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

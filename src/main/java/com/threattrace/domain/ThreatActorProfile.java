package com.threattrace.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "threat_actor_profiles")
public class ThreatActorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long id;

    @Column(name = "actor_codename", nullable = false, unique = true, length = 100)
    private String actorCodename;

    @Column(name = "tactics_summary", columnDefinition = "TEXT", nullable = false)
    private String tacticsSummary;

    @Column(name = "typical_kill_chain_signature", nullable = false, length = 255)
    private String typicalKillChainSignature;

    @Column(name = "risk_indicator_score", nullable = false)
    private Integer riskIndicatorScore = 5;

    public ThreatActorProfile() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getActorCodename() { return actorCodename; }
    public void setActorCodename(String actorCodename) { this.actorCodename = actorCodename; }

    public String getTacticsSummary() { return tacticsSummary; }
    public void setTacticsSummary(String tacticsSummary) { this.tacticsSummary = tacticsSummary; }

    public String getTypicalKillChainSignature() { return typicalKillChainSignature; }
    public void setTypicalKillChainSignature(String typicalKillChainSignature) { this.typicalKillChainSignature = typicalKillChainSignature; }

    public Integer getRiskIndicatorScore() { return riskIndicatorScore; }
    public void setRiskIndicatorScore(Integer riskIndicatorScore) { this.riskIndicatorScore = riskIndicatorScore; }
}

package com.threattrace.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_stage_mappings")
public class EventStageMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mapping_id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id", nullable = false)
    private EventLog eventLog;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "stage_id", nullable = false)
    private KillChainStage killChainStage;

    @Column(name = "mapping_rule", nullable = false, length = 100)
    private String mappingRule;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String explanation;

    @Column(name = "mapped_at", updatable = false)
    private LocalDateTime mappedAt;

    public EventStageMapping() {}

    public EventStageMapping(EventLog eventLog, KillChainStage killChainStage, String mappingRule, String explanation) {
        this.eventLog = eventLog;
        this.killChainStage = killChainStage;
        this.mappingRule = mappingRule;
        this.explanation = explanation;
    }

    @PrePersist
    protected void onCreate() {
        if (mappedAt == null) mappedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public EventLog getEventLog() { return eventLog; }
    public void setEventLog(EventLog eventLog) { this.eventLog = eventLog; }

    public KillChainStage getKillChainStage() { return killChainStage; }
    public void setKillChainStage(KillChainStage killChainStage) { this.killChainStage = killChainStage; }

    public String getMappingRule() { return mappingRule; }
    public void setMappingRule(String mappingRule) { this.mappingRule = mappingRule; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public LocalDateTime getMappedAt() { return mappedAt; }
    public void setMappedAt(LocalDateTime mappedAt) { this.mappedAt = mappedAt; }
}

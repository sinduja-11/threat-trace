package com.threattrace.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "kill_chain_stages")
public class KillChainStage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stage_id")
    private Long id;

    @Column(name = "stage_name", nullable = false, unique = true, length = 50)
    private String stageName;

    @Column(name = "stage_order", nullable = false, unique = true)
    private Integer stageOrder;

    @Column(columnDefinition = "TEXT")
    private String description;

    public KillChainStage() {}

    public KillChainStage(String stageName, Integer stageOrder, String description) {
        this.stageName = stageName;
        this.stageOrder = stageOrder;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStageName() { return stageName; }
    public void setStageName(String stageName) { this.stageName = stageName; }

    public Integer getStageOrder() { return stageOrder; }
    public void setStageOrder(Integer stageOrder) { this.stageOrder = stageOrder; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

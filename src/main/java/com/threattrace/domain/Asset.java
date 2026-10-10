package com.threattrace.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "assets")
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "asset_id")
    private Long id;

    @Column(name = "asset_name", nullable = false, length = 100)
    private String assetName;

    @Column(name = "asset_type", nullable = false, length = 50)
    private String assetType;

    @Column(name = "criticality_score", nullable = false)
    private Integer criticalityScore; // 1 to 10

    @Column(name = "owner_team", nullable = false, length = 100)
    private String ownerTeam;

    @Column(name = "ip_subnet", length = 45)
    private String ipSubnet;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Asset() {}

    public Asset(String assetName, String assetType, Integer criticalityScore, String ownerTeam, String ipSubnet) {
        this.assetName = assetName;
        this.assetType = assetType;
        this.criticalityScore = criticalityScore;
        this.ownerTeam = ownerTeam;
        this.ipSubnet = ipSubnet;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAssetName() { return assetName; }
    public void setAssetName(String assetName) { this.assetName = assetName; }

    public String getAssetType() { return assetType; }
    public void setAssetType(String assetType) { this.assetType = assetType; }

    public Integer getCriticalityScore() { return criticalityScore; }
    public void setCriticalityScore(Integer criticalityScore) { this.criticalityScore = criticalityScore; }

    public String getOwnerTeam() { return ownerTeam; }
    public void setOwnerTeam(String ownerTeam) { this.ownerTeam = ownerTeam; }

    public String getIpSubnet() { return ipSubnet; }
    public void setIpSubnet(String ipSubnet) { this.ipSubnet = ipSubnet; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

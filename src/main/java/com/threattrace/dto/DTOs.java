package com.threattrace.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DTOs {

    public static class ProcessNodeDTO {
        private Long eventId;
        private String eventCode;
        private String processName;
        private Long parentEventId;
        private String severity;
        private String eventType;
        private LocalDateTime eventTime;
        private String target;
        private Integer depthLevel;
        private List<ProcessNodeDTO> children = new ArrayList<>();

        public ProcessNodeDTO() {}

        public ProcessNodeDTO(Long eventId, String eventCode, String processName, Long parentEventId,
                              String severity, String eventType, LocalDateTime eventTime, String target, Integer depthLevel) {
            this.eventId = eventId;
            this.eventCode = eventCode;
            this.processName = processName;
            this.parentEventId = parentEventId;
            this.severity = severity;
            this.eventType = eventType;
            this.eventTime = eventTime;
            this.target = target;
            this.depthLevel = depthLevel;
        }

        public Long getEventId() { return eventId; }
        public void setEventId(Long eventId) { this.eventId = eventId; }

        public String getEventCode() { return eventCode; }
        public void setEventCode(String eventCode) { this.eventCode = eventCode; }

        public String getProcessName() { return processName; }
        public void setProcessName(String processName) { this.processName = processName; }

        public Long getParentEventId() { return parentEventId; }
        public void setParentEventId(Long parentEventId) { this.parentEventId = parentEventId; }

        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }

        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }

        public LocalDateTime getEventTime() { return eventTime; }
        public void setEventTime(LocalDateTime eventTime) { this.eventTime = eventTime; }

        public String getTarget() { return target; }
        public void setTarget(String target) { this.target = target; }

        public Integer getDepthLevel() { return depthLevel; }
        public void setDepthLevel(Integer depthLevel) { this.depthLevel = depthLevel; }

        public List<ProcessNodeDTO> getChildren() { return children; }
        public void setChildren(List<ProcessNodeDTO> children) { this.children = children; }

        public void addChild(ProcessNodeDTO child) {
            this.children.add(child);
        }
    }

    public static class RiskScoreBreakdownDTO {
        private Double totalScore;
        private Double assetCriticalityScore;
        private Double maxSeverityWeight;
        private Double killChainProgressionScore;
        private Double suspiciousChainMultiplier;
        private Integer totalEventCount;
        private String algorithmVersion = "v1.0.0";
        private List<String> explanations = new ArrayList<>();

        public RiskScoreBreakdownDTO() {}

        public Double getTotalScore() { return totalScore; }
        public void setTotalScore(Double totalScore) { this.totalScore = totalScore; }

        public Double getAssetCriticalityScore() { return assetCriticalityScore; }
        public void setAssetCriticalityScore(Double assetCriticalityScore) { this.assetCriticalityScore = assetCriticalityScore; }

        public Double getMaxSeverityWeight() { return maxSeverityWeight; }
        public void setMaxSeverityWeight(Double maxSeverityWeight) { this.maxSeverityWeight = maxSeverityWeight; }

        public Double getKillChainProgressionScore() { return killChainProgressionScore; }
        public void setKillChainProgressionScore(Double killChainProgressionScore) { this.killChainProgressionScore = killChainProgressionScore; }

        public Double getSuspiciousChainMultiplier() { return suspiciousChainMultiplier; }
        public void setSuspiciousChainMultiplier(Double suspiciousChainMultiplier) { this.suspiciousChainMultiplier = suspiciousChainMultiplier; }

        public Integer getTotalEventCount() { return totalEventCount; }
        public void setTotalEventCount(Integer totalEventCount) { this.totalEventCount = totalEventCount; }

        public String getAlgorithmVersion() { return algorithmVersion; }
        public void setAlgorithmVersion(String algorithmVersion) { this.algorithmVersion = algorithmVersion; }

        public List<String> getExplanations() { return explanations; }
        public void setExplanations(List<String> explanations) { this.explanations = explanations; }
    }

    public static class AttackDNASummaryDTO {
        private List<String> eventTypeSequence = new ArrayList<>();
        private List<Long> timeGapsSeconds = new ArrayList<>();
        private List<String> affectedAssets = new ArrayList<>();
        private List<String> observedKillChainStages = new ArrayList<>();
        private String LeadThreatActorMatch;
        private Double similarityScorePercentage;

        public AttackDNASummaryDTO() {}

        public List<String> getEventTypeSequence() { return eventTypeSequence; }
        public void setEventTypeSequence(List<String> eventTypeSequence) { this.eventTypeSequence = eventTypeSequence; }

        public List<Long> getTimeGapsSeconds() { return timeGapsSeconds; }
        public void setTimeGapsSeconds(List<Long> timeGapsSeconds) { this.timeGapsSeconds = timeGapsSeconds; }

        public List<String> getAffectedAssets() { return affectedAssets; }
        public void setAffectedAssets(List<String> affectedAssets) { this.affectedAssets = affectedAssets; }

        public List<String> getObservedKillChainStages() { return observedKillChainStages; }
        public void setObservedKillChainStages(List<String> observedKillChainStages) { this.observedKillChainStages = observedKillChainStages; }

        public String getLeadThreatActorMatch() { return LeadThreatActorMatch; }
        public void setLeadThreatActorMatch(String leadThreatActorMatch) { LeadThreatActorMatch = leadThreatActorMatch; }

        public Double getSimilarityScorePercentage() { return similarityScorePercentage; }
        public void setSimilarityScorePercentage(Double similarityScorePercentage) { this.similarityScorePercentage = similarityScorePercentage; }
    }

    public static class ImportResultDTO {
        private Long caseId;
        private int totalProcessed;
        private int validCount;
        private int errorCount;
        private List<String> validationErrors = new ArrayList<>();

        public ImportResultDTO() {}

        public ImportResultDTO(Long caseId, int totalProcessed, int validCount, int errorCount, List<String> validationErrors) {
            this.caseId = caseId;
            this.totalProcessed = totalProcessed;
            this.validCount = validCount;
            this.errorCount = errorCount;
            this.validationErrors = validationErrors;
        }

        public Long getCaseId() { return caseId; }
        public void setCaseId(Long caseId) { this.caseId = caseId; }

        public int getTotalProcessed() { return totalProcessed; }
        public void setTotalProcessed(int totalProcessed) { this.totalProcessed = totalProcessed; }

        public int getValidCount() { return validCount; }
        public void setValidCount(int validCount) { this.validCount = validCount; }

        public int getErrorCount() { return errorCount; }
        public void setErrorCount(int errorCount) { this.errorCount = errorCount; }

        public List<String> getValidationErrors() { return validationErrors; }
        public void setValidationErrors(List<String> validationErrors) { this.validationErrors = validationErrors; }
    }
}

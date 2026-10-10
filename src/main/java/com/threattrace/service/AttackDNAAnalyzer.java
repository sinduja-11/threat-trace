package com.threattrace.service;

import com.threattrace.domain.*;
import com.threattrace.dto.DTOs.AttackDNASummaryDTO;
import com.threattrace.repository.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AttackDNAAnalyzer {

    private final EventLogRepository eventLogRepository;
    private final EventStageMappingRepository mappingRepository;
    private final ThreatActorProfileRepository profileRepository;

    public AttackDNAAnalyzer(EventLogRepository eventLogRepository,
                              EventStageMappingRepository mappingRepository,
                              ThreatActorProfileRepository profileRepository) {
        this.eventLogRepository = eventLogRepository;
        this.mappingRepository = mappingRepository;
        this.profileRepository = profileRepository;
    }

    public AttackDNASummaryDTO generateAttackDNA(Long caseId) {
        List<EventLog> events = eventLogRepository.findByInvestigationCaseIdOrderByEventTimeAsc(caseId);
        List<EventStageMapping> mappings = mappingRepository.findByEventLogInvestigationCaseId(caseId);

        AttackDNASummaryDTO dna = new AttackDNASummaryDTO();

        if (events.isEmpty()) {
            dna.setLeadThreatActorMatch("NONE");
            dna.setSimilarityScorePercentage(0.0);
            return dna;
        }

        // 1. Sequence of Event Types
        List<String> eventTypes = events.stream().map(EventLog::getEventType).collect(Collectors.toList());
        dna.setEventTypeSequence(eventTypes);

        // 2. Inter-event Time Gaps (Seconds)
        List<Long> timeGaps = new ArrayList<>();
        for (int i = 1; i < events.size(); i++) {
            long gap = Duration.between(events.get(i - 1).getEventTime(), events.get(i).getEventTime()).getSeconds();
            timeGaps.add(gap);
        }
        dna.setTimeGapsSeconds(timeGaps);

        // 3. Affected Assets / Devices
        List<String> assets = events.stream()
                .map(e -> e.getDevice().getHostname())
                .distinct()
                .collect(Collectors.toList());
        dna.setAffectedAssets(assets);

        // 4. Observed Kill Chain Stages
        List<String> stages = mappings.stream()
                .map(m -> m.getKillChainStage().getStageName())
                .distinct()
                .collect(Collectors.toList());
        dna.setObservedKillChainStages(stages);

        // 5. Threat Actor Profile Lead Similarity Calculation
        List<ThreatActorProfile> profiles = profileRepository.findAll();
        String leadMatch = "UNKNOWN / GENERIC_MALWARE";
        double maxSimilarity = 0.0;

        for (ThreatActorProfile profile : profiles) {
            double similarity = calculateProfileSimilarity(stages, profile);
            if (similarity > maxSimilarity) {
                maxSimilarity = similarity;
                leadMatch = profile.getActorCodename() + " (Lead Similarity)";
            }
        }

        dna.setLeadThreatActorMatch(leadMatch);
        dna.setSimilarityScorePercentage(Math.round(maxSimilarity * 10.0) / 10.0);

        return dna;
    }

    private double calculateProfileSimilarity(List<String> observedStages, ThreatActorProfile profile) {
        String signature = profile.getTypicalKillChainSignature();
        if (signature == null || signature.isEmpty() || observedStages.isEmpty()) return 0.0;

        String[] sigStages = signature.split("->");
        int matchCount = 0;
        for (String sigStage : sigStages) {
            String trimmed = sigStage.trim().toLowerCase();
            for (String obs : observedStages) {
                if (obs.toLowerCase().contains(trimmed)) {
                    matchCount++;
                    break;
                }
            }
        }

        return ((double) matchCount / Math.max(sigStages.length, 1)) * 100.0;
    }
}

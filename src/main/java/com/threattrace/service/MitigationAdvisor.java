package com.threattrace.service;

import com.threattrace.domain.*;
import com.threattrace.exception.CustomExceptions.ResourceNotFoundException;
import com.threattrace.repository.*;
import com.threattrace.service.strategy.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class MitigationAdvisor {

    private final InvestigationCaseRepository caseRepository;
    private final MitigationStrategyRepository strategyRepository;
    private final CaseMitigationRepository caseMitigationRepository;

    private final Map<String, MitigationRankingStrategy> strategyMap = new HashMap<>();

    public MitigationAdvisor(InvestigationCaseRepository caseRepository,
                             MitigationStrategyRepository strategyRepository,
                             CaseMitigationRepository caseMitigationRepository,
                             RiskReductionStrategy riskReductionStrategy,
                             LowEffortStrategy lowEffortStrategy,
                             BusinessImpactStrategy businessImpactStrategy) {
        this.caseRepository = caseRepository;
        this.strategyRepository = strategyRepository;
        this.caseMitigationRepository = caseMitigationRepository;

        strategyMap.put("MAX_RISK_REDUCTION", riskReductionStrategy);
        strategyMap.put("LOWEST_EFFORT_FIRST", lowEffortStrategy);
        strategyMap.put("MINIMAL_BUSINESS_IMPACT", businessImpactStrategy);
    }

    @Transactional
    public List<CaseMitigation> generateMitigationRecommendations(Long caseId, String rankingStrategyName) {
        InvestigationCase investigationCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Case not found: " + caseId));

        MitigationRankingStrategy strategy = strategyMap.getOrDefault(
                rankingStrategyName != null ? rankingStrategyName.toUpperCase() : "MAX_RISK_REDUCTION",
                strategyMap.get("MAX_RISK_REDUCTION")
        );

        List<MitigationStrategy> catalog = strategyRepository.findAll();
        double caseRisk = investigationCase.getRiskScore() != null ? investigationCase.getRiskScore() : 50.0;

        List<CaseMitigation> recommendations = new ArrayList<>();

        for (MitigationStrategy ms : catalog) {
            double score = strategy.calculateScore(ms, caseRisk);
            String rationale = String.format("Evaluated using %s strategy. Expected effectiveness: %.1f/100.", strategy.getStrategyName(), score);

            Optional<CaseMitigation> existing = caseMitigationRepository.findByInvestigationCaseIdOrderByRecommendationScoreDesc(caseId).stream()
                    .filter(cm -> cm.getMitigationStrategy().getId().equals(ms.getId()))
                    .findFirst();

            if (existing.isPresent()) {
                CaseMitigation cm = existing.get();
                cm.setRecommendationScore(score);
                cm.setAnalystRationale(rationale);
                recommendations.add(caseMitigationRepository.save(cm));
            } else {
                CaseMitigation cm = new CaseMitigation(investigationCase, ms, score, rationale);
                recommendations.add(caseMitigationRepository.save(cm));
            }
        }

        recommendations.sort((a, b) -> Double.compare(b.getRecommendationScore(), a.getRecommendationScore()));
        return recommendations;
    }
}

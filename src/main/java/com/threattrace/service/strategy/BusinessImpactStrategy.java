package com.threattrace.service.strategy;

import com.threattrace.domain.MitigationStrategy;
import org.springframework.stereotype.Component;

@Component
public class BusinessImpactStrategy implements MitigationRankingStrategy {
    @Override
    public double calculateScore(MitigationStrategy strategy, double caseRiskScore) {
        double impactWeight = switch (strategy.getBusinessImpact().toUpperCase()) {
            case "LOW" -> 95.0; // Minimal disruption to business
            case "MEDIUM" -> 65.0;
            default -> 35.0;
        };
        return (impactWeight * 0.65) + (caseRiskScore * 0.35);
    }

    @Override
    public String getStrategyName() {
        return "MINIMAL_BUSINESS_IMPACT";
    }
}

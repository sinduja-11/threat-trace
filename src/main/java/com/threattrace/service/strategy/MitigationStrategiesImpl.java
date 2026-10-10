package com.threattrace.service.strategy;

import com.threattrace.domain.MitigationStrategy;
import org.springframework.stereotype.Component;

@Component
public class RiskReductionStrategy implements MitigationRankingStrategy {
    @Override
    public double calculateScore(MitigationStrategy strategy, double caseRiskScore) {
        double base = caseRiskScore * 0.75;
        if ("NETWORK".equalsIgnoreCase(strategy.getCategory())) base += 20.0;
        if ("PROCESS".equalsIgnoreCase(strategy.getCategory())) base += 15.0;
        return Math.min(100.0, base);
    }

    @Override
    public String getStrategyName() {
        return "MAX_RISK_REDUCTION";
    }
}

@Component
public class LowEffortStrategy implements MitigationRankingStrategy {
    @Override
    public double calculateScore(MitigationStrategy strategy, double caseRiskScore) {
        double effortWeight = switch (strategy.getEffortLevel().toUpperCase()) {
            case "LOW" -> 90.0;
            case "MEDIUM" -> 60.0;
            default -> 30.0;
        };
        return (effortWeight * 0.7) + (caseRiskScore * 0.3);
    }

    @Override
    public String getStrategyName() {
        return "LOWEST_EFFORT_FIRST";
    }
}

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

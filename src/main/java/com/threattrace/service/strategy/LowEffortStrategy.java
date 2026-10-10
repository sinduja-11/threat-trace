package com.threattrace.service.strategy;

import com.threattrace.domain.MitigationStrategy;
import org.springframework.stereotype.Component;

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

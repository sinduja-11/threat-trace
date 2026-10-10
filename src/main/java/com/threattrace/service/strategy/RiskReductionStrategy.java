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

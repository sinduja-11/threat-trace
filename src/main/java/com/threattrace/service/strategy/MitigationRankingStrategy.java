package com.threattrace.service.strategy;

import com.threattrace.domain.MitigationStrategy;

public interface MitigationRankingStrategy {
    double calculateScore(MitigationStrategy strategy, double caseRiskScore);
    String getStrategyName();
}

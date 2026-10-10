package com.threattrace.repository;

import com.threattrace.domain.KillChainStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KillChainStageRepository extends JpaRepository<KillChainStage, Long> {
    Optional<KillChainStage> findByStageName(String stageName);
    List<KillChainStage> findAllByOrderByStageOrderAsc();
}

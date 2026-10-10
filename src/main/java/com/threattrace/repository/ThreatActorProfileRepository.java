package com.threattrace.repository;

import com.threattrace.domain.ThreatActorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ThreatActorProfileRepository extends JpaRepository<ThreatActorProfile, Long> {
    Optional<ThreatActorProfile> findByActorCodename(String actorCodename);
}

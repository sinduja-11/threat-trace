package com.threattrace.repository;

import com.threattrace.domain.EndpointDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EndpointDeviceRepository extends JpaRepository<EndpointDevice, Long> {
    Optional<EndpointDevice> findByHostname(String hostname);
}

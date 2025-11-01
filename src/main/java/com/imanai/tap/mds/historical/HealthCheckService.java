package com.imanai.tap.mds.historical;

import io.micrometer.tracing.annotation.NewSpan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class HealthCheckService {
    private static final Logger log = LoggerFactory.getLogger(HealthCheckRepository.class);

    private final HealthCheckRepository healthCheckRepository;

    public HealthCheckService(HealthCheckRepository healthCheckRepository) {
        this.healthCheckRepository = healthCheckRepository;
    }

    @NewSpan
    public String getDatabaseStatus() {
        log.info("Health check HealthCheckService getDatabaseStatus called");

        String status = healthCheckRepository.checkDatabase();

        return "Service OK, DB: " + status;
    }
}
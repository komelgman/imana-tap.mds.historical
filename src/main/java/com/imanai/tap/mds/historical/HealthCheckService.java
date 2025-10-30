package com.imanai.tap.mds.historical;

import org.springframework.stereotype.Service;

@Service
public class HealthCheckService {

    private final HealthCheckRepository healthCheckRepository;

    public HealthCheckService(HealthCheckRepository healthCheckRepository) {
        this.healthCheckRepository = healthCheckRepository;
    }

    public String getStatus() {
        String dbStatus = healthCheckRepository.checkDatabase();
        return "Service OK, DB: " + dbStatus;
    }
}
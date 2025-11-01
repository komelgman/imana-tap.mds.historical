package com.imanai.tap.mds.historical;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class HealthCheckRepository {

    private static final Logger log = LoggerFactory.getLogger(HealthCheckRepository.class);

    public String checkDatabase() {
        log.info("Health check HealthCheckRepository checkDatabase called");

        return "SIMULATED_OK";
    }
}
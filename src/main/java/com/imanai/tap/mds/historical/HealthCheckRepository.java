package com.imanai.tap.mds.historical;

import org.springframework.stereotype.Repository;

@Repository
public class HealthCheckRepository {

    public String checkDatabase() {
        // Пока заглушка без реальной БД
        return "SIMULATED_OK";
    }
}
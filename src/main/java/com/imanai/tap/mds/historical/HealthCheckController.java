package com.imanai.tap.mds.historical;

import javax.naming.OperationNotSupportedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
class HealthCheckController {

    private static final Logger log = LoggerFactory.getLogger(HealthCheckController.class);
    private final HealthCheckService healthCheckService;

    public HealthCheckController(HealthCheckService healthCheckService) {
        this.healthCheckService = healthCheckService;
    }

    @GetMapping
    public String check(@RequestParam(defaultValue = "0") int delayMs) throws InterruptedException {
        log.info("HealthCheckController.check called");

        if (delayMs < 0) {
            throw new IllegalArgumentException("Delay must be non-negative");
        }

        String status = healthCheckService.getDatabaseStatus();

        if (delayMs > 0) {
            Thread.sleep(delayMs);
        }

        return status;
    }

    @GetMapping("/detailed")
    public HealthDetailsResponse detailedCheck() {
        log.info("HealthCheckController.detailedCheck called");

        return new HealthDetailsResponse(
                healthCheckService.getDatabaseStatus(),
                System.currentTimeMillis()
        );
    }

    @GetMapping("/exceptions/runtime")
    public HealthDetailsResponse runtimeException() {
        throw new RuntimeException("For testing purpose");
    }

    @GetMapping("/exceptions/generic")
    public HealthDetailsResponse genericException() throws OperationNotSupportedException {
        throw new OperationNotSupportedException("For testing purpose");
    }

    record HealthDetailsResponse(
            String databaseStatus,
            long timestamp
    ) {
    }
}

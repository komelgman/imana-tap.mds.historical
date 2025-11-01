package com.imanai.tap.mds.historical;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HealthCheckServiceTest {

    @Mock
    private HealthCheckRepository healthCheckRepository;

    @InjectMocks
    private HealthCheckService healthCheckService;

    @Test
    void getDatabaseStatusReturnsCombinedStatus() {
        when(healthCheckRepository.checkDatabase()).thenReturn("SIMULATED_OK");
        
        String result = healthCheckService.getDatabaseStatus();
        
        assertEquals("Service OK, DB: SIMULATED_OK", result);
    }

    @Test
    void getDatabaseStatusHandlesDifferentRepositoryResponses() {
        when(healthCheckRepository.checkDatabase()).thenReturn("CONNECTION_FAILURE");
        
        String result = healthCheckService.getDatabaseStatus();
        
        assertEquals("Service OK, DB: CONNECTION_FAILURE", result);
    }
}
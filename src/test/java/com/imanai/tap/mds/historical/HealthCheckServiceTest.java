package com.imanai.tap.mds.historical;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @Test
    void getDatabaseStatusThrowsWhenRepositoryFails() {
        doThrow(new RuntimeException("Database connection failed"))
                .when(healthCheckRepository).checkDatabase();

        Exception exception = assertThrows(RuntimeException.class,
                () -> healthCheckService.getDatabaseStatus());

        assertEquals("Database connection failed", exception.getMessage());
    }
}

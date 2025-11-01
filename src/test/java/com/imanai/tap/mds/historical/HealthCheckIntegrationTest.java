package com.imanai.tap.mds.historical;

import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class HealthCheckIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthCheckEndpointReturnsOk() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Service OK, DB: SIMULATED_OK"));
    }

    @Test
    void detailedHealthCheckReturnsValidResponse() throws Exception {
        mockMvc.perform(get("/api/health/detailed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.databaseStatus").value("Service OK, DB: SIMULATED_OK"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 100, 500})
    void healthCheckWithDelayReturnsAfterSpecifiedTime(int delayMs) throws Exception {
        long startTime = System.currentTimeMillis();
        mockMvc.perform(get("/api/health").param("delayMs", String.valueOf(delayMs)))
                .andExpect(status().isOk());
        long duration = System.currentTimeMillis() - startTime;
        
        assertTrue(duration >= delayMs, "Response time should be at least the delay duration");
    }

    @Test
    void healthCheckWithNegativeDelayReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/health").param("delayMs", "-100"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Delay must be non-negative"));
    }
}
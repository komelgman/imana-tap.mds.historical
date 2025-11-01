package com.imanai.tap.mds.historical;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
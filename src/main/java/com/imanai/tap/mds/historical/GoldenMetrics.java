package com.imanai.tap.mds.historical;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class GoldenMetrics {

    private final MeterRegistry registry;
    private final Timer healthCheckLatency;
    private final Counter requestCounter;
    private final Counter errorCounter;

    private final AtomicInteger activeConnections = new AtomicInteger(0);
    private final AtomicInteger queueSize = new AtomicInteger(0);
    
    public GoldenMetrics(MeterRegistry registry) {
        this.registry = registry;
        
        this.healthCheckLatency = Timer.builder("health.check.duration")
            .description("Health check response time")
            .tag("component", "health-check")
            .publishPercentiles(0.5, 0.95, 0.99)
            .publishPercentileHistogram()
            .serviceLevelObjectives(
                java.time.Duration.ofMillis(10),
                java.time.Duration.ofMillis(50),
                java.time.Duration.ofMillis(100),
                java.time.Duration.ofMillis(200)
            )
            .register(registry);
        
        this.requestCounter = Counter.builder("health.check.requests.total")
            .description("Total health check requests")
            .tag("component", "health-check")
            .register(registry);
        
        this.errorCounter = Counter.builder("health.check.errors.total")
            .description("Total health check errors")
            .tag("component", "health-check")
            .register(registry);
        
        // SATURATION - Active connections
        Gauge.builder("health.check.connections.active", activeConnections, AtomicInteger::get)
            .description("Currently active health check connections")
            .tag("component", "health-check")
            .register(registry);
        
        // SATURATION - Queue size
        Gauge.builder("health.check.queue.size", queueSize, AtomicInteger::get)
            .description("Health check processing queue size")
            .tag("component", "health-check")
            .register(registry);
        
        // SATURATION - Connection pool utilization (example)
        Gauge.builder("health.check.pool.utilization", this, self -> {
                int active = activeConnections.get();
                int maxConnections = 100; // from config
                return (double) active / maxConnections;
            })
            .description("Connection pool utilization ratio (0-1)")
            .tag("component", "health-check")
            .register(registry);
    }
    
    public Timer.Sample startTimer() {
        activeConnections.incrementAndGet();
        return Timer.start(registry);
    }
    
    public void recordSuccess(Timer.Sample sample) {
        sample.stop(healthCheckLatency);
        requestCounter.increment();
        activeConnections.decrementAndGet();
    }
    
    public void recordError(Timer.Sample sample, String errorType) {
        sample.stop(Timer.builder("health.check.duration")
            .tag("component", "health-check")
            .tag("status", "error")
            .tag("error.type", errorType)
            .register(registry));
        
        errorCounter.increment();
        activeConnections.decrementAndGet();
    }
    
    public void incrementQueue() {
        queueSize.incrementAndGet();
    }
    
    public void decrementQueue() {
        queueSize.decrementAndGet();
    }
}

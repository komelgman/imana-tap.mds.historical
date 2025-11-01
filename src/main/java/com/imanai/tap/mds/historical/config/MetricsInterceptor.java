package com.imanai.tap.mds.historical.config;

import com.imanai.tap.mds.historical.GoldenMetrics;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
class MetricsInterceptor implements HandlerInterceptor {

    private static final String TIMER_SAMPLE_ATTR = "metrics.timer.sample";
    private final GoldenMetrics metrics;

    public MetricsInterceptor(GoldenMetrics metrics) {
        this.metrics = metrics;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Timer.Sample sample = metrics.startTimer();
        request.setAttribute(TIMER_SAMPLE_ATTR, sample);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Timer.Sample sample = (Timer.Sample) request.getAttribute(TIMER_SAMPLE_ATTR);

        if (sample != null && ex == null) {
            if (response.getStatus() >= 400) {
                metrics.recordError(sample, "HttpError" + response.getStatus());
            } else {
                metrics.recordSuccess(sample);
            }
        }
    }
}

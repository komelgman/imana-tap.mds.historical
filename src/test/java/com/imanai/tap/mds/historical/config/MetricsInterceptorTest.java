package com.imanai.tap.mds.historical.config;

import com.imanai.tap.mds.historical.GoldenMetrics;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MetricsInterceptorTest {

    private MetricsInterceptor interceptor;
    private GoldenMetrics metrics;
    private HttpServletRequest request;
    private HttpServletResponse response;

    @BeforeEach
    void setup() {
        metrics = mock(GoldenMetrics.class);
        interceptor = new MetricsInterceptor(metrics);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
    }

    @Test
    void shouldRecordSuccessOn200() throws Exception {
        Timer.Sample sample = Timer.start();
        when(metrics.startTimer()).thenReturn(sample);
        when(response.getStatus()).thenReturn(200);

        interceptor.preHandle(request, response, new Object());
        when(request.getAttribute("metrics.timer.sample")).thenReturn(sample);
        interceptor.afterCompletion(request, response, new Object(), null);

        verify(metrics).startTimer();
        verify(metrics).recordSuccess(sample);
        verify(metrics, never()).recordError(any(), anyString());
    }

    @Test
    void shouldRecordErrorOn4xx() throws Exception {
        Timer.Sample sample = Timer.start();
        when(metrics.startTimer()).thenReturn(sample);
        when(response.getStatus()).thenReturn(400);

        interceptor.preHandle(request, response, new Object());
        when(request.getAttribute("metrics.timer.sample")).thenReturn(sample);
        interceptor.afterCompletion(request, response, new Object(), null);

        verify(metrics).recordError(eq(sample), eq("HttpError400"));
        verify(metrics, never()).recordSuccess(any());
    }
}

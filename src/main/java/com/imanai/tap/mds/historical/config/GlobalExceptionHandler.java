package com.imanai.tap.mds.historical.config;

import com.imanai.tap.mds.historical.GoldenMetrics;
import io.micrometer.core.instrument.Timer;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String TIMER_SAMPLE_ATTR = "metrics.timer.sample";

    private final Tracer tracer;
    private final GoldenMetrics metrics;

    public GlobalExceptionHandler(Tracer tracer, GoldenMetrics metrics) {
        this.tracer = tracer;
        this.metrics = metrics;
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ProblemDetail> handleRuntimeException(
            RuntimeException ex,
            HttpServletRequest request) {

        log.error("Runtime exception occurred", ex);

        recordExceptionInSpan(ex);
        recordErrorMetrics(request, ex);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage() != null ? ex.getMessage() : "Internal server error"
        );
        problem.setTitle("Runtime Error");
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("traceId", getCurrentTraceId());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(problem);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        log.warn("Invalid argument: {}", ex.getMessage());

        recordExceptionInSpan(ex);
        recordErrorMetrics(request, ex);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        problem.setTitle("Invalid Request");
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("traceId", getCurrentTraceId());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unexpected exception occurred", ex);

        recordExceptionInSpan(ex);
        recordErrorMetrics(request, ex);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred"
        );
        problem.setTitle("Internal Server Error");
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("traceId", getCurrentTraceId());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(problem);
    }

    private void recordExceptionInSpan(Exception ex) {
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            currentSpan.error(ex);
            currentSpan.tag("error.type", ex.getClass().getSimpleName());
            currentSpan.tag("error.message", ex.getMessage() != null ? ex.getMessage() : "");
        }
    }

    private void recordErrorMetrics(HttpServletRequest request, Exception ex) {
        Timer.Sample sample = (Timer.Sample) request.getAttribute(TIMER_SAMPLE_ATTR);
        if (sample != null) {
            metrics.recordError(sample, ex.getClass().getSimpleName());
            request.removeAttribute(TIMER_SAMPLE_ATTR);
        }
    }

    private String getCurrentTraceId() {
        Span currentSpan = tracer.currentSpan();
        return currentSpan != null ? currentSpan.context().traceId() : "unknown";
    }
}
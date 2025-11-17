package com.imanai.tap.mds.historical.config;

import com.imanai.tap.mds.historical.exceptions.HasCategory;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String TIMESTAMP = "timestamp";
    private static final String TRACE_ID = "traceId";
    private static final String SPAN_ID = "spanId";
    private static final String UNKNOWN = "unknown";

    private final Tracer tracer;

    public GlobalExceptionHandler(Tracer tracer) {
        this.tracer = tracer;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Invalid argument: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(createProblemDetail(ex, HttpStatus.BAD_REQUEST, "Invalid Request"));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ProblemDetail> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime exception occurred", ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createProblemDetail(ex, HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(Exception ex) {
        log.error("Unexpected exception occurred", ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createProblemDetail(ex, HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"));
    }

    private ProblemDetail createProblemDetail(Exception ex, HttpStatus status, String title) {
        String spanId = recordExceptionInSpan(ex);

        String detail = ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred";
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);

        problem.setTitle(title);
        problem.setProperty(TIMESTAMP, Instant.now());
        problem.setProperty(TRACE_ID, getCurrentTraceId());
        problem.setProperty(SPAN_ID, spanId);

        return problem;
    }

    private String recordExceptionInSpan(Exception ex) {
        Span currentSpan = tracer.currentSpan();
        if (currentSpan == null) {
            return UNKNOWN;
        }

        currentSpan.error(ex);
        currentSpan.tag("error.type", ex.getClass().getSimpleName());
        currentSpan.tag("error.message", ex.getMessage() != null ? ex.getMessage() : "");

        ResponseStatus annotation = ex.getClass().getAnnotation(ResponseStatus.class);
        HttpStatus status = annotation != null
                ? annotation.code()
                : HttpStatus.INTERNAL_SERVER_ERROR;
        currentSpan.tag("error.code", String.valueOf(status.value()));

        if (ex instanceof HasCategory hasCategory) {
            currentSpan.tag("error.category", hasCategory.getCategory());
        }

        return currentSpan.context().spanId();
    }

    private String getCurrentTraceId() {
        Span currentSpan = tracer.currentSpan();
        return currentSpan != null ? currentSpan.context().traceId() : UNKNOWN;
    }
}

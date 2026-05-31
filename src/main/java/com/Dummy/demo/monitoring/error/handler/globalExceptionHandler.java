package com.Dummy.demo.monitoring.error.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.Dummy.demo.monitoring.error.model.errorLog;
import com.Dummy.demo.monitoring.error.service.errorLogService;
import com.Dummy.demo.monitoring.service.RequestMetricsService;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class globalExceptionHandler {

    @Autowired
    private errorLogService errorService;

    @Autowired
    private RequestMetricsService metricsService;

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handle(RuntimeException e, HttpServletRequest request) {
        try {
            String service = (String) request.getAttribute("service");
            String operation = (String) request.getAttribute("operation");

            if ("SYSTEM_DOWN".equals(e.getMessage())) {
                metricsService.markSystemDown();
            }

            errorLog log = errorService.logFromException(e, service, operation);
            return ResponseEntity.status(500).body(log);
        } finally {
            errorService.clearRequestLoggingFlag();
        }
    }
}

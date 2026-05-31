package com.Dummy.demo.monitoring.error.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;

import com.Dummy.demo.monitoring.error.ErrorClassifier;
import com.Dummy.demo.monitoring.error.model.ErrorCategory;
import com.Dummy.demo.monitoring.error.model.errorLog;

@Service
public class errorLogService {

    private final List<errorLog> logs = new CopyOnWriteArrayList<>();
    private final ThreadLocal<Boolean> loggedOnCurrentThread = ThreadLocal.withInitial(() -> false);

    public void log(errorLog log) {
        logs.add(log);
    }

    public void logStructured(ErrorCategory category, String message, String service, String operation,
            String dependency) {
        errorLog entry = new errorLog();
        entry.setTimestamp(System.currentTimeMillis());
        entry.setErrorType(category.name());
        entry.setMessage(message);
        entry.setService(service);
        entry.setOperation(operation);
        entry.setDependency(dependency);
        logs.add(entry);
    }

    public void logDependencyFailure(String dependency, Exception e, String operation) {
        ErrorCategory category = ErrorClassifier.classifyDependencyFailure(dependency, e);
        String message = e.getMessage() != null ? e.getMessage() : category.name();
        logStructured(category, message, null, operation, dependency);
        loggedOnCurrentThread.set(true);
    }

    public errorLog logFromException(Exception e, String service, String operation) {
        if (Boolean.TRUE.equals(loggedOnCurrentThread.get())) {
            return logs.isEmpty() ? null : logs.get(logs.size() - 1);
        }
        ErrorCategory category = ErrorClassifier.classifyException(e);
        String dependency = ErrorClassifier.resolveDependencyName(e);
        String message = e.getMessage() != null ? e.getMessage() : category.name();
        errorLog entry = new errorLog();
        entry.setTimestamp(System.currentTimeMillis());
        entry.setErrorType(category.name());
        entry.setMessage(message);
        entry.setService(service);
        entry.setOperation(operation);
        entry.setDependency(dependency);
        logs.add(entry);
        return entry;
    }

    public void clearRequestLoggingFlag() {
        loggedOnCurrentThread.remove();
    }

    public List<errorLog> getLogs() {
        return new ArrayList<>(logs);
    }

    public void clearLogs() {
        logs.clear();
    }

    @SuppressWarnings("unchecked")
    public static String extractOperation(com.Dummy.demo.service.externalDependency.model.externalRequest request) {
        if (request == null || request.getPayload() == null) {
            return null;
        }
        Object op = request.getPayload().get("operation");
        return op != null ? op.toString() : null;
    }
}

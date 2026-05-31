package com.Dummy.demo.monitoring.error;

import com.Dummy.demo.monitoring.error.model.ErrorCategory;

public final class ErrorClassifier {

    private ErrorClassifier() {
    }

    public static ErrorCategory classifyDependencyFailure(String dependency, Exception e) {
        String dep = dependency != null ? dependency.toUpperCase() : "";
        String msg = messageOf(e);

        if (isTimeout(msg)) {
            return ErrorCategory.DEPENDENCY_TIMEOUT;
        }

        return switch (dep) {
            case "DB" -> ErrorCategory.DB_FAILURE;
            case "PAYMENT" -> ErrorCategory.PAYMENT_FAILURE;
            case "API", "THIRD_PARTY" -> ErrorCategory.API_FAILURE;
            default -> ErrorCategory.DEPENDENCY_FAILURE;
        };
    }

    public static ErrorCategory classifyException(Exception e) {
        String msg = messageOf(e);

        if ("SYSTEM_DOWN".equals(msg)) {
            return ErrorCategory.SYSTEM_DOWN;
        }
        if (msg.startsWith("OPERATION_FAILED")) {
            return ErrorCategory.INTERNAL_FAILURE;
        }
        if (isTimeout(msg)) {
            return resolveDependencyTimeoutCategory(msg);
        }
        if (isDependencyMessage(msg)) {
            return resolveDependencyFailureCategory(msg);
        }
        return ErrorCategory.INTERNAL_FAILURE;
    }

    public static String resolveDependencyName(Exception e) {
        String msg = messageOf(e);
        if (msg.contains("DB") || msg.toLowerCase().contains("db")) {
            return "DB";
        }
        if (msg.contains("PAYMENT") || msg.contains("Payment")) {
            return "PAYMENT";
        }
        if (msg.contains("THIRD_PARTY") || msg.toLowerCase().contains("third-party")) {
            return "API";
        }
        return null;
    }

    private static ErrorCategory resolveDependencyTimeoutCategory(String msg) {
        if (msg.toLowerCase().contains("db")) {
            return ErrorCategory.DEPENDENCY_TIMEOUT;
        }
        if (msg.toLowerCase().contains("payment") || "Timeout".equals(msg)) {
            return ErrorCategory.DEPENDENCY_TIMEOUT;
        }
        if (msg.toLowerCase().contains("third-party")) {
            return ErrorCategory.DEPENDENCY_TIMEOUT;
        }
        return ErrorCategory.DEPENDENCY_TIMEOUT;
    }

    private static ErrorCategory resolveDependencyFailureCategory(String msg) {
        String upper = msg.toUpperCase();
        if (upper.contains("DB")) {
            return ErrorCategory.DB_FAILURE;
        }
        if (upper.contains("PAYMENT")) {
            return ErrorCategory.PAYMENT_FAILURE;
        }
        if (upper.contains("THIRD_PARTY") || upper.contains("THIRD-PARTY")) {
            return ErrorCategory.API_FAILURE;
        }
        return ErrorCategory.DEPENDENCY_FAILURE;
    }

    private static boolean isDependencyMessage(String msg) {
        String lower = msg.toLowerCase();
        return lower.contains("db")
                || lower.contains("payment")
                || lower.contains("third-party")
                || lower.contains("third_party")
                || lower.contains("circuit open")
                || lower.contains("rate limit")
                || lower.contains("service down")
                || lower.contains(" failed");
    }

    private static boolean isTimeout(String msg) {
        return msg.toLowerCase().contains("timeout");
    }

    private static String messageOf(Exception e) {
        return e.getMessage() != null ? e.getMessage() : "";
    }
}

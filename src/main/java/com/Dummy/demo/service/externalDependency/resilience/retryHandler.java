package com.Dummy.demo.service.externalDependency.resilience;

import java.util.function.Supplier;

public class retryHandler {
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 100;

    public <T> T execute(Supplier<T> action) {// takes in a codeblock/function,and returns what it returns

        int attempts = 0;

        while (true) {
            try {
                return action.get(); // try actual call
            } catch (Exception e) {
                attempts++;

                if (attempts > MAX_RETRIES) {
                    throw e; // give up
                }

                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}

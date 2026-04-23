package com.Dummy.demo.service.externalDependency.resilience;

import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.Dummy.demo.service.Simulation.SimulationToggle;

@Component
public class retryHandler {
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 100;
    @Autowired
    private SimulationToggle toggle;

    public <T> T execute(Supplier<T> action) {// takes in a codeblock/function,and returns what it returns
        if (!toggle.FailureisEnabled()) {
            return action.get(); // 👈 no retry
        }
        int attempts = 0;

        while (true) {
            try {
                return action.get(); // try actual call. If it returns an error,next line gonna catch it.
            } catch (Exception e) {
                attempts++;

                if (attempts > MAX_RETRIES) {
                    throw e; // give up
                }

                try {
                    Thread.sleep(RETRY_DELAY_MS);// real life external systems pe we need to wait,trying instantly gonna
                                                 // make their chances of failing increase
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}

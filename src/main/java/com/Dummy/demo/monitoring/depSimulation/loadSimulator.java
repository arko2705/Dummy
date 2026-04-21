package com.Dummy.demo.monitoring.depSimulation;

import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

@Component
public class loadSimulator {
    private AtomicInteger currRequests = new AtomicInteger(0);

    public void incrementLoad() {
        currRequests.incrementAndGet(); // it can also return a value,and can also be used in such a sense that it
                                        // doesnt throw an error when the value isnt stored.Doign currRRequests++ is not
                                        // multithread(req) safe. Its acting like incrememnt here
    }

    public void decrementLoad() {
        currRequests.decrementAndGet();
    }

    public int getCurrentLoad() {
        return currRequests.get();
    }
}

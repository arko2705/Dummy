package com.Dummy.demo.service;

import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;

@Service
public class ErrorSimulationService {
    private AtomicInteger currRequests = new AtomicInteger(0);

    public void incrementLoad() {
        currRequests.incrementAndGet(); // it can also return a value,and can also be used in such a sense that it
                                        // doesnt throw an error when the value isnt stored.Doign currRRequests++ is not
                                        // multithread(req) safe. Its acting like incrememnt here
    }

    public void decrementLoad() {
        currRequests.decrementAndGet();
    }

    public long simulateLatency(long delay) {
        int load = currRequests.get();
        System.out.println("Load: " + load);
        if (load > 50)
            return delay + 300; // ion wanna absolutely hardcode this,will get back to this.
        if (load > 20)
            return delay + 150;
        return delay; // Default latency
    }
}

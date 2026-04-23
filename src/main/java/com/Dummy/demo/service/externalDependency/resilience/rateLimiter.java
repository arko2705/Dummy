package com.Dummy.demo.service.externalDependency.resilience;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.Dummy.demo.service.Simulation.SimulationToggle;

@Component
public class rateLimiter {

    private static final int MAX_REQUESTS = 200;
    private static final long WINDOW_SECONDS = 10;
    @Autowired
    private SimulationToggle toggle;

    private static class RateLimitData {
        long currentWindowStart; // timestamp when this window started
        AtomicInteger count;

        RateLimitData(long currentWindowStart) {
            this.currentWindowStart = currentWindowStart;// when the current window starts
            this.count = new AtomicInteger(0);// how many requests there are in current window
        }
    }

    private ConcurrentHashMap<String, RateLimitData> map = new ConcurrentHashMap<>();

    public boolean allowRequest(String dependency) {
        if (!toggle.FailureisEnabled())
            return true; // 👈 always allow
        long now = System.currentTimeMillis() / 1000;
        long windowStart = (now / WINDOW_SECONDS) * WINDOW_SECONDS; // Doing 10 second windows. Since getting
                                                                    // divided by integer,it wont give a decimal value.
                                                                    // Its gonna be rounded down
        // if (!map.containsKey("DB")) {
        // map.put("DB", new RateLimitData(123));
        // }
        // RateLimitData data = map.get("DB");; So like apprently this is not
        // thread safe,doing the same below is atomic.
        RateLimitData data = map.computeIfAbsent(dependency, k -> new RateLimitData(windowStart));// map.computeIfAbsent
                                                                                                  // Ex:
                                                                                                  // searche skey "DB".
                                                                                                  // {k-> new
                                                                                                  // RateLimitData(123)
                                                                                                  // If key NOT found}
                                                                                                  // Returns existing OR
                                                                                                  // newly created one

        synchronized (data) {// synchronized(data) prevents 2 threads from messing up SAME dependency's
                             // counter at the SAME time.
            if (data.currentWindowStart != windowStart) {
                // New 10-second window started
                data.currentWindowStart = windowStart;
                data.count.set(0);
            }

            int count = data.count.incrementAndGet();
            return count <= MAX_REQUESTS;
        }
    }
}
// So basically rateLimitData is taken from map,checked if its currenWindowStart
// is same as the window,if not then counts incrememented and returned true if
// <=200 else not.If currentWindoeStart is not same,it means window changed and
// each dependency window and count has to be reset. A bit confusing but shld
// add up.
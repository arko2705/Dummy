package com.Dummy.demo.monitoring.dependency.service;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.stereotype.Service;

import com.Dummy.demo.monitoring.dependency.model.DependencyEvent;

@Service
public class DependencyMetricsService {

    private static final int MAX_SIZE = 500;
    private final Queue<DependencyEvent> recentEvents = new ConcurrentLinkedQueue<>();

    public void recordDependencyCall(String dependencyName, long latency, boolean success) {
        recentEvents.add(new DependencyEvent(dependencyName, latency, success, System.currentTimeMillis()));
        if (recentEvents.size() > MAX_SIZE) {
            recentEvents.poll();
        }
    }

    /** Returns a point-in-time copy for aggregation. */
    public List<DependencyEvent> getRecentEventsSnapshot() {
        return List.copyOf(recentEvents);
    }
}


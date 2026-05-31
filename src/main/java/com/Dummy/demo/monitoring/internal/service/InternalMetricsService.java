package com.Dummy.demo.monitoring.internal.service;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.stereotype.Service;

import com.Dummy.demo.monitoring.internal.model.InternalEvent;
import com.Dummy.demo.monitoring.internal.model.InternalEventType;

@Service
public class InternalMetricsService {

    private static final int MAX_SIZE = 500;
    private final Queue<InternalEvent> recentEvents = new ConcurrentLinkedQueue<>();

    public void recordEvent(InternalEventType eventType, String operation, String service) {
        recentEvents.add(new InternalEvent(eventType, System.currentTimeMillis(), operation, service));
        if (recentEvents.size() > MAX_SIZE) {
            recentEvents.poll();
        }
    }

    /** Returns a point-in-time copy for aggregation. */
    public List<InternalEvent> getRecentEventsSnapshot() {
        return List.copyOf(recentEvents);
    }
}

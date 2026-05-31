package com.Dummy.demo.monitoring.trend.service;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.stereotype.Service;

import com.Dummy.demo.monitoring.trend.model.MetricsHistoryEntry;

@Service
public class SnapshotHistoryService {

    static final int MAX_HISTORY = 30;

    private final Queue<MetricsHistoryEntry> history = new ConcurrentLinkedQueue<>();

    public void record(double avgLatency, double errorRate, double throughput) {
        history.add(new MetricsHistoryEntry(avgLatency, errorRate, throughput));
        while (history.size() > MAX_HISTORY) {
            history.poll();
        }
    }

    /** Returns a point-in-time copy for trend calculation. */
    public List<MetricsHistoryEntry> getHistorySnapshot() {
        return List.copyOf(history);
    }
}

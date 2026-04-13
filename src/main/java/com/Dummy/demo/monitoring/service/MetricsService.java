package com.Dummy.demo.monitoring.service;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.Dummy.demo.monitoring.model.RequestMetric;

@Service
public class MetricsService {

    private final Queue<RequestMetric> recentRequests = new ConcurrentLinkedQueue<>();// normal queue but thread safe

    private static final int MAX_SIZE = 500;

    // record new request
    public void recordRequest(String endpoint, long latency, int statusCode, boolean error) {

        RequestMetric metric = new RequestMetric(endpoint, latency, statusCode, error);
        recentRequests.add(metric);

        // maintain rolling window
        if (recentRequests.size() > MAX_SIZE) {
            recentRequests.poll(); // removes element at the front of the queue
        }
        System.out.println("Queue size: " + recentRequests.size());
    }

    // Metrics calculation

    public int getRequestCount() {
        return recentRequests.size();
    }

    // Stream is just a collection of objects on which you can directly call
    // multiple functions one after another to create a data processing pipeline
    public double getAverageLatency() {
        return recentRequests.stream() // .stream() converts collections to steam
                .mapToLong(RequestMetric::getLatency) // Basic syntax: ClassName::methodName.
                .average()
                .orElse(0.0);
    }

    public double getErrorRate() {
        if (recentRequests.isEmpty())
            return 0.0;

        long errorCount = recentRequests.stream()
                .filter(RequestMetric::isError)
                .count();

        return (double) errorCount / recentRequests.size();
    }

    public long getP95Latency() { // value below which 95%of the latencies fall
        if (recentRequests.isEmpty())
            return 0;

        List<Long> latencies = recentRequests.stream() // convert queue to stream
                .map(RequestMetric::getLatency) // get only latency value out of the RequestMetric object
                .sorted() // Sort in ascending order
                .collect(Collectors.toList()); // Convert stream to list

        int index = (int) (0.95 * latencies.size()); // Get the 95th percentile index
        return latencies.get(index);
    }
}
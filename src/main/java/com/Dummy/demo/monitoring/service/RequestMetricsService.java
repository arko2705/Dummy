package com.Dummy.demo.monitoring.service;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.Dummy.demo.monitoring.model.RequestMetric;
import com.Dummy.demo.service.nwMetrics;

import java.util.ArrayList;

@Service
public class RequestMetricsService {

    private final Queue<RequestMetric> recentRequests = new ConcurrentLinkedQueue<>();// normal queue but thread safe
    private final long systemStartTime = System.currentTimeMillis();
    private static final int MAX_SIZE = 500;
    List<Long> failureTimestamps = new ArrayList<>();
    List<Long> downtimeStartTimes = new ArrayList<>();
    List<Long> downtimeendTimes = new ArrayList<>();
    AtomicInteger totalRequests = new AtomicInteger(0);
    AtomicInteger failedRequests = new AtomicInteger(0);

    // record new request
    public void recordRequest(long startTime, long endTime, String endpoint, long latency, int statusCode,
            boolean error) {

        RequestMetric metric = new RequestMetric(startTime, endTime, endpoint, latency, statusCode, error);
        recentRequests.add(metric);

        // maintain rolling window
        if (recentRequests.size() > MAX_SIZE) {
            recentRequests.poll(); // removes element at the front of the queue
        }
        totalRequests.incrementAndGet();
        if (error) {
            failedRequests.incrementAndGet();
            failureTimestamps.add(System.currentTimeMillis());
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

    public long getErrorCount() {
        return recentRequests.stream()
                .filter(RequestMetric::isError)
                .count();
    }

    public double getErrorRate() {
        if (recentRequests.isEmpty())
            return 0.0;

        return (double) getErrorCount() / getRequestCount();
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

    public int getTotalRequests() {
        return totalRequests.get();
    }

    public int getFailedRequests() {
        return failedRequests.get();
    }

    public long getSystemsStartTime() {
        return systemStartTime;
    }
}
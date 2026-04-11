package com.Dummy.demo.monitoring.model;

public class RequestMetric {
	private String endpoint;
	private long latency;
	private int statusCode;
	private boolean error;
	private long timestamp;
	
	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public long getLatency() {
		return latency;
	}

	public void setLatency(long latency) {
		this.latency = latency;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	
	
	public RequestMetric(String endpoint, long latency, int statusCode, boolean error) {
        this.endpoint = endpoint;
        this.latency = latency;
        this.statusCode = statusCode;
        this.error = error;
        this.timestamp = System.currentTimeMillis();
    }

}

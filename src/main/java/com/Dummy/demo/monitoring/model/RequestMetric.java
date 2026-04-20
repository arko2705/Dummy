package com.Dummy.demo.monitoring.model;

public class RequestMetric {
	private String endpoint;
	private long latency;
	private int statusCode;
	private boolean error;
	private long startTime;
	private long endTime;
	private String errorType;

	public String getErrorType() {
		return errorType;
	}

	public void setErrorType(String errorType) {
		this.errorType = errorType;
	}

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

	public long getStartTime() {
		return startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public RequestMetric(long startTime, long endTime, String endpoint, long latency, int statusCode, boolean error) {
		this.endpoint = endpoint;
		this.latency = latency;
		this.statusCode = statusCode;
		this.error = error;
		this.startTime = startTime;
		this.endTime = endTime;
	}
}

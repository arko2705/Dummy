# Dummy App – System Monitoring & ML Integration

## Problem Statement

Modern distributed systems often lack:
- Real-time observability
- Early anomaly detection
- Proactive failure handling   

This project simulates a backend system that:
- Tracks request flow
- Collects runtime metrics
- Simulates failures
- Provides structured data for Machine Learning models

---

## Objective

To build a **mini intelligent observability system** that can:
- Monitor system behavior in real time
- Detect anomalies automatically
- Predict failures before they occur        (this part depends on how we plan on doing ml part)
- Simulate real-world production issues    

---

## System Architecture

The project is divided into three main layers:

### 1. Request Tracking Layer
Tracks each request through the system.

**Captured Data:**
(basic REST API to simulate emarketplace)
- Request ID
- Timestamps (start/end)
- Processing stages
- Completion status

---

### 2. Metrics Collection Layer
Collects system and request-level metrics.

**Metrics include:**
- Response time (latency)
- Throughput
- Error rate
- Active requests

### 3. Error Simulation Layer 
Simulates abnormal system behavior:
- Random failures
- Latency spikes
- Service degradation

**Purpose:**
- Generate realistic abnormal data
- Help train ML models

---

## ⚙️ How It Works

1. Client sends request → Controller  
2. Request is logged and assigned a unique ID  
3. Request passes through processing logic  
4. Metrics are recorded during execution  
5. Error simulation may introduce failures  
6. Request completes and final metrics are stored  
7. For now metrics are just logged in terminal. Can be modified easily to have an endpoint send back json response and convert to csv file

---

## Current Implementation

### Backend (Spring Boot)
- REST APIs implemented
- Request tracking system working
- Metrics collection implemented
- Logging enabled

### Data Generated
- Structured request logs
- Time-based metrics
- Success/failure records

---

## Work in Progress

- Error simulation layer
- Machine Learning integration

---

## Data Available for ML

The system provides structured data for ML models:

### Metrics Data
- Response time
- Error rate
- Request frequency
- Concurrent load

### Event Data
- Request lifecycle (start/end)
- Failures (simulated)
- Latency spikes

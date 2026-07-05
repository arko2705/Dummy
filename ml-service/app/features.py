"""
Feature extraction: flatten MetricsSnapshot into a fixed numeric vector.

The Spring Boot app sends nested JSON (endpoints, dependencies, trends).
ML models need a flat, consistent feature set — this module does that mapping.
"""

from typing import Dict, List, Tuple

import numpy as np

from app.schemas import MetricsSnapshot

# Known dependencies from the Java app's ErrorClassifier / fake clients
KNOWN_DEPENDENCIES = ("DB", "PAYMENT", "API", "THIRD_PARTY")

FEATURE_NAMES: List[str] = [
    "avgLatency",
    "p95Latency",
    "errorRate",
    "requestCount",
    "throughput",
    "maxEndpointLatency",
    "maxEndpointErrorRate",
    "worstEndpointRequestCount",
    "latencySlope",
    "errorSlope",
    "throughputSlope",
    "failureRate",
    "mtbf",
    "mttr",
    "availability",
    "processingDelayEvents",
    "stateInconsistencyEvents",
    "systemCrashEvents",
    "concurrencyEvents",
    *[
        f"dep_{dep.lower()}_{metric}"
        for dep in KNOWN_DEPENDENCIES
        for metric in ("latency", "failureRate", "requestCount")
    ],
]


def extract_features(snapshot: MetricsSnapshot) -> Tuple[np.ndarray, Dict[str, float]]:
    """
    Convert one MetricsSnapshot into a 1-D numpy array plus a name→value dict
    for explainability / root-cause rules.
    """
    endpoint_latencies = [m.avgLatency for m in snapshot.endpointMetrics.values()]
    endpoint_errors = [m.errorRate for m in snapshot.endpointMetrics.values()]
    endpoint_counts = [m.requestCount for m in snapshot.endpointMetrics.values()]

    dep_features: Dict[str, float] = {}
    for dep in KNOWN_DEPENDENCIES:
        metric = snapshot.dependencyMetrics.get(dep)
        prefix = f"dep_{dep.lower()}"
        dep_features[f"{prefix}_latency"] = metric.avgLatency if metric else 0.0
        dep_features[f"{prefix}_failureRate"] = metric.failureRate if metric else 0.0
        dep_features[f"{prefix}_requestCount"] = metric.requestCount if metric else 0.0

    named: Dict[str, float] = {
        "avgLatency": snapshot.avgLatency,
        "p95Latency": float(snapshot.p95Latency),
        "errorRate": snapshot.errorRate,
        "requestCount": float(snapshot.requestCount),
        "throughput": snapshot.throughput,
        "maxEndpointLatency": max(endpoint_latencies) if endpoint_latencies else 0.0,
        "maxEndpointErrorRate": max(endpoint_errors) if endpoint_errors else 0.0,
        "worstEndpointRequestCount": max(endpoint_counts) if endpoint_counts else 0.0,
        "latencySlope": snapshot.trendMetrics.latencySlope,
        "errorSlope": snapshot.trendMetrics.errorSlope,
        "throughputSlope": snapshot.trendMetrics.throughputSlope,
        "failureRate": snapshot.reliabilityMetrics.failureRate,
        "mtbf": snapshot.reliabilityMetrics.mtbf,
        "mttr": snapshot.reliabilityMetrics.mttr,
        "availability": snapshot.reliabilityMetrics.availability,
        "processingDelayEvents": float(snapshot.internalMetrics.processingDelayEvents),
        "stateInconsistencyEvents": float(snapshot.internalMetrics.stateInconsistencyEvents),
        "systemCrashEvents": float(snapshot.internalMetrics.systemCrashEvents),
        "concurrencyEvents": float(snapshot.internalMetrics.concurrencyEvents),
        **dep_features,
    }

    vector = np.array([named[name] for name in FEATURE_NAMES], dtype=np.float64)
    return vector, named


def worst_endpoint(snapshot: MetricsSnapshot) -> Tuple[str, float, float]:
    """Return (endpoint path, latency, errorRate) for the hottest failing endpoint."""
    if not snapshot.endpointMetrics:
        return "", 0.0, 0.0

    worst = max(
        snapshot.endpointMetrics.items(),
        key=lambda item: item[1].errorRate * 100 + item[1].avgLatency,
    )
    return worst[0], worst[1].avgLatency, worst[1].errorRate


def worst_dependency(snapshot: MetricsSnapshot) -> Tuple[str, float, float]:
    """Return (dependency name, latency, failureRate) for the slowest/failing dep."""
    if not snapshot.dependencyMetrics:
        return "", 0.0, 0.0

    worst = max(
        snapshot.dependencyMetrics.items(),
        key=lambda item: item[1].failureRate * 100 + item[1].avgLatency,
    )
    return worst[0], worst[1].avgLatency, worst[1].failureRate

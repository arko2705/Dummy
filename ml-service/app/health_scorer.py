"""
System health scoring: map metrics + anomalies into a 0–100 score and status.

Uses weighted penalties — no ML model needed for the score itself. This gives
a human-readable "how bad is it?" number that dashboards can show immediately.
"""

from typing import Dict, List, Tuple

from app.config import HEALTH_OK, HEALTH_WARNING
from app.schemas import MetricsSnapshot


def compute_health_score(
    named_features: Dict[str, float],
    anomalies: List[str],
) -> Tuple[int, str]:
    """
    Returns (healthScore 0–100, status HEALTHY|WARNING|CRITICAL).
    Starts at 100 and subtracts penalties for bad signals.
    """
    score = 100.0

    # Error rate: 0–1 scale, each 1% error costs ~2 points
    score -= named_features.get("errorRate", 0.0) * 200

    # Latency: penalize high p95 (ms); 500ms p95 → -15 points
    p95 = named_features.get("p95Latency", 0.0)
    score -= min(25, p95 / 500 * 15)

    # Availability drop
    availability = named_features.get("availability", 1.0)
    score -= (1.0 - availability) * 40

    # Negative trends (degradation incoming)
    latency_slope = named_features.get("latencySlope", 0.0)
    error_slope = named_features.get("errorSlope", 0.0)
    if latency_slope > 0:
        score -= min(15, latency_slope * 50)
    if error_slope > 0:
        score -= min(20, error_slope * 100)

    # Internal failure events
    score -= named_features.get("systemCrashEvents", 0.0) * 5
    score -= named_features.get("stateInconsistencyEvents", 0.0) * 3
    score -= named_features.get("processingDelayEvents", 0.0) * 2

    # Dependency pain
    for dep in ("db", "payment", "api", "third_party"):
        score -= named_features.get(f"dep_{dep}_failureRate", 0.0) * 30
        dep_lat = named_features.get(f"dep_{dep}_latency", 0.0)
        score -= min(10, dep_lat / 1000 * 10)

    # Each statistically anomalous feature costs a bit more
    score -= len(anomalies) * 3

    score = int(max(0, min(100, round(score))))

    if score >= HEALTH_OK:
        status = "HEALTHY"
    elif score >= HEALTH_WARNING:
        status = "WARNING"
    else:
        status = "CRITICAL"

    return score, status

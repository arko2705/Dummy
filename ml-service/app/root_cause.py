"""
Rule-based root cause hinting (Phase 1).

Inspects the snapshot and flagged anomalies to produce a human-readable reason
and short prediction — no LLM, just ordered rules mirroring ops intuition.
"""

from typing import List, Tuple

from app.features import worst_dependency, worst_endpoint
from app.schemas import MetricsSnapshot


def infer_root_cause(
    snapshot: MetricsSnapshot,
    anomalies: List[str],
    status: str,
) -> Tuple[str, str, List[str]]:
    """
    Returns (reason, prediction, topContributors).
    Rules are evaluated top-down; first strong match wins for reason.
    """
    contributors: List[str] = []
    endpoint, ep_lat, ep_err = worst_endpoint(snapshot)
    dep, dep_lat, dep_fail = worst_dependency(snapshot)
    trends = snapshot.trendMetrics
    internal = snapshot.internalMetrics

    if internal.systemCrashEvents > 0:
        contributors.append(f"systemCrashEvents={internal.systemCrashEvents}")
        return (
            "Internal system crash events detected",
            "Service instability likely; check simulated crash endpoints",
            contributors,
        )

    if dep == "DB" and (dep_lat > 200 or dep_fail > 0.05 or "dep_db_latency" in anomalies):
        contributors.append(f"DB latency={dep_lat:.0f}ms failureRate={dep_fail:.2%}")
        prediction = "Possible degradation incoming" if trends.latencySlope > 0 else "Database bottleneck active"
        return "Database latency increasing", prediction, contributors

    if dep in ("PAYMENT", "API", "THIRD_PARTY") and (dep_lat > 300 or dep_fail > 0.05):
        contributors.append(f"{dep} latency={dep_lat:.0f}ms failureRate={dep_fail:.2%}")
        return (
            f"External dependency {dep} degraded",
            "Third-party or payment gateway delays may cascade to user-facing errors",
            contributors,
        )

    if endpoint and ep_err > 0.1:
        contributors.append(f"endpoint={endpoint} errorRate={ep_err:.2%}")
        return (
            f"Endpoint {endpoint} showing elevated errors",
            "Localized failure pattern; isolate this API path",
            contributors,
        )

    if trends.errorSlope > 0.01:
        contributors.append(f"errorSlope={trends.errorSlope:.4f}")
        return (
            "Error rate trending upward",
            "Increasing instability; failure rate may continue to rise",
            contributors,
        )

    if trends.latencySlope > 1.0 or snapshot.p95Latency > 500:
        contributors.append(f"latencySlope={trends.latencySlope:.2f} p95={snapshot.p95Latency}ms")
        return (
            "Latency spike or gradual slowdown detected",
            "Possible degradation incoming",
            contributors,
        )

    if internal.stateInconsistencyEvents > 0:
        contributors.append(f"stateInconsistencyEvents={internal.stateInconsistencyEvents}")
        return (
            "Data consistency anomalies detected",
            "Stale or duplicated responses may affect users",
            contributors,
        )

    if snapshot.errorRate > 0.05:
        contributors.append(f"errorRate={snapshot.errorRate:.2%}")
        return (
            "Elevated global error rate",
            "Monitor dependency and endpoint metrics for spread",
            contributors,
        )

    if anomalies:
        contributors.extend(anomalies[:3])
        return (
            f"Anomalous metrics: {', '.join(anomalies[:3])}",
            "Statistical deviation from recent baseline",
            contributors,
        )

    if status == "HEALTHY":
        return "All metrics within normal range", "System operating normally", []

    return (
        "Mixed degradation signals across metrics",
        "Continue monitoring; no single dominant cause yet",
        contributors,
    )

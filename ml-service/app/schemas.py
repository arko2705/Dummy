"""Pydantic models mirroring the Spring Boot MetricsSnapshot JSON contract."""

from typing import Dict, List, Literal, Optional

from pydantic import BaseModel, Field


class EndpointMetric(BaseModel):
    avgLatency: float = 0.0
    errorRate: float = 0.0
    requestCount: int = 0


class DependencyMetric(BaseModel):
    avgLatency: float = 0.0
    failureRate: float = 0.0
    requestCount: int = 0


class InternalMetrics(BaseModel):
    processingDelayEvents: int = 0
    stateInconsistencyEvents: int = 0
    systemCrashEvents: int = 0
    concurrencyEvents: int = 0


class TrendMetrics(BaseModel):
    latencySlope: float = 0.0
    errorSlope: float = 0.0
    throughputSlope: float = 0.0


class ReliabilityMetrics(BaseModel):
    failureRate: float = 0.0
    mtbf: float = 0.0
    mttr: float = 0.0
    availability: float = 1.0


class MetricsSnapshot(BaseModel):
    """Single aggregated metrics object produced every 5s by MetricsAggregator."""

    avgLatency: float = 0.0
    errorRate: float = 0.0
    p95Latency: int = 0
    requestCount: int = 0
    throughput: float = 0.0
    endpointMetrics: Dict[str, EndpointMetric] = Field(default_factory=dict)
    dependencyMetrics: Dict[str, DependencyMetric] = Field(default_factory=dict)
    internalMetrics: InternalMetrics = Field(default_factory=InternalMetrics)
    trendMetrics: TrendMetrics = Field(default_factory=TrendMetrics)
    reliabilityMetrics: ReliabilityMetrics = Field(default_factory=ReliabilityMetrics)


class HealthInsight(BaseModel):
    """Phase 1 ML output returned to callers."""

    status: Literal["HEALTHY", "WARNING", "CRITICAL"]
    healthScore: int = Field(ge=0, le=100)
    reason: str
    prediction: str
    anomalies: List[str] = Field(default_factory=list)
    confidence: float = Field(ge=0.0, le=1.0)
    topContributors: List[str] = Field(default_factory=list)

"""
Inference pipeline: ties feature extraction, anomaly detection, scoring, and
root-cause rules into one call used by both POST /infer and the poller.
"""

from app.anomaly import AnomalyDetector
from app.features import extract_features
from app.health_scorer import compute_health_score
from app.root_cause import infer_root_cause
from app.schemas import HealthInsight, MetricsSnapshot

# Shared detector instance — baseline grows as snapshots arrive
_detector = AnomalyDetector()


def run_inference(snapshot: MetricsSnapshot) -> HealthInsight:
    vector, named = extract_features(snapshot)
    anomaly_result = _detector.detect(vector)
    health_score, status = compute_health_score(named, anomaly_result.anomalies)
    reason, prediction, contributors = infer_root_cause(
        snapshot, anomaly_result.anomalies, status
    )

    # Feed this snapshot into the baseline for the next call
    _detector.observe(vector)

    return HealthInsight(
        status=status,
        healthScore=health_score,
        reason=reason,
        prediction=prediction,
        anomalies=anomaly_result.anomalies,
        confidence=round(anomaly_result.confidence, 2),
        topContributors=contributors,
    )


def get_baseline_size() -> int:
    return _detector.sample_count

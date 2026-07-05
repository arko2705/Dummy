"""
Unsupervised anomaly detection (Phase 1 — no deep learning).

Maintains a rolling baseline from recent snapshots and flags features whose
Z-score exceeds a threshold. Once enough samples exist, also runs Isolation
Forest for multivariate outlier detection.
"""

from collections import deque
from dataclasses import dataclass, field
from typing import Deque, Dict, List, Optional, Tuple

import numpy as np
from sklearn.ensemble import IsolationForest

from app.config import BASELINE_WINDOW, ZSCORE_THRESHOLD
from app.features import FEATURE_NAMES


@dataclass
class AnomalyResult:
    anomalies: List[str] = field(default_factory=list)
    z_scores: Dict[str, float] = field(default_factory=dict)
    isolation_score: Optional[float] = None
    confidence: float = 0.5


class AnomalyDetector:
    """Rolling-window Z-score + optional Isolation Forest."""

    def __init__(self, window_size: int = BASELINE_WINDOW, z_threshold: float = ZSCORE_THRESHOLD):
        self.window_size = window_size
        self.z_threshold = z_threshold
        self._history: Deque[np.ndarray] = deque(maxlen=window_size)
        self._forest: Optional[IsolationForest] = None

    @property
    def sample_count(self) -> int:
        return len(self._history)

    def observe(self, vector: np.ndarray) -> None:
        """Add a snapshot to the rolling baseline (called after each inference)."""
        self._history.append(vector.copy())

    def detect(self, vector: np.ndarray) -> AnomalyResult:
        if len(self._history) < 10:
            return AnomalyResult(confidence=0.3)

        matrix = np.vstack(self._history)
        means = matrix.mean(axis=0)
        stds = matrix.std(axis=0)
        stds = np.where(stds < 1e-9, 1.0, stds)

        z_scores = (vector - means) / stds
        anomalies: List[str] = []
        z_map: Dict[str, float] = {}

        for i, name in enumerate(FEATURE_NAMES):
            z = float(z_scores[i])
            z_map[name] = round(z, 3)
            if abs(z) >= self.z_threshold:
                anomalies.append(name)

        isolation_score: Optional[float] = None
        confidence = min(0.95, 0.4 + len(self._history) / self.window_size * 0.5)

        if len(self._history) >= 30:
            self._forest = IsolationForest(
                contamination=0.1,
                random_state=42,
                n_estimators=100,
            )
            self._forest.fit(matrix)
            score = self._forest.decision_function(vector.reshape(1, -1))[0]
            isolation_score = float(score)
            if self._forest.predict(vector.reshape(1, -1))[0] == -1 and not anomalies:
                # Multivariate outlier with no single extreme feature
                top_idx = int(np.argmax(np.abs(z_scores)))
                anomalies.append(FEATURE_NAMES[top_idx])
                confidence = min(0.9, confidence + 0.1)

        return AnomalyResult(
            anomalies=anomalies,
            z_scores=z_map,
            isolation_score=isolation_score,
            confidence=confidence,
        )

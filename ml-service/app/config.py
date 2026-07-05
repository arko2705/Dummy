import os

# Spring Boot metrics endpoint (pull mode)
METRICS_URL: str = os.getenv("METRICS_URL", "http://localhost:8080/api/metrics")

# How often the background poller fetches snapshots (seconds)
POLL_INTERVAL_SEC: float = float(os.getenv("POLL_INTERVAL_SEC", "5"))

# Rolling window size for baseline / anomaly detection
BASELINE_WINDOW: int = int(os.getenv("BASELINE_WINDOW", "60"))

# Z-score threshold above which a feature is flagged anomalous
ZSCORE_THRESHOLD: float = float(os.getenv("ZSCORE_THRESHOLD", "2.5"))

# Health score thresholds
HEALTH_OK: int = int(os.getenv("HEALTH_OK", "80"))
HEALTH_WARNING: int = int(os.getenv("HEALTH_WARNING", "60"))

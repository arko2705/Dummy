"""
Background metrics poller (pull integration).

Fetches GET /api/metrics from Spring Boot every POLL_INTERVAL_SEC seconds,
runs inference, and stores the latest result for GET /latest on the ML service.
"""

import asyncio
import logging
import time
from typing import Optional

import httpx

from app.config import METRICS_URL, POLL_INTERVAL_SEC
from app.pipeline import run_inference
from app.schemas import HealthInsight, MetricsSnapshot

logger = logging.getLogger(__name__)

_latest_insight: Optional[HealthInsight] = None
_last_error: Optional[str] = None
_poll_count: int = 0
_failed_poll_count: int = 0
_last_success_at: Optional[float] = None


def get_latest_insight() -> Optional[HealthInsight]:
    return _latest_insight


def get_poll_stats() -> dict:
    return {
        "pollCount": _poll_count,
        "failedPollCount": _failed_poll_count,
        "lastSuccessAt": _last_success_at,
        "lastError": _last_error,
        "hasInsight": _latest_insight is not None,
    }


async def poll_metrics_loop() -> None:
    """Runs forever while the FastAPI app is up."""
    global _latest_insight, _last_error, _poll_count, _failed_poll_count, _last_success_at

    logger.info("Starting metrics poller → %s every %.0fs", METRICS_URL, POLL_INTERVAL_SEC)

    async with httpx.AsyncClient(timeout=10.0) as client:
        while True:
            try:
                response = await client.get(METRICS_URL)
                response.raise_for_status()
                snapshot = MetricsSnapshot.model_validate(response.json())
                _latest_insight = run_inference(snapshot)
                _last_error = None
                _poll_count += 1
                _last_success_at = time.time()
                logger.info(
                    "Poll #%d → status=%s score=%d reason=%s",
                    _poll_count,
                    _latest_insight.status,
                    _latest_insight.healthScore,
                    _latest_insight.reason,
                )
            except httpx.HTTPError as exc:
                _last_error = str(exc)
                _failed_poll_count += 1
                logger.warning("Metrics poll failed: %s", exc)
            except Exception as exc:
                _last_error = str(exc)
                _failed_poll_count += 1
                logger.exception("Unexpected poll error: %s", exc)

            await asyncio.sleep(POLL_INTERVAL_SEC)

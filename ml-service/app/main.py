"""
FastAPI entry point for the Phase 1 ML service.

Endpoints:
  POST /infer   — push a MetricsSnapshot JSON (for Spring Boot push mode later)
  GET  /latest  — last result from the background poller (pull mode)
  GET  /health  — ML service liveness + baseline stats
"""

import asyncio
import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI, HTTPException

from app.collector import get_latest_insight, get_poll_stats, poll_metrics_loop
from app.pipeline import get_baseline_size, run_inference
from app.schemas import HealthInsight, MetricsSnapshot

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(name)s %(message)s")


@asynccontextmanager
async def lifespan(app: FastAPI):
    # Start pulling metrics from Spring Boot in the background
    task = asyncio.create_task(poll_metrics_loop())
    yield
    task.cancel()
    try:
        await task
    except asyncio.CancelledError:
        pass


app = FastAPI(
    title="Dummy App ML Service",
    description="Phase 1 real-time health inference on MetricsSnapshot",
    version="0.1.0",
    lifespan=lifespan,
)


@app.post("/infer", response_model=HealthInsight)
def infer(snapshot: MetricsSnapshot) -> HealthInsight:
    """
    Run inference on a single MetricsSnapshot.

    Use this when Spring Boot pushes snapshots, or for manual/testing calls.
    Same pipeline as the background poller.
    """
    return run_inference(snapshot)


@app.get("/latest", response_model=HealthInsight)
def latest() -> HealthInsight:
    """Return the most recent insight from the background poller."""
    insight = get_latest_insight()
    if insight is None:
        raise HTTPException(
            status_code=503,
            detail="No insight yet — ensure Spring Boot is running on :8080 and /api/metrics is reachable",
        )
    return insight


@app.get("/health")
def health() -> dict:
    stats = get_poll_stats()
    return {
        "service": "ml-service",
        "status": "up",
        "baselineSamples": get_baseline_size(),
        **stats,
    }

# ML Service — Phase 1

Real-time system health inference on top of the Spring Boot `MetricsSnapshot`.

## Architecture

```
Spring Boot (MetricsAggregator, every 5s)
        │
        ▼  GET /api/metrics
   ml-service poller
        │
        ├── feature extraction  (app/features.py)
        ├── anomaly detection   (app/anomaly.py)   — Z-score + Isolation Forest
        ├── health scoring      (app/health_scorer.py)
        └── root cause hints    (app/root_cause.py)
        │
        ▼
   HealthInsight JSON  →  GET /latest or POST /infer response
```

## Quick start

### 1. Start Spring Boot

```bash
./mvnw spring-boot:run
```

Generate traffic (optional): run k6 `test.js` or hit marketplace APIs.

### 2. Install and run ML service

```bash
cd ml-service
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8000
```

### 3. Check results

```bash
# ML service health
curl http://localhost:8000/health

# Latest inference (after ~5s warmup)
curl http://localhost:8000/latest

# Manual push (same JSON as /api/metrics)
curl -X POST http://localhost:8000/infer \
  -H "Content-Type: application/json" \
  -d @sample_snapshot.json
```

## Endpoints

| Method | Path | Purpose |
|--------|------|---------|
| `POST` | `/infer` | Run inference on a `MetricsSnapshot` body |
| `GET` | `/latest` | Last poller result |
| `GET` | `/health` | Service status + baseline sample count |

## Collect training data

```bash
source .venv/bin/activate
python scripts/collect_snapshots.py --duration 600 --output data/snapshots.jsonl
```

Run with simulations on and under load for realistic patterns.

## Configuration (env vars)

| Variable | Default | Description |
|----------|---------|-------------|
| `METRICS_URL` | `http://localhost:8080/api/metrics` | Spring Boot metrics endpoint |
| `POLL_INTERVAL_SEC` | `5` | Poller interval (matches aggregator) |
| `BASELINE_WINDOW` | `60` | Rolling window for anomaly baseline |
| `ZSCORE_THRESHOLD` | `2.5` | Feature flagged if \|z\| exceeds this |

## Example output

```json
{
  "status": "WARNING",
  "healthScore": 68,
  "reason": "Database latency increasing",
  "prediction": "Possible degradation incoming",
  "anomalies": ["dep_db_latency", "p95Latency"],
  "confidence": 0.72,
  "topContributors": ["DB latency=450ms failureRate=8.00%"]
}
```

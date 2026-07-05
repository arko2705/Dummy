#!/usr/bin/env python3
"""
Offline data collection script for training / evaluation.

Polls Spring Boot /api/metrics and appends each snapshot as one JSON line.
Run while simulations and k6 load are active to build a labeled-ish dataset.

Usage:
  python scripts/collect_snapshots.py --duration 300 --output data/snapshots.jsonl
"""

import argparse
import json
import sys
import time
from pathlib import Path

import httpx

DEFAULT_URL = "http://localhost:8080/api/metrics"


def main() -> int:
    parser = argparse.ArgumentParser(description="Collect MetricsSnapshot JSONL")
    parser.add_argument("--url", default=DEFAULT_URL)
    parser.add_argument("--interval", type=float, default=5.0)
    parser.add_argument("--duration", type=int, default=120, help="seconds to collect")
    parser.add_argument("--output", type=Path, default=Path("data/snapshots.jsonl"))
    args = parser.parse_args()

    args.output.parent.mkdir(parents=True, exist_ok=True)
    deadline = time.time() + args.duration
    count = 0

    print(f"Collecting from {args.url} for {args.duration}s → {args.output}")

    with httpx.Client(timeout=10.0) as client, args.output.open("a") as out:
        while time.time() < deadline:
            try:
                resp = client.get(args.url)
                resp.raise_for_status()
                record = {"collectedAt": int(time.time() * 1000), "snapshot": resp.json()}
                out.write(json.dumps(record) + "\n")
                out.flush()
                count += 1
                print(f"  [{count}] ok")
            except httpx.HTTPError as exc:
                print(f"  error: {exc}", file=sys.stderr)
            time.sleep(args.interval)

    print(f"Done. {count} snapshots written to {args.output}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

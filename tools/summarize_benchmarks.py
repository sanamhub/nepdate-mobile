#!/usr/bin/env python3
"""Prints a Markdown table from Macrobenchmark benchmarkData.json files.

One row per test and metric: minimum, median and maximum over the iterations. Frame timing is
reported by percentile (P50, P90, P95, P99); the benchmark gives each percentile as one number, so
that number fills the median column and the minimum and maximum are left out. A percentile that
carries the raw samples instead is summarised across them.
"""

import json
import statistics
import sys


def stat(entry):
    if isinstance(entry, (int, float)):
        return "-", f"{entry:.1f}", "-"
    if isinstance(entry, list) and entry:
        values = sorted(entry)
        return f"{values[0]:.1f}", f"{statistics.median(values):.1f}", f"{values[-1]:.1f}"
    if isinstance(entry, dict) and {"minimum", "median", "maximum"} <= set(entry):
        return f"{entry['minimum']:.1f}", f"{entry['median']:.1f}", f"{entry['maximum']:.1f}"
    raise SystemExit(f"a metric is in an unexpected shape: {entry!r}")


def main(paths):
    rows = []
    for path in paths:
        with open(path, encoding="utf-8") as handle:
            data = json.load(handle)
        for bench in data.get("benchmarks", []):
            name = bench["name"]
            for metric, entry in sorted(bench.get("metrics", {}).items()):
                rows.append((name, metric, *stat(entry)))
            for metric, percentiles in sorted(bench.get("sampledMetrics", {}).items()):
                for percentile, entry in sorted(percentiles.items()):
                    rows.append((name, f"{metric} {percentile}", *stat(entry)))
    print("| Test | Metric | Min | Median | Max |")
    print("|---|---|---|---|---|")
    for row in rows:
        print("| " + " | ".join(row) + " |")


if __name__ == "__main__":
    main(sys.argv[1:])

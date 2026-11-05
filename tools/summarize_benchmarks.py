#!/usr/bin/env python3
"""Prints a Markdown table from Macrobenchmark benchmarkData.json files.

One row per test and metric: minimum, median and maximum over the iterations. Frame timing is
reported by percentile (P50, P90, P95, P99), each with its median and worst value over the
iterations.
"""

import json
import sys


def stat(entry):
    return f"{entry['minimum']:.1f}", f"{entry['median']:.1f}", f"{entry['maximum']:.1f}"


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

#!/usr/bin/env python3
"""Prints a Markdown table from Macrobenchmark benchmarkData.json files.

One row per test and metric: minimum, median and maximum. A metric that the benchmark already
summarised (the `metrics` block) is printed as it is. A sampled metric (the `sampledMetrics` block,
one entry per percentile) is printed from the sample or samples it carries: a single number fills the
median column, and a list, however deeply nested, is summarised with `statistics`. A value the script
does not recognise prints `?` in every column instead of failing the benchmark job, so a schema change
is visible in the table rather than as a red build.
"""

import json
import statistics
import sys


def numbers(entry):
    """Every number inside [entry], flattening lists and plain dictionaries."""
    if isinstance(entry, bool):
        return []
    if isinstance(entry, (int, float)):
        return [entry]
    if isinstance(entry, dict):
        found = []
        for value in entry.values():
            found.extend(numbers(value))
        return found
    if isinstance(entry, (list, tuple)):
        found = []
        for value in entry:
            found.extend(numbers(value))
        return found
    return []


def summary(values):
    ordered = sorted(values)
    return (
        f"{ordered[0]:.1f}",
        f"{statistics.median(ordered):.1f}",
        f"{ordered[-1]:.1f}",
    )


def stat(entry):
    if isinstance(entry, dict) and {"minimum", "median", "maximum"} <= set(entry):
        return (
            f"{entry['minimum']:.1f}",
            f"{entry['median']:.1f}",
            f"{entry['maximum']:.1f}",
        )
    values = numbers(entry)
    if not values:
        return "?", "?", "?"
    if len(values) == 1:
        return "-", f"{values[0]:.1f}", "-"
    return summary(values)


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

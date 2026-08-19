import os
import sys
import unittest

here = os.path.dirname(__file__)
if here not in sys.path:
    sys.path.insert(0, here)

from generate_benchmark_summary import (
    comparison_status,
    generate_markdown,
)


def result(
    name="io.prometheus.metrics.benchmarks.CounterBenchmark.prometheusInc",
    score=100.0,
    error=1.0,
    threads=4,
):
    primary_metric = {
        "score": score,
        "scoreUnit": "ops/s",
    }
    if error is not None:
        primary_metric.update(
            {
                "scoreError": error,
                "scoreConfidence": [score - error, score + error],
            }
        )

    return {
        "benchmark": name,
        "jmhVersion": "1.37",
        "mode": "thrpt",
        "threads": threads,
        "forks": 3,
        "warmupIterations": 3,
        "warmupTime": "10 s",
        "measurementIterations": 5,
        "measurementTime": "10 s",
        "jdkVersion": "25.0.3",
        "primaryMetric": primary_metric,
    }


class TestBenchmarkComparison(unittest.TestCase):
    def test_meaningful_improvement_requires_threshold_and_separation(self):
        self.assertEqual(
            comparison_status(result(score=106), result()), "meaningful improvement"
        )

    def test_meaningful_regression_requires_threshold_and_separation(self):
        self.assertEqual(
            comparison_status(result(score=94), result()), "meaningful regression"
        )

    def test_small_or_uncertain_change_is_within_noise(self):
        self.assertEqual(
            comparison_status(result(score=102, error=5), result(error=5)),
            "within noise",
        )

    def test_mismatched_metadata_is_inconclusive(self):
        self.assertEqual(comparison_status(result(threads=1), result()), "inconclusive")

    def test_execution_metadata_mismatch_is_inconclusive(self):
        for field, value in (
            ("vmName", "different-vm"),
            ("vmVersion", "different-vm-version"),
            ("jvmArgs", ["-Xmx1g"]),
            ("jvmArgsPrepend", ["-XX:+UseZGC"]),
            ("jvmArgsAppend", ["-XX:-UseCompressedOops"]),
            ("warmupBatchSize", 2),
            ("measurementBatchSize", 2),
        ):
            head = result()
            base = result()
            head[field] = value
            self.assertEqual(comparison_status(head, base), "inconclusive", field)

    def test_latency_uses_lower_is_better_direction(self):
        head = result(score=94, error=1)
        base = result(score=100, error=1)
        head["mode"] = base["mode"] = "avgt"
        head["primaryMetric"]["scoreUnit"] = base["primaryMetric"]["scoreUnit"] = "ms"
        self.assertEqual(comparison_status(head, base), "meaningful improvement")

    def test_exact_practical_threshold_is_meaningful(self):
        self.assertEqual(
            comparison_status(result(score=105, error=0.1), result(error=0.1)),
            "meaningful improvement",
        )

    def test_zero_or_non_finite_scores_are_inconclusive(self):
        self.assertEqual(comparison_status(result(score=0), result()), "inconclusive")
        self.assertEqual(comparison_status(result(), result(score=0)), "inconclusive")
        self.assertEqual(
            comparison_status(result(score=float("inf")), result()), "inconclusive"
        )

    def test_missing_confidence_interval_is_inconclusive(self):
        head = result(score=106, error=None)
        self.assertEqual(comparison_status(head, result()), "inconclusive")


class TestBenchmarkMarkdown(unittest.TestCase):
    def test_head_only_benchmarks_are_separate_and_not_ranked(self):
        base = [result()]
        head_only = result(
            name="io.prometheus.metrics.benchmarks.HistogramBenchmark.newProbe",
            score=5.0,
        )
        markdown = generate_markdown(
            base + [head_only],
            "head",
            "prometheus/client_java",
            baseline_results=base,
            baseline_sha="base",
            baseline_repo="prometheus/client_java",
        )

        self.assertIn(
            "| Benchmark | PR | Base | Head vs base | Regression verdict |", markdown
        )
        self.assertIn("## New benchmarks in PR head", markdown)
        self.assertIn("no base counterpart", markdown)
        self.assertIn("Throughput scores are higher-is-better", markdown)
        self.assertNotIn("Within run", markdown)
        self.assertNotIn("x slower", markdown)

    def test_latency_note_is_mode_aware(self):
        base = result()
        head = result()
        base["mode"] = head["mode"] = "avgt"
        base["primaryMetric"]["scoreUnit"] = head["primaryMetric"]["scoreUnit"] = "ms"
        markdown = generate_markdown(
            [head],
            "head",
            "prometheus/client_java",
            baseline_results=[base],
            baseline_sha="base",
            baseline_repo="prometheus/client_java",
        )
        self.assertIn("Latency scores are lower-is-better", markdown)


if __name__ == "__main__":
    unittest.main()

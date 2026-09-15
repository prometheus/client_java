import re
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
WORKFLOW = (ROOT / ".github/workflows/pr-benchmarks.yml").read_text()
PATTERNS = {
    topic: pattern.strip("'\"")
    for topic, pattern in re.findall(r"- topic: (\w+)\s+pattern: ([^\n]+)", WORKFLOW)
}
PACKAGE = "io.prometheus.metrics.benchmarks."


class TestPrBenchmarkSelection(unittest.TestCase):
    def test_only_client_java_counter_and_histogram_methods_are_selected(self):
        for topic, class_name in (
            ("counter", "CounterBenchmark"),
            ("histogram", "HistogramBenchmark"),
        ):
            source = (
                ROOT
                / "benchmarks/src/main/java/io/prometheus/metrics/benchmarks"
                / f"{class_name}.java"
            ).read_text()
            methods = re.findall(
                r"@Benchmark\s+@Threads\(\d+\)\s+public \S+ (\w+)\(", source
            )
            self.assertTrue(methods)
            for method in methods:
                with self.subTest(method=method):
                    selected = re.search(
                        PATTERNS[topic], PACKAGE + class_name + "." + method
                    )
                    self.assertEqual(
                        selected is not None, method.startswith("prometheus")
                    )

    def test_external_systems_including_future_bound_instruments_are_excluded(self):
        for class_name in ("CounterBenchmark", "HistogramBenchmark"):
            for method in (
                "openTelemetryAdd",
                "openTelemetryBoundInc",
                "openTelemetryBoundClassic",
                "codahaleIncNoLabels",
                "simpleclientAdd",
                "simpleclient",
            ):
                name = PACKAGE + class_name + "." + method
                self.assertFalse(
                    any(re.search(p, name) for p in PATTERNS.values()), name
                )

    def test_all_lookup_and_cached_variants_are_selected(self):
        for method in (
            "prometheusLabelValuesInc",
            "prometheusLabelValuesIncSingleThread",
            "prometheusCachedLabelValuesInc",
            "prometheusCachedLabelValuesIncSingleThread",
        ):
            self.assertRegex(
                PACKAGE + "CounterBenchmark." + method, PATTERNS["counter"]
            )

    def test_exposition_keeps_openmetrics_and_prometheus_formats(self):
        for class_name in ("HistogramTextFormatBenchmark", "TextFormatUtilBenchmark"):
            for method in ("openMetricsWriteToNull", "prometheusWriteToNull"):
                self.assertRegex(
                    PACKAGE + class_name + "." + method, PATTERNS["exposition"]
                )

    def test_base_and_head_use_the_same_selection(self):
        self.assertIn("JMH_PATTERN: ${{ matrix.pattern }}", WORKFLOW)
        self.assertEqual(
            WORKFLOW.count("JMH_ARGS: -f 3 -wi 3 -i 5 ${{ env.JMH_PATTERN }}"), 2
        )


if __name__ == "__main__":
    unittest.main()

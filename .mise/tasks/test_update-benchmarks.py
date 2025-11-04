import os
import re
import sys
import tempfile
import unittest

from update_benchmarks import update_pre_blocks_under_module

# Ensure the tasks directory is importable when running the test directly
here = os.path.dirname(__file__)
if here not in sys.path:
    sys.path.insert(0, here)


class TestRunBenchmarksFiltering(unittest.TestCase):
    def setUp(self):
        # sample JMH table with mixed-class lines
        self.table = (
            "Benchmark                                             Mode  Cnt       Score   Error  Units\n"
            "CounterBenchmark.codahaleIncNoLabels                 thrpt        57881.585          ops/s\n"
            "HistogramBenchmark.prometheusNative                  thrpt         2385.134          ops/s\n"
            "TextFormatUtilBenchmark.prometheusWriteToNull        thrpt       885331.328          ops/s\n"
            "CounterBenchmark.prometheusInc                       thrpt        54090.469          ops/s\n"
        )

        # create temp dir to act as module path
        self.tmpdir = tempfile.TemporaryDirectory()
        self.module_path = self.tmpdir.name

        # Create three files with a javadoc <pre> block that contains mixed results
        self.files = {}
        javadoc_pre = (
            "/**\n"
            " * Example javadoc\n"
            " * <pre>\n"
            " * Benchmark                                             Mode  Cnt       Score   Error  Units\n"
            " * CounterBenchmark.codahaleIncNoLabels                 thrpt        57881.585          ops/s\n"
            " * HistogramBenchmark.prometheusNative                  thrpt         2385.134          ops/s\n"
            " * TextFormatUtilBenchmark.prometheusWriteToNull        thrpt       885331.328          ops/s\n"
            " * CounterBenchmark.prometheusInc                       thrpt        54090.469          ops/s\n"
            " * </pre>\n"
            " */\n"
        )

        for cls in (
            "CounterBenchmark",
            "HistogramBenchmark",
            "TextFormatUtilBenchmark",
        ):
            fname = os.path.join(self.module_path, f"{cls}.java")
            with open(fname, "w", encoding="utf-8") as f:
                f.write(javadoc_pre)
                f.write(f"public class {cls} {{}}\n")
            self.files[cls] = fname

    def tearDown(self):
        self.tmpdir.cleanup()

    def _read_pre_contents(self, path):
        with open(path, "r", encoding="utf-8") as f:
            content = f.read()
        m = re.search(r"<pre>\n([\s\S]*?)</pre>", content)
        return m.group(1) if m else ""

    def test_update_only_inserts_matching_class_lines(self):
        updated = update_pre_blocks_under_module(self.module_path, self.table)
        # All three files should be updated
        self.assertEqual(
            set(os.path.basename(p) for p in updated),
            {
                os.path.basename(self.files["CounterBenchmark"]),
                os.path.basename(self.files["HistogramBenchmark"]),
                os.path.basename(self.files["TextFormatUtilBenchmark"]),
            },
        )

        # Verify CounterBenchmark file contains only CounterBenchmark lines
        cb_pre = self._read_pre_contents(self.files["CounterBenchmark"])
        self.assertIn("CounterBenchmark.codahaleIncNoLabels", cb_pre)
        self.assertIn("CounterBenchmark.prometheusInc", cb_pre)
        self.assertNotIn("HistogramBenchmark.prometheusNative", cb_pre)
        self.assertNotIn("TextFormatUtilBenchmark.prometheusWriteToNull", cb_pre)

        # Verify HistogramBenchmark contains only its line
        hb_pre = self._read_pre_contents(self.files["HistogramBenchmark"])
        self.assertIn("HistogramBenchmark.prometheusNative", hb_pre)
        self.assertNotIn("CounterBenchmark.codahaleIncNoLabels", hb_pre)
        self.assertNotIn("TextFormatUtilBenchmark.prometheusWriteToNull", hb_pre)

        # Verify TextFormatUtilBenchmark contains only its line
        tf_pre = self._read_pre_contents(self.files["TextFormatUtilBenchmark"])
        self.assertIn("TextFormatUtilBenchmark.prometheusWriteToNull", tf_pre)
        self.assertNotIn("CounterBenchmark.prometheusInc", tf_pre)
        self.assertNotIn("HistogramBenchmark.prometheusNative", tf_pre)


if __name__ == "__main__":
    unittest.main()

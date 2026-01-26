#!/usr/bin/env python3

# [MISE] description="Generate markdown summary from JMH benchmark JSON results"
# [MISE] alias="generate-benchmark-summary"

"""
Generate a markdown summary from JMH benchmark JSON results.

Usage:
    python3 .mise/tasks/generate_benchmark_summary.py [--input results.json] [--output-dir ./benchmark-results]

This script:
1. Reads JMH JSON output
2. Generates a README.md with formatted tables
3. Copies results to the output directory with historical naming
"""

import argparse
import json
import os
import shutil
import sys
from datetime import datetime, timezone
from pathlib import Path
from typing import Dict, List, Optional


def parse_args():
    parser = argparse.ArgumentParser(
        description="Generate benchmark summary from JMH JSON"
    )
    parser.add_argument(
        "--input",
        default="benchmark-results.json",
        help="Path to JMH JSON results file (default: benchmark-results.json)",
    )
    parser.add_argument(
        "--output-dir",
        default="benchmark-results",
        help="Output directory for results (default: benchmark-results)",
    )
    parser.add_argument(
        "--commit-sha",
        default=None,
        help="Git commit SHA (default: read from git or 'local')",
    )
    return parser.parse_args()


def get_system_info() -> Dict[str, str]:
    """Capture system hardware information."""
    import multiprocessing
    import platform

    info = {}

    try:
        info["cpu_cores"] = str(multiprocessing.cpu_count())
    except Exception:
        pass

    try:
        with open("/proc/cpuinfo", "r") as f:
            for line in f:
                if line.startswith("model name"):
                    info["cpu_model"] = line.split(":")[1].strip()
                    break
    except FileNotFoundError:
        # macOS
        try:
            import subprocess

            result = subprocess.run(
                ["sysctl", "-n", "machdep.cpu.brand_string"],
                capture_output=True,
                text=True,
                timeout=5,
            )
            if result.returncode == 0:
                info["cpu_model"] = result.stdout.strip()
        except Exception:
            pass

    try:
        with open("/proc/meminfo", "r") as f:
            for line in f:
                if line.startswith("MemTotal"):
                    kb = int(line.split()[1])
                    info["memory_gb"] = str(round(kb / 1024 / 1024))
                    break
    except FileNotFoundError:
        # macOS
        try:
            import subprocess

            result = subprocess.run(
                ["sysctl", "-n", "hw.memsize"],
                capture_output=True,
                text=True,
                timeout=5,
            )
            if result.returncode == 0:
                bytes_mem = int(result.stdout.strip())
                info["memory_gb"] = str(round(bytes_mem / 1024 / 1024 / 1024))
        except Exception:
            pass

    info["os"] = f"{platform.system()} {platform.release()}"

    return info


def get_commit_sha(provided_sha: Optional[str]) -> str:
    """Get commit SHA from argument, git, or return 'local'."""
    if provided_sha:
        return provided_sha

    try:
        import subprocess

        result = subprocess.run(
            ["git", "rev-parse", "HEAD"],
            capture_output=True,
            text=True,
            timeout=5,
        )
        if result.returncode == 0:
            return result.stdout.strip()
    except Exception:
        pass

    return "local"


def format_score(score) -> str:
    """Format score with appropriate precision."""
    try:
        val = float(score)
        if val >= 1_000_000:
            return f"{val / 1_000_000:.2f}M"
        elif val >= 1_000:
            return f"{val / 1_000:.2f}K"
        else:
            return f"{val:.2f}"
    except (ValueError, TypeError):
        return str(score)


def format_error(error) -> str:
    """Format error value, handling NaN."""
    try:
        error_val = float(error)
        if error_val != error_val:  # NaN check
            return ""
        elif error_val >= 1_000:
            return f"± {error_val / 1_000:.2f}K"
        else:
            return f"± {error_val:.2f}"
    except (ValueError, TypeError):
        return ""


def generate_markdown(results: List, commit_sha: str, repo: str) -> str:
    """Generate markdown summary from JMH results."""
    commit_short = commit_sha[:7]
    datetime_str = datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")

    # Extract metadata from first result
    first = results[0] if results else {}
    jdk_version = first.get("jdkVersion", "unknown")
    vm_name = first.get("vmName", "unknown")
    threads = first.get("threads", "?")
    forks = first.get("forks", "?")
    warmup_iters = first.get("warmupIterations", "?")
    measure_iters = first.get("measurementIterations", "?")

    sysinfo = get_system_info()

    md = []
    md.append("# Prometheus Java Client Benchmarks")
    md.append("")

    md.append("## Run Information")
    md.append("")
    md.append(f"- **Date:** {datetime_str}")
    if commit_sha != "local":
        md.append(
            f"- **Commit:** [`{commit_short}`](https://github.com/{repo}/commit/{commit_sha})"
        )
    else:
        md.append(f"- **Commit:** `{commit_short}` (local run)")
    md.append(f"- **JDK:** {jdk_version} ({vm_name})")
    bench_cfg = f"{forks} fork(s), {warmup_iters} warmup, {measure_iters} measurement, {threads} threads"
    md.append(f"- **Benchmark config:** {bench_cfg}")

    hw_parts = []
    if sysinfo.get("cpu_model"):
        hw_parts.append(sysinfo["cpu_model"])
    if sysinfo.get("cpu_cores"):
        hw_parts.append(f"{sysinfo['cpu_cores']} cores")
    if sysinfo.get("memory_gb"):
        hw_parts.append(f"{sysinfo['memory_gb']} GB RAM")
    if hw_parts:
        md.append(f"- **Hardware:** {', '.join(hw_parts)}")
    if sysinfo.get("os"):
        md.append(f"- **OS:** {sysinfo['os']}")

    md.append("")

    # Group by benchmark class
    benchmarks_by_class: Dict[str, List] = {}
    for b in results:
        name = b.get("benchmark", "")
        parts = name.rsplit(".", 1)
        if len(parts) == 2:
            class_name, method = parts
            class_short = class_name.split(".")[-1]
        else:
            class_short = "Other"
        benchmarks_by_class.setdefault(class_short, []).append(b)

    md.append("## Results")
    md.append("")

    # Generate table for each class
    for class_name in sorted(benchmarks_by_class.keys()):
        benchmarks = benchmarks_by_class[class_name]
        md.append(f"### {class_name}")
        md.append("")

        # Sort by score descending
        sorted_benchmarks = sorted(
            benchmarks,
            key=lambda x: x.get("primaryMetric", {}).get("score", 0),
            reverse=True,
        )

        md.append("| Benchmark | Score | Error | Units | |")
        md.append("|:----------|------:|------:|:------|:---|")

        best_score = (
            sorted_benchmarks[0].get("primaryMetric", {}).get("score", 1)
            if sorted_benchmarks
            else 1
        )

        for i, b in enumerate(sorted_benchmarks):
            name = b.get("benchmark", "").split(".")[-1]
            score = b.get("primaryMetric", {}).get("score", 0)
            error = b.get("primaryMetric", {}).get("scoreError", 0)
            unit = b.get("primaryMetric", {}).get("scoreUnit", "ops/s")

            score_fmt = format_score(score)
            error_fmt = format_error(error)

            # Calculate relative performance as multiplier
            try:
                if i == 0:
                    relative_fmt = "**fastest**"
                else:
                    multiplier = float(best_score) / float(score)
                    if multiplier >= 10:
                        relative_fmt = f"{multiplier:.0f}x slower"
                    else:
                        relative_fmt = f"{multiplier:.1f}x slower"
            except (ValueError, TypeError, ZeroDivisionError):
                relative_fmt = ""

            md.append(
                f"| {name} | {score_fmt} | {error_fmt} | {unit} | {relative_fmt} |"
            )

        md.append("")

    md.append("### Raw Results")
    md.append("")
    md.append("```")
    md.append(
        f"{'Benchmark':<50} {'Mode':>6} {'Cnt':>4} {'Score':>14} {'Error':>12}  Units"
    )

    for b in sorted(results, key=lambda x: x.get("benchmark", "")):
        name = b.get("benchmark", "").replace("io.prometheus.metrics.benchmarks.", "")
        mode = b.get("mode", "thrpt")
        cnt = b.get("measurementIterations", 0) * b.get("forks", 1)
        score = b.get("primaryMetric", {}).get("score", 0)
        error = b.get("primaryMetric", {}).get("scoreError", 0)
        unit = b.get("primaryMetric", {}).get("scoreUnit", "ops/s")

        try:
            score_str = f"{float(score):.3f}"
        except (ValueError, TypeError):
            score_str = str(score)

        try:
            error_val = float(error)
            if error_val != error_val:  # NaN
                error_str = ""
            else:
                error_str = f"± {error_val:.3f}"
        except (ValueError, TypeError):
            error_str = ""

        md.append(
            f"{name:<50} {mode:>6} {cnt:>4} {score_str:>14} {error_str:>12}  {unit}"
        )

    md.append("```")
    md.append("")

    md.append("## Notes")
    md.append("")
    md.append("- **Score** = Throughput in operations per second (higher is better)")
    md.append("- **Error** = 99.9% confidence interval")
    md.append("")

    md.append("## Benchmark Descriptions")
    md.append("")
    md.append("| Benchmark | Description |")
    md.append("|:----------|:------------|")
    md.append(
        "| **CounterBenchmark** | Counter increment performance: "
        "Prometheus, OpenTelemetry, simpleclient, Codahale |"
    )
    md.append(
        "| **HistogramBenchmark** | Histogram observation performance "
        "(classic vs native/exponential) |"
    )
    md.append(
        "| **TextFormatUtilBenchmark** | Metric exposition format writing speed |"
    )
    md.append("")
    return "\n".join(md)


def main():
    args = parse_args()

    input_path = Path(args.input)
    if not input_path.exists():
        print(f"Error: Input file not found: {input_path}")
        sys.exit(1)

    print(f"Reading results from: {input_path}")
    with open(input_path, "r") as f:
        results = json.load(f)

    print(f"Found {len(results)} benchmark results")

    commit_sha = get_commit_sha(args.commit_sha)
    commit_short = commit_sha[:7]
    repo = os.environ.get("GITHUB_REPOSITORY", "prometheus/client_java")

    output_dir = Path(args.output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)
    history_dir = output_dir / "history"
    history_dir.mkdir(parents=True, exist_ok=True)

    results_json_path = output_dir / "results.json"
    shutil.copy(input_path, results_json_path)
    print(f"Copied results to: {results_json_path}")

    date_str = datetime.now(timezone.utc).strftime("%Y-%m-%d")
    history_path = history_dir / f"{date_str}-{commit_short}.json"
    shutil.copy(input_path, history_path)
    print(f"Saved historical entry: {history_path}")

    markdown = generate_markdown(results, commit_sha, repo)
    readme_path = output_dir / "README.md"
    with open(readme_path, "w") as f:
        f.write(markdown)
    print(f"Generated summary: {readme_path}")

    print(f"\nDone! Results are in: {output_dir}/")


if __name__ == "__main__":
    main()

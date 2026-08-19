#!/usr/bin/env python3

# [MISE] description="Generate markdown summary from JMH benchmark JSON results"
# [MISE] alias="generate-benchmark-summary"

"""
Generate a markdown summary from JMH benchmark JSON results.

Usage:
    python3 .mise/tasks/generate_benchmark_summary.py \
        [--input results.json] [--output-dir ./benchmark-results]

This script:
1. Reads JMH JSON output
2. Generates a README.md with formatted tables
3. Copies results to the output directory with historical naming
"""

import argparse
import json
import math
import os
import shutil
import subprocess
import sys
from datetime import datetime, timezone
from pathlib import Path

PRACTICAL_CHANGE_THRESHOLD = 5.0


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
    parser.add_argument(
        "--baseline",
        default=None,
        help="Optional JMH JSON results file to compare against",
    )
    parser.add_argument(
        "--baseline-sha",
        default=None,
        help="Baseline commit SHA (default: read from git or 'local')",
    )
    parser.add_argument(
        "--baseline-repo",
        default=None,
        help="Baseline GitHub repository for commit links",
    )
    parser.add_argument(
        "--comparison-note",
        default=None,
        help="Optional note to include in the benchmark comparison section",
    )
    parser.add_argument(
        "--system-info",
        default=None,
        help="Optional JSON file with system info for the measured run",
    )
    parser.add_argument(
        "--baseline-system-info",
        default=None,
        help="Optional JSON file with system info for the baseline run",
    )
    parser.add_argument(
        "--write-system-info",
        default=None,
        help="Write current system info to this JSON file and exit",
    )
    return parser.parse_args()


def get_system_info() -> dict[str, str]:
    """Capture system hardware information."""
    import multiprocessing
    import platform

    info = {}

    try:
        info["cpu_cores"] = str(multiprocessing.cpu_count())
    except NotImplementedError:
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
            result = subprocess.run(
                ["sysctl", "-n", "machdep.cpu.brand_string"],
                capture_output=True,
                check=False,
                text=True,
                timeout=5,
            )
            if result.returncode == 0:
                info["cpu_model"] = result.stdout.strip()
        except (OSError, subprocess.SubprocessError):
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
            result = subprocess.run(
                ["sysctl", "-n", "hw.memsize"],
                capture_output=True,
                check=False,
                text=True,
                timeout=5,
            )
            if result.returncode == 0:
                bytes_mem = int(result.stdout.strip())
                info["memory_gb"] = str(round(bytes_mem / 1024 / 1024 / 1024))
        except (OSError, ValueError, subprocess.SubprocessError):
            pass

    info["os"] = f"{platform.system()} {platform.release()}"

    return info


def read_system_info(path: str | None) -> dict[str, str]:
    """Read system info from JSON, or capture it from the current host."""
    if not path:
        return get_system_info()
    with open(path, "r") as f:
        return json.load(f)


def write_system_info(path: str) -> None:
    """Write current system info to JSON."""
    output_path = Path(path)
    output_path.parent.mkdir(parents=True, exist_ok=True)
    with open(output_path, "w") as f:
        json.dump(get_system_info(), f, indent=2, sort_keys=True)
        f.write("\n")


def format_system_info(sysinfo: dict[str, str] | None) -> str:
    """Format captured system info for markdown."""
    if not sysinfo:
        return "unknown"
    parts = []
    if sysinfo.get("cpu_model"):
        parts.append(sysinfo["cpu_model"])
    if sysinfo.get("cpu_cores"):
        parts.append(f"{sysinfo['cpu_cores']} cores")
    if sysinfo.get("memory_gb"):
        parts.append(f"{sysinfo['memory_gb']} GB RAM")
    if sysinfo.get("os"):
        parts.append(sysinfo["os"])
    return ", ".join(parts) if parts else "unknown"


def get_commit_sha(provided_sha: str | None) -> str:
    """Get commit SHA from argument, git, or return 'local'."""
    if provided_sha:
        return provided_sha

    try:
        result = subprocess.run(
            ["git", "rev-parse", "HEAD"],
            capture_output=True,
            check=False,
            text=True,
            timeout=5,
        )
        if result.returncode == 0:
            return result.stdout.strip()
    except (OSError, subprocess.SubprocessError):
        pass

    return "local"


def format_score(score) -> str:
    """Format score with appropriate precision."""
    if score is None:
        return ""
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
        if math.isnan(error_val):
            return ""
        elif error_val >= 1_000:
            return f"± {error_val / 1_000:.2f}K"
        else:
            return f"± {error_val:.2f}"
    except (ValueError, TypeError):
        return ""


def format_commit_link(commit_sha: str, repo: str) -> str:
    """Format a commit SHA as a GitHub markdown link."""
    commit_short = commit_sha[:7]
    if commit_sha != "local":
        return f"[`{commit_short}`](https://github.com/{repo}/commit/{commit_sha})"
    return f"`{commit_short}` (local run)"


def short_benchmark_name(name: str) -> str:
    """Remove the common benchmark package prefix."""
    return name.replace("io.prometheus.metrics.benchmarks.", "")


def metric_score(result: dict) -> float | None:
    """Extract a benchmark score as a finite float."""
    try:
        score = float(result.get("primaryMetric", {}).get("score"))
        if math.isfinite(score):
            return score
    except (ValueError, TypeError):
        pass
    return None


def score_interval(result: dict) -> tuple[float, float] | None:
    """Extract the JMH confidence interval for a benchmark result."""
    metric = result.get("primaryMetric", {})
    confidence = metric.get("scoreConfidence")
    if isinstance(confidence, list) and len(confidence) == 2:
        try:
            low = float(confidence[0])
            high = float(confidence[1])
            if math.isfinite(low) and math.isfinite(high):
                return min(low, high), max(low, high)
        except (ValueError, TypeError):
            pass

    score = metric_score(result)
    if score is None:
        return None
    try:
        error = float(metric.get("scoreError"))
        if math.isfinite(error) and error >= 0:
            return score - error, score + error
    except (ValueError, TypeError):
        pass
    return None


def lower_is_better(result: dict) -> bool:
    """Return true for JMH modes where lower score is better."""
    mode = str(result.get("mode", ""))
    unit = str(result.get("primaryMetric", {}).get("scoreUnit", ""))
    return mode in {"avgt", "sample", "ss"} or unit.endswith("/op")


def normalize_jvm_args(value) -> list:
    """Normalize optional JMH JVM argument fields for metadata comparison."""
    if value is None:
        return []
    if isinstance(value, list):
        return value
    return [value]


def benchmark_metadata(result: dict) -> dict:
    """Return metadata that must match for a base/head comparison."""
    primary_metric = result.get("primaryMetric", {})
    return {
        "jmhVersion": result.get("jmhVersion"),
        "mode": result.get("mode"),
        "vmName": result.get("vmName"),
        "vmVersion": result.get("vmVersion"),
        "jvmArgs": normalize_jvm_args(result.get("jvmArgs")),
        "jvmArgsPrepend": normalize_jvm_args(result.get("jvmArgsPrepend")),
        "jvmArgsAppend": normalize_jvm_args(result.get("jvmArgsAppend")),
        "threads": result.get("threads"),
        "forks": result.get("forks"),
        "warmupIterations": result.get("warmupIterations"),
        "warmupTime": result.get("warmupTime"),
        "warmupBatchSize": result.get("warmupBatchSize"),
        "measurementIterations": result.get("measurementIterations"),
        "measurementTime": result.get("measurementTime"),
        "measurementBatchSize": result.get("measurementBatchSize"),
        "jdkVersion": result.get("jdkVersion"),
        "scoreUnit": primary_metric.get("scoreUnit"),
        "params": result.get("params", {}),
    }


def comparable_metadata(head: dict, baseline: dict) -> bool:
    """Return whether two results describe the same benchmark configuration."""
    return benchmark_metadata(head) == benchmark_metadata(baseline)


def comparison_status(head: dict, baseline: dict) -> str:
    """Classify a benchmark comparison using confidence intervals and a threshold."""
    head_interval = score_interval(head)
    baseline_interval = score_interval(baseline)
    head_score = metric_score(head)
    baseline_score = metric_score(baseline)
    if (
        not comparable_metadata(head, baseline)
        or head_score is None
        or baseline_score is None
    ):
        return "inconclusive"

    change = performance_change(head, baseline)
    if change is None or head_interval is None or baseline_interval is None:
        return "inconclusive"

    head_low, head_high = head_interval
    baseline_low, baseline_high = baseline_interval
    intervals_overlap = head_low <= baseline_high and baseline_low <= head_high
    if intervals_overlap or abs(change) < PRACTICAL_CHANGE_THRESHOLD:
        return "within noise"

    return "meaningful improvement" if change > 0 else "meaningful regression"


def performance_change(head: dict, baseline: dict) -> float | None:
    """Return percent performance change, with positive meaning faster."""
    head_score = metric_score(head)
    baseline_score = metric_score(baseline)
    if (
        head_score is None
        or baseline_score is None
        or head_score == 0
        or baseline_score == 0
    ):
        return None
    if lower_is_better(head):
        return (float(baseline_score) / head_score - 1) * 100
    return (head_score / float(baseline_score) - 1) * 100


def format_change(change: float | None) -> str:
    """Format a percent performance change."""
    if change is None:
        return ""
    return f"{change:+.1f}%"


def metric_direction_note(results: list) -> str:
    """Describe whether scores represent throughput or latency."""
    directions = {
        "latency" if lower_is_better(result) else "throughput" for result in results
    }
    if directions == {"throughput"}:
        return (
            "Throughput scores are higher-is-better; positive Head vs base deltas "
            "indicate faster performance."
        )
    if directions == {"latency"}:
        return (
            "Latency scores are lower-is-better; positive Head vs base deltas "
            "indicate faster performance."
        )
    return (
        "Throughput scores are higher-is-better and latency scores are "
        "lower-is-better; positive Head vs base deltas indicate faster performance."
    )


def generate_comparison_section(
    results: list,
    baseline_results: list,
    commit_sha: str,
    baseline_sha: str,
    repo: str,
    baseline_repo: str,
    comparison_note: str | None = None,
    system_info: dict[str, str] | None = None,
    baseline_system_info: dict[str, str] | None = None,
) -> list[str]:
    """Generate a base-vs-head benchmark comparison section."""
    by_name = {b.get("benchmark", ""): b for b in results if b.get("benchmark")}
    baseline_by_name = {
        b.get("benchmark", ""): b for b in baseline_results if b.get("benchmark")
    }
    common_names = sorted(set(by_name) & set(baseline_by_name))

    md = []
    md.append("## Comparison with base")
    md.append("")
    md.append(f"- **Head:** {format_commit_link(commit_sha, repo)}")
    md.append(f"- **Base:** {format_commit_link(baseline_sha, baseline_repo)}")
    md.append(f"- **Metric direction:** {metric_direction_note(results)}")
    if comparison_note:
        md.append(f"- **Note:** {comparison_note}")
    if baseline_system_info:
        md.append(f"- **Head runner:** {format_system_info(system_info)}")
        md.append(f"- **Base runner:** {format_system_info(baseline_system_info)}")
        md.append(
            "- **Note:** base and head run in parallel jobs, so runner "
            "hardware can differ and affect results."
        )
    md.append("")

    if not common_names:
        md.append("_No matching benchmark names were found in the base results._")
        md.append("")
        return md

    md.append("| Benchmark | PR | Base | Head vs base | Regression verdict |")
    md.append("|:----------|---:|-----:|-------:|:-------|")

    for name in common_names:
        head = by_name[name]
        baseline = baseline_by_name[name]
        head_score = metric_score(head)
        baseline_score = metric_score(baseline)
        md.append(
            "| "
            f"{short_benchmark_name(name)} | "
            f"{format_score(head_score)} | "
            f"{format_score(baseline_score)} | "
            f"{format_change(performance_change(head, baseline))} | "
            f"{comparison_status(head, baseline)} |"
        )

    missing_in_base = sorted(set(by_name) - set(baseline_by_name))
    missing_in_head = sorted(set(baseline_by_name) - set(by_name))
    if missing_in_base or missing_in_head:
        md.append("")
        if missing_in_base:
            missing = ", ".join(short_benchmark_name(name) for name in missing_in_base)
            md.append(
                f"- Benchmarks only in PR results (listed separately below): {missing}"
            )
        if missing_in_head:
            missing = ", ".join(short_benchmark_name(name) for name in missing_in_head)
            md.append(f"- Benchmarks only in base results: {missing}")

    md.append("")
    return md


def generate_markdown(
    results: list,
    commit_sha: str,
    repo: str,
    baseline_results: list | None = None,
    baseline_sha: str | None = None,
    baseline_repo: str | None = None,
    comparison_note: str | None = None,
    system_info: dict[str, str] | None = None,
    baseline_system_info: dict[str, str] | None = None,
) -> str:
    """Generate markdown summary from JMH results."""
    datetime_str = datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")

    # Extract metadata from first result
    first = results[0] if results else {}
    jdk_version = first.get("jdkVersion", "unknown")
    vm_name = first.get("vmName", "unknown")
    threads = first.get("threads", "?")
    forks = first.get("forks", "?")
    warmup_iters = first.get("warmupIterations", "?")
    measure_iters = first.get("measurementIterations", "?")

    sysinfo = system_info or get_system_info()

    md = []
    md.append("# Prometheus Java Client Benchmarks")
    md.append("")

    md.append("## Run Information")
    md.append("")
    md.append(f"- **Date:** {datetime_str}")
    md.append(f"- **Commit:** {format_commit_link(commit_sha, repo)}")
    md.append(f"- **JDK:** {jdk_version} ({vm_name})")
    bench_cfg = (
        f"{forks} fork(s), {warmup_iters} warmup, "
        f"{measure_iters} measurement, {threads} threads"
    )
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

    if baseline_results and baseline_sha and baseline_repo:
        md.extend(
            generate_comparison_section(
                results,
                baseline_results,
                commit_sha,
                baseline_sha,
                repo,
                baseline_repo,
                comparison_note=comparison_note,
                system_info=sysinfo,
                baseline_system_info=baseline_system_info,
            )
        )

    # A benchmark without a base counterpart cannot receive a regression verdict.
    # Keep it out of the comparison-oriented head table and list it separately.
    baseline_names = {
        b.get("benchmark", "") for b in (baseline_results or []) if b.get("benchmark")
    }
    if baseline_results:
        comparable_results = [
            b for b in results if b.get("benchmark", "") in baseline_names
        ]
        head_only_results = [
            b for b in results if b.get("benchmark", "") not in baseline_names
        ]
    else:
        comparable_results = results
        head_only_results = []

    # Group by benchmark class
    benchmarks_by_class: dict[str, list] = {}
    for b in comparable_results:
        name = b.get("benchmark", "")
        parts = name.rsplit(".", 1)
        if len(parts) == 2:
            class_name, _method = parts
            class_short = class_name.split(".")[-1]
        else:
            class_short = "Other"
        benchmarks_by_class.setdefault(class_short, []).append(b)

    md.append("## Results for PR head")
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

        md.append("| Benchmark | Score | Error | Units |")
        md.append("|:----------|------:|------:|:------|")

        for b in sorted_benchmarks:
            name = b.get("benchmark", "").split(".")[-1]
            score = b.get("primaryMetric", {}).get("score", 0)
            error = b.get("primaryMetric", {}).get("scoreError", 0)
            unit = b.get("primaryMetric", {}).get("scoreUnit", "ops/s")

            score_fmt = format_score(score)
            error_fmt = format_error(error)

            md.append(f"| {name} | {score_fmt} | {error_fmt} | {unit} |")

        md.append("")

    if head_only_results:
        md.append("## New benchmarks in PR head")
        md.append("")
        md.append(
            "These benchmarks have no base counterpart; scores are descriptive only "
            "and have no regression verdict."
        )
        md.append("")
        md.append("| Benchmark | Score | Error | Units |")
        md.append("|:----------|------:|------:|:------|")
        for b in sorted(head_only_results, key=lambda x: x.get("benchmark", "")):
            name = short_benchmark_name(b.get("benchmark", ""))
            score = b.get("primaryMetric", {}).get("score", 0)
            error = b.get("primaryMetric", {}).get("scoreError", 0)
            unit = b.get("primaryMetric", {}).get("scoreUnit", "ops/s")
            md.append(
                f"| {name} | {format_score(score)} | {format_error(error)} | {unit} |"
            )
        md.append("")

    md.append("### Raw Results")
    md.append("")
    md.append("```")
    md.append(
        f"{'Benchmark':<50} {'Mode':>6} {'Cnt':>4} {'Score':>14} {'Error':>12}  Units"
    )

    for b in sorted(results, key=lambda x: x.get("benchmark", "")):
        name = short_benchmark_name(b.get("benchmark", ""))
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
            if math.isnan(error_val):
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
    md.append(
        "- **Score** = the JMH primary metric; "
        "throughput is higher-is-better and latency is lower-is-better."
    )
    md.append("- **Error** = 99.9% confidence interval")
    if baseline_results:
        md.append(
            "- **Regression verdict** requires comparable benchmark metadata, "
            "non-overlapping JMH confidence intervals, and a change of at least "
            f"{PRACTICAL_CHANGE_THRESHOLD:.0f}%; otherwise it is marked "
            '"within noise" or "inconclusive".'
        )
    md.append(
        "- Scores for different benchmark methods are not ranked against one another; "
        "they may measure different workloads."
    )
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

    if args.write_system_info:
        write_system_info(args.write_system_info)
        print(f"Wrote system info to: {args.write_system_info}")
        return

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
    baseline_results = None
    baseline_sha = None
    baseline_repo = args.baseline_repo or os.environ.get("GITHUB_BASE_REPOSITORY", repo)
    system_info = read_system_info(args.system_info)
    baseline_system_info = None

    baseline_path = Path(args.baseline) if args.baseline else None
    if baseline_path:
        if not baseline_path.exists():
            print(f"Error: Baseline file not found: {baseline_path}")
            sys.exit(1)
        print(f"Reading baseline results from: {baseline_path}")
        with open(baseline_path, "r") as f:
            baseline_results = json.load(f)
        baseline_sha = get_commit_sha(args.baseline_sha)
        baseline_system_info = (
            read_system_info(args.baseline_system_info)
            if args.baseline_system_info
            else None
        )
        print(f"Found {len(baseline_results)} baseline benchmark results")

    output_dir = Path(args.output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)
    history_dir = output_dir / "history"
    history_dir.mkdir(parents=True, exist_ok=True)

    results_json_path = output_dir / "results.json"
    shutil.copy(input_path, results_json_path)
    print(f"Copied results to: {results_json_path}")

    if baseline_path:
        baseline_json_path = output_dir / "baseline-results.json"
        shutil.copy(baseline_path, baseline_json_path)
        print(f"Copied baseline results to: {baseline_json_path}")

    date_str = datetime.now(timezone.utc).strftime("%Y-%m-%d")
    history_path = history_dir / f"{date_str}-{commit_short}.json"
    shutil.copy(input_path, history_path)
    print(f"Saved historical entry: {history_path}")

    markdown = generate_markdown(
        results,
        commit_sha,
        repo,
        baseline_results=baseline_results,
        baseline_sha=baseline_sha,
        baseline_repo=baseline_repo,
        comparison_note=args.comparison_note,
        system_info=system_info,
        baseline_system_info=baseline_system_info,
    )
    readme_path = output_dir / "README.md"
    with open(readme_path, "w") as f:
        f.write(markdown)
    print(f"Generated summary: {readme_path}")

    print(f"\nDone! Results are in: {output_dir}/")


if __name__ == "__main__":
    main()

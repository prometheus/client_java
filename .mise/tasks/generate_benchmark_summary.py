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
import os
import shutil
import sys
from datetime import datetime, timezone
from pathlib import Path
from typing import Dict, List, Optional, Tuple


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


def read_system_info(path: Optional[str]) -> Dict[str, str]:
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


def format_system_info(sysinfo: Optional[Dict[str, str]]) -> str:
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
        if error_val != error_val:  # NaN check
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


def metric_score(result: Dict) -> Optional[float]:
    """Extract a benchmark score as a finite float."""
    try:
        score = float(result.get("primaryMetric", {}).get("score"))
        if score == score:
            return score
    except (ValueError, TypeError):
        pass
    return None


def score_interval(result: Dict) -> Optional[Tuple[float, float]]:
    """Extract the JMH confidence interval for a benchmark result."""
    metric = result.get("primaryMetric", {})
    confidence = metric.get("scoreConfidence")
    if isinstance(confidence, list) and len(confidence) == 2:
        try:
            low = float(confidence[0])
            high = float(confidence[1])
            if low == low and high == high:
                return min(low, high), max(low, high)
        except (ValueError, TypeError):
            pass

    score = metric_score(result)
    if score is None:
        return None
    try:
        error = float(metric.get("scoreError"))
        if error == error:
            return score - error, score + error
    except (ValueError, TypeError):
        pass
    return None


def lower_is_better(result: Dict) -> bool:
    """Return true for JMH modes where lower score is better."""
    mode = str(result.get("mode", ""))
    unit = str(result.get("primaryMetric", {}).get("scoreUnit", ""))
    return mode in {"avgt", "sample", "ss"} or unit.endswith("/op")


def comparison_status(head: Dict, baseline: Dict) -> str:
    """Classify a benchmark comparison using confidence intervals."""
    head_interval = score_interval(head)
    baseline_interval = score_interval(baseline)
    head_score = metric_score(head)
    baseline_score = metric_score(baseline)
    if head_score is None or baseline_score is None:
        return ""

    is_lower_better = lower_is_better(head)
    if head_interval and baseline_interval:
        head_low, head_high = head_interval
        baseline_low, baseline_high = baseline_interval
        if is_lower_better:
            if head_high < baseline_low:
                return "faster"
            if head_low > baseline_high:
                return "slower"
        else:
            if head_low > baseline_high:
                return "faster"
            if head_high < baseline_low:
                return "slower"
        return "within noise"

    if is_lower_better:
        return "faster" if head_score < baseline_score else "slower"
    return "faster" if head_score > baseline_score else "slower"


def performance_change(head: Dict, baseline: Dict) -> Optional[float]:
    """Return percent performance change, with positive meaning faster."""
    head_score = metric_score(head)
    baseline_score = metric_score(baseline)
    if head_score is None or baseline_score in (None, 0):
        return None
    if lower_is_better(head):
        return (float(baseline_score) / head_score - 1) * 100
    return (head_score / float(baseline_score) - 1) * 100


def format_change(change: Optional[float]) -> str:
    """Format a percent performance change."""
    if change is None:
        return ""
    return f"{change:+.1f}%"


def generate_comparison_section(
    results: List,
    baseline_results: List,
    commit_sha: str,
    baseline_sha: str,
    repo: str,
    baseline_repo: str,
    system_info: Optional[Dict[str, str]] = None,
    baseline_system_info: Optional[Dict[str, str]] = None,
) -> List[str]:
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
    md.append("- **Change:** positive means the PR is faster than base.")
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

    md.append("| Benchmark | PR | Base | Change | Result |")
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
            md.append(f"- Benchmarks only in PR results: {missing}")
        if missing_in_head:
            missing = ", ".join(short_benchmark_name(name) for name in missing_in_head)
            md.append(f"- Benchmarks only in base results: {missing}")

    md.append("")
    return md


def generate_markdown(
    results: List,
    commit_sha: str,
    repo: str,
    baseline_results: Optional[List] = None,
    baseline_sha: Optional[str] = None,
    baseline_repo: Optional[str] = None,
    system_info: Optional[Dict[str, str]] = None,
    baseline_system_info: Optional[Dict[str, str]] = None,
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
                system_info=sysinfo,
                baseline_system_info=baseline_system_info,
            )
        )

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

        md.append("| Benchmark | Score | Error | Units | Within run |")
        md.append("|:----------|------:|------:|:------|:-----------|")

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
    if baseline_results:
        md.append(
            "- **Comparison with base** uses JMH confidence intervals when "
            'available; overlapping intervals are marked "within noise".'
        )
    md.append(
        "- **Within run** compares benchmarks in the same result set, not against "
        "the base commit."
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

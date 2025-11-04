#!/usr/bin/env python3

# [MISE] description="Run and update JMH benchmark outputs in the benchmarks module"
# [MISE] alias="update-benchmarks"

"""
Run benchmarks for the `benchmarks` module, capture JMH text output, and update
any <pre>...</pre> blocks containing "thrpt" under the `benchmarks/` module
(files such as Java sources with embedded example output in javadocs).

Usage: ./.mise/tasks/update_benchmarks.py [--mvnw ./mvnw] [--module benchmarks] [--java java] [--jmh-args "-f 1 -wi 0 -i 1"]

By default this will:
 - run the maven wrapper to package the benchmarks: `./mvnw -pl benchmarks -am -DskipTests package`
 - locate the shaded jar under `benchmarks/target/` (named containing "benchmarks")
 - run `java -jar <jar> -rf text` (add extra JMH args with --jmh-args)
 - parse the first JMH table (the block starting with the "Benchmark  Mode" header)
 - update all files under the `benchmarks/` directory which contain a `<pre>` block with the substring "thrpt"

This script is careful to preserve Javadoc comment prefixes like " * " when replacing the
contents of the <pre> block.
"""

import argparse
import glob
import os
import re
import shlex
import subprocess
import sys
from typing import List, Optional


def run_cmd(cmd: List[str], cwd: Optional[str] = None) -> str:
    """Run a command, stream stdout/stderr to the console for progress, and return the full output.

    This replaces the previous blocking subprocess.run approach so users can see build / JMH
    progress in real time while the command runs.
    """
    try:
        proc = subprocess.Popen(
            cmd, cwd=cwd, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True
        )
    except FileNotFoundError:
        # Helpful message if the executable is not found
        print(f"Command not found: {cmd[0]}")
        raise

    output_lines: List[str] = []
    try:
        assert proc.stdout is not None
        # Stream lines as they appear and capture them for returning
        for line in proc.stdout:
            # Print immediately so callers (and CI) can observe progress
            print(line, end="")
            output_lines.append(line)
        proc.wait()
    except KeyboardInterrupt:
        # If the user interrupts, ensure the child process is terminated
        proc.kill()
        proc.wait()
        print("\nCommand interrupted by user.")
        raise

    output = "".join(output_lines)
    if proc.returncode != 0:
        print(
            f"Command failed: {' '.join(cmd)}\nExit: {proc.returncode}\nOutput:\n{output}"
        )
        raise SystemExit(proc.returncode)
    return output


def build_benchmarks(mvnw: str, module: str) -> None:
    print(f"Building Maven module '{module}' using {mvnw} (this may take a while)...")
    cmd = [mvnw, "-pl", module, "-am", "-DskipTests", "package"]
    run_cmd(cmd)
    print("Build completed.")


def find_benchmarks_jar(module: str) -> str:
    pattern = os.path.join(module, "target", "*.jar")
    jars = [p for p in glob.glob(pattern) if "original" not in p and p.endswith(".jar")]
    # prefer jar whose basename contains module name
    jars_pref = [j for j in jars if module in os.path.basename(j)]
    chosen = (jars_pref or jars)[:1]
    if not chosen:
        raise FileNotFoundError(
            f"No jar found in {os.path.join(module, 'target')} (tried: {pattern})"
        )
    jar = chosen[0]
    print(f"Using jar: {jar}")
    return jar


def run_jmh(jar: str, java_cmd: str, extra_args: Optional[str]) -> str:
    args = [java_cmd, "-jar", jar, "-rf", "text"]
    if extra_args:
        args += shlex.split(extra_args)
    print(f"Running JMH: {' '.join(args)}")
    output = run_cmd(args)
    print("JMH run completed.")
    return output


def extract_first_table(jmh_output: str) -> str:
    # Try to extract the first table that starts with "Benchmark" header and continues until a blank line
    m = re.search(r"(\nBenchmark\s+Mode[\s\S]*?)(?:\n\s*\n|\Z)", jmh_output)
    if not m:
        # fallback: collect all lines that contain 'thrpt' plus a header if present
        lines = [line for line in jmh_output.splitlines() if "thrpt" in line]
        if not lines:
            raise ValueError('Could not find any "thrpt" lines in JMH output')
        # try to find header
        header = next(
            (
                line
                for line in jmh_output.splitlines()
                if line.startswith("Benchmark") and "Mode" in line
            ),
            "Benchmark                                     Mode  Cnt      Score     Error  Units",
        )
        return header + "\n" + "\n".join(lines)
    table = m.group(1).strip("\n")
    # Ensure we return only the table lines (remove any leading iteration info lines that JMH sometimes prints)
    # Normalize spaces: keep as-is
    return table


def filter_table_for_class(table: str, class_name: str) -> Optional[str]:
    """
    Return a table string that contains only the header and the lines belonging to `class_name`.
    If no matching lines are found, return None.
    """
    lines = table.splitlines()
    # find header line index (starts with 'Benchmark' and contains 'Mode')
    header_idx = None
    for i, ln in enumerate(lines):
        if ln.strip().startswith("Benchmark") and "Mode" in ln:
            header_idx = i
            break
    header = (
        lines[header_idx]
        if header_idx is not None
        else "Benchmark                                     Mode  Cnt      Score     Error  Units"
    )

    matched = []
    pattern = re.compile(r"^\s*" + re.escape(class_name) + r"\.")
    for ln in lines[header_idx + 1 if header_idx is not None else 0 :]:
        if "thrpt" in ln and pattern.search(ln):
            matched.append(ln)

    if not matched:
        return None
    return header + "\n" + "\n".join(matched)


def update_pre_blocks_under_module(module: str, table: str) -> List[str]:
    # Find files under module and update any <pre>...</pre> block that contains 'thrpt'
    updated_files = []
    for path in glob.glob(os.path.join(module, "**"), recursive=True):
        if os.path.isdir(path):
            continue
        try:
            with open(path, "r", encoding="utf-8") as f:
                content = f.read()
        except Exception:
            continue
        # quick filter
        if "<pre>" not in content or "thrpt" not in content:
            continue

        original = content
        new_content = content

        # Determine the class name from the filename (e.g. TextFormatUtilBenchmark.java -> TextFormatUtilBenchmark)
        base = os.path.basename(path)
        class_name = os.path.splitext(base)[0]

        # Build a filtered table for this class; if no matching lines, skip updating this file
        filtered_table = filter_table_for_class(table, class_name)
        if filtered_table is None:
            # nothing to update for this class
            continue

        # Regex to find any line-starting Javadoc prefix like " * " before <pre>
        # This will match patterns like: " * <pre>... </pre>" and capture the prefix (e.g. " * ")
        pattern = re.compile(r"(?m)^(?P<prefix>[ \t]*\*[ \t]*)<pre>[\s\S]*?</pre>")

        def repl(m: re.Match) -> str:
            prefix = m.group("prefix")
            # Build the new block with the same prefix on each line
            lines = filtered_table.splitlines()
            replaced = prefix + "<pre>\n"
            for ln in lines:
                replaced += prefix + ln.rstrip() + "\n"
            replaced += prefix + "</pre>"
            return replaced

        new_content, nsubs = pattern.subn(repl, content)
        if nsubs > 0 and new_content != original:
            with open(path, "w", encoding="utf-8") as f:
                f.write(new_content)
            updated_files.append(path)
            print(f"Updated {path}: replaced {nsubs} <pre> block(s)")
    return updated_files


def main(argv: List[str]):
    parser = argparse.ArgumentParser()
    parser.add_argument("--mvnw", default="./mvnw", help="Path to maven wrapper")
    parser.add_argument(
        "--module", default="benchmarks", help="Module directory to build/run"
    )
    parser.add_argument("--java", default="java", help="Java command")
    parser.add_argument(
        "--jmh-args",
        default="",
        help='Extra arguments to pass to the JMH main (e.g. "-f 1 -wi 0 -i 1")',
    )
    args = parser.parse_args(argv)

    build_benchmarks(args.mvnw, args.module)
    jar = find_benchmarks_jar(args.module)
    output = run_jmh(jar, args.java, args.jmh_args)

    # Print a short preview of the JMH output
    preview = "\n".join(output.splitlines()[:120])
    print("\n--- JMH output preview ---")
    print(preview)
    print("--- end preview ---\n")

    table = extract_first_table(output)

    updated = update_pre_blocks_under_module(args.module, table)

    if not updated:
        print(
            'No files were updated (no <pre> blocks with "thrpt" found under the module).'
        )
    else:
        print("\nUpdated files:")
        for p in updated:
            print(" -", p)


if __name__ == "__main__":
    main(sys.argv[1:])

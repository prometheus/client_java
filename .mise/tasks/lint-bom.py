#!/usr/bin/env python3

#MISE description="Make sure the BOM has all necessary modules"

import os
import re
import sys
from fnmatch import fnmatch
from pathlib import Path
from typing import List
import difflib

ROOT = Path(__file__).resolve().parents[2]  # repo root (.. from .mise/tasks)
IGNORE_DIRS = {"prometheus-metrics-parent"}
MODULE_PREFIX = "prometheus-metrics"
BOM_POM = ROOT / "prometheus-metrics-bom" / "pom.xml"


def first_artifact_id(pom_file: Path) -> str:
    """Return the second <artifactId> value from the given pom.xml (matches original script).

    The original shell function greps all <artifactId> lines and returns the second one
    (head -n 2 | tail -n 1). We replicate that behavior exactly.
    """
    if not pom_file.is_file():
        raise FileNotFoundError(f"File {pom_file} does not exist.")

    text = pom_file.read_text(encoding="utf-8")
    matches = re.findall(r"<artifactId>\s*(.*?)\s*</artifactId>", text)
    if len(matches) < 2:
        return ""
    return matches[1].strip()


def add_dir(dir_path: Path, want: List[str]):
    if not dir_path.is_dir():
        raise FileNotFoundError(f"Directory {dir_path} does not exist.")

    if any(dir_path.name == ig for ig in IGNORE_DIRS):
        print(f"Skipping {dir_path}")
        return

    pom = dir_path / "pom.xml"
    if not pom.is_file():
        raise FileNotFoundError(f"File {pom} does not exist.")

    artifact_id = first_artifact_id(pom)
    if not artifact_id:
        raise RuntimeError(f"No artifactId found in {pom}")

    print(f"Found artifactId '{artifact_id}' in {pom}")
    want.append(artifact_id)


def collect_want(root: Path) -> List[str]:
    want: List[str] = []
    # top-level prometheus-metrics*
    for entry in sorted(root.iterdir()):
        if entry.is_dir() and fnmatch(entry.name, f"{MODULE_PREFIX}*"):
            add_dir(entry, want)

    # prometheus-metrics-tracer/prometheus-metrics*
    tracer_dir = root / "prometheus-metrics-tracer"
    if tracer_dir.is_dir():
        for entry in sorted(tracer_dir.iterdir()):
            if entry.is_dir() and fnmatch(entry.name, f"{MODULE_PREFIX}*"):
                add_dir(entry, want)

    # deduplicate and sort
    want_unique = sorted(set(want))
    return want_unique


def collect_have(bom_pom: Path) -> List[str]:
    if not bom_pom.is_file():
        raise FileNotFoundError(f"BOM file {bom_pom} does not exist.")

    text = bom_pom.read_text(encoding="utf-8")
    # find artifactId values that start with MODULE_PREFIX
    matches = re.findall(
        r"<artifactId>\s*(%s[^<\s]*)\s*</artifactId>" % re.escape(MODULE_PREFIX), text
    )
    return sorted(matches)


def main() -> int:
    try:
        want = collect_want(ROOT)
        have = collect_have(BOM_POM)

        want_text = "\n".join(want)
        have_text = "\n".join(have)

        if want_text != have_text:
            print(
                "The BOM file prometheus-metrics-bom/bom.xml does not match the current directory contents."
            )
            print("Expected:")
            print(want_text)
            print("Found:")
            print(have_text)
            print()
            diff = difflib.unified_diff(
                have_text.splitlines(keepends=True),
                want_text.splitlines(keepends=True),
                fromfile="found",
                tofile="expected",
            )
            sys.stdout.writelines(diff)
            return 1
        else:
            print("BOM file is up to date.")
            return 0

    except Exception as e:
        print(e, file=sys.stderr)
        return 1


if __name__ == "__main__":
    sys.exit(main())

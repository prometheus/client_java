#!/usr/bin/env python3

# [MISE] description="Verify standalone example POMs won't break spotless"

"""Check that standalone example modules don't break 'mise run format'.

Example modules are intentionally standalone (no <parent> from the project)
so users can copy them. But they're included in the Maven reactor via the
examples-and-integration-tests profile. If 'mise run format' doesn't
exclude them, spotless:apply fails because the plugin isn't declared.

This lint verifies that every standalone example POM is excluded from
the format task in mise.toml.
"""

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
EXAMPLES_DIR = ROOT / "examples"


def find_standalone_example_poms() -> list[Path]:
    """Find example pom.xml files that don't inherit from the project parent."""
    standalone = []
    for pom in sorted(EXAMPLES_DIR.rglob("pom.xml")):
        if "target" in pom.parts:
            continue
        text = pom.read_text(encoding="utf-8")
        # Check if this POM has a <parent> with the project's groupId/artifactId
        has_project_parent = bool(
            re.search(
                r"<parent>\s*<groupId>io\.prometheus</groupId>\s*"
                r"<artifactId>client_java</artifactId>",
                text,
            )
        )
        if not has_project_parent:
            standalone.append(pom)
    return standalone


def format_task_excludes_examples() -> bool:
    """Check that the format task in mise.toml excludes standalone examples."""
    mise_toml = ROOT / "mise.toml"
    text = mise_toml.read_text(encoding="utf-8")
    # Look for the format task run command
    match = re.search(
        r'\[tasks\.format\].*?run\s*=\s*"([^"]*)"', text, re.DOTALL
    )
    if not match:
        return False
    run_cmd = match.group(1)
    # The command should deactivate the examples-and-integration-tests profile
    return "!examples-and-integration-tests" in run_cmd


def main() -> int:
    standalone = find_standalone_example_poms()
    if not standalone:
        return 0

    if format_task_excludes_examples():
        return 0

    print("ERROR: Standalone example POMs found but 'mise run format'")
    print("does not exclude the examples-and-integration-tests profile.")
    print()
    print("Standalone example POMs (no project parent):")
    for pom in standalone:
        print(f"  {pom.relative_to(ROOT)}")
    print()
    print("Fix: ensure the format task in mise.toml deactivates the")
    print("examples-and-integration-tests profile, e.g.:")
    print("  -P '!examples-and-integration-tests'")
    return 1


if __name__ == "__main__":
    sys.exit(main())

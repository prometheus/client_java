#!/usr/bin/env python3

# [MISE] description="Bump the snapshot version in all pom.xml files"
# [MISE] alias="bump-snapshot"

"""
Bump the SNAPSHOT version in all pom.xml files.

By default, increments the minor version (e.g. X.Y.0 -> X.(Y+1).0).
An explicit version can be passed as argument:
    mise run bump-snapshot 2.0.0-SNAPSHOT
"""

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]  # repo root
PARENT_POM = ROOT / "prometheus-metrics-parent" / "pom.xml"


def current_snapshot() -> str:
    """Extract the current SNAPSHOT version from the parent pom.xml."""
    text = PARENT_POM.read_text(encoding="utf-8")
    m = re.search(
        r"<groupId>io\.prometheus</groupId>\s*"
        r"<artifactId>client_java_parent</artifactId>\s*"
        r"<version>(\S+)</version>",
        text,
    )
    if not m:
        sys.exit("Could not find version in " + str(PARENT_POM))
    version = m.group(1)
    if not version.endswith("-SNAPSHOT"):
        sys.exit(f"Current version '{version}' is not a SNAPSHOT version")
    return version


def next_minor(version: str) -> str:
    """Increment the minor version: 1.5.0-SNAPSHOT -> 1.6.0-SNAPSHOT."""
    base = version.removesuffix("-SNAPSHOT")
    parts = base.split(".")
    if len(parts) != 3:
        sys.exit(f"Expected three-part version, got '{base}'")
    parts[1] = str(int(parts[1]) + 1)
    parts[2] = "0"
    return ".".join(parts) + "-SNAPSHOT"


def find_pom_files() -> list[Path]:
    """Find all pom.xml files in the repository."""
    return sorted(ROOT.rglob("pom.xml"))


def main() -> None:
    old_version = current_snapshot()

    if len(sys.argv) > 1:
        new_version = sys.argv[1]
        if not new_version.endswith("-SNAPSHOT"):
            sys.exit(f"New version must end with -SNAPSHOT, got '{new_version}'")
    else:
        new_version = next_minor(old_version)

    if old_version == new_version:
        sys.exit(f"Old and new version are the same: {old_version}")

    print(f"Bumping {old_version} -> {new_version}")

    updated_count = 0
    for pom in find_pom_files():
        content = pom.read_text(encoding="utf-8")
        updated = content.replace(old_version, new_version)
        if content != updated:
            pom.write_text(updated, encoding="utf-8")
            print(f"  updated {pom.relative_to(ROOT)}")
            updated_count += 1

    if updated_count == 0:
        sys.exit(f"No pom.xml files contain '{old_version}'")

    print(f"\nDone. {updated_count} pom.xml file(s) updated to {new_version}.")


if __name__ == "__main__":
    main()

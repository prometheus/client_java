#!/usr/bin/env python3

from __future__ import annotations

import os
import re
import subprocess
import xml.etree.ElementTree as ET
from pathlib import Path
from typing import Optional


DEFAULT_JMX_EXPORTER_DIR = Path(
    os.environ.get("JMX_EXPORTER_DIR", "/tmp/jmx-exporter-compat")
)
DEFAULT_JMX_EXPORTER_REPOSITORY = os.environ.get(
    "JMX_EXPORTER_REPOSITORY", "prometheus/jmx_exporter"
)
DEFAULT_JMX_EXPORTER_REMOTE = os.environ.get("JMX_EXPORTER_REMOTE", "origin")
# Test jmx_exporter main rather than the latest release: the integration_test_suite
# only compiles against client_java once a release adopts the stable Metrics class
# (see the tracking issue referenced in mise.toml's DEFAULT_JMX_EXPORTER_VERSION).
DEFAULT_JMX_EXPORTER_REF = os.environ.get("JMX_EXPORTER_REF") or "main"
DEFAULT_PROM_VERSION = os.environ.get("PROM_VERSION")

# Quick test configuration: the integration_test_suite runs one matrix cell
# (single Java + Prometheus distribution) instead of the full smoke-test matrix.
# The distributions are read from the checked-out jmx_exporter's run-quick-test.sh
# so they stay aligned with upstream rather than drifting from a hardcoded copy.
QUICK_TEST_SCRIPT = "run-quick-test.sh"
QUICK_TEST_IMAGE_VARS = ("JAVA_DOCKER_IMAGES", "PROMETHEUS_DOCKER_IMAGES")


def run_cmd(
    cmd: list[str],
    cwd: Optional[Path] = None,
    env: Optional[dict[str, str]] = None,
) -> None:
    subprocess.run(cmd, cwd=cwd, check=True, env=env)


def jmx_exporter_repository_url(repository: str) -> str:
    return f"https://github.com/{repository}.git"


def check_clean_worktree(jmx_exporter_dir: Path) -> None:
    result = subprocess.run(
        ["git", "status", "--short"],
        cwd=jmx_exporter_dir,
        check=True,
        capture_output=True,
        text=True,
    )
    if result.stdout.strip():
        raise RuntimeError(
            f"{jmx_exporter_dir} has uncommitted changes; use a clean clone or set "
            "JMX_EXPORTER_DIR"
        )


def get_prom_version(root_dir: Path = Path.cwd()) -> str:
    configured_version = DEFAULT_PROM_VERSION
    if configured_version:
        return configured_version
    pom = ET.parse(root_dir / "pom.xml")
    root = pom.getroot()
    version = root.findtext("./{*}version")
    if not version:
        version = root.findtext("./{*}parent/{*}version")
    if not version:
        raise RuntimeError("could not determine Prometheus version from pom.xml")
    return version


def prepare_repo(
    jmx_exporter_dir: Path = DEFAULT_JMX_EXPORTER_DIR,
    repository: str = DEFAULT_JMX_EXPORTER_REPOSITORY,
    remote: str = DEFAULT_JMX_EXPORTER_REMOTE,
    ref: str = DEFAULT_JMX_EXPORTER_REF,
) -> None:
    repository_url = jmx_exporter_repository_url(repository)
    if (jmx_exporter_dir / ".git").is_dir():
        check_clean_worktree(jmx_exporter_dir)
        run_cmd(
            ["git", "remote", "set-url", remote, repository_url],
            cwd=jmx_exporter_dir,
        )
        run_cmd(["git", "fetch", remote, ref], cwd=jmx_exporter_dir)
    else:
        run_cmd(["git", "clone", repository_url, str(jmx_exporter_dir)])
        run_cmd(["git", "fetch", remote, ref], cwd=jmx_exporter_dir)
    run_cmd(
        ["git", "checkout", "-B", "jmx-exporter-compat", "FETCH_HEAD"],
        cwd=jmx_exporter_dir,
    )


def install_local_artifacts(root_dir: Path = Path.cwd()) -> None:
    run_cmd(
        [
            "./mvnw",
            "install",
            # Skip test compilation too (not just execution): downstream needs only
            # our main artifacts, and our test sources target a newer release than
            # the compatibility JDK supports.
            "-Dmaven.test.skip=true",
            "-Dcoverage.skip=true",
            "-Dcheckstyle.skip=true",
            "-Dwarnings=-nowarn",
        ],
        cwd=root_dir,
    )


def quick_test_images(
    jmx_exporter_dir: Path = DEFAULT_JMX_EXPORTER_DIR,
) -> dict[str, str]:
    """Read the quick test docker image pins from the checked-out jmx_exporter's
    run-quick-test.sh. An explicit env var overrides the script value."""
    script_path = jmx_exporter_dir / QUICK_TEST_SCRIPT
    script = script_path.read_text()
    images: dict[str, str] = {}
    for var in QUICK_TEST_IMAGE_VARS:
        override = os.environ.get(var)
        if override:
            images[var] = override
            continue
        match = re.search(rf'^\s*export\s+{var}="([^"]+)"', script, re.MULTILINE)
        if not match:
            raise RuntimeError(f"could not find {var} in {script_path}")
        images[var] = match.group(1)
    return images


def run_maven_test(
    jmx_exporter_dir: Path = DEFAULT_JMX_EXPORTER_DIR,
    prom_version: Optional[str] = None,
) -> None:
    if prom_version is None:
        prom_version = get_prom_version()
    # Build the full reactor (including the integration_test_suite) the same way
    # run-quick-test.sh does, but against our locally installed io.prometheus
    # artifacts. The pinned distribution env vars keep this to the quick (single
    # cell) matrix rather than the full smoke-test matrix.
    env = {**os.environ, **quick_test_images(jmx_exporter_dir)}
    cmd = [
        "./mvnw",
        "-B",
        "clean",
        "install",
        f"-Dprometheus.metrics.version={prom_version}",
        f"-Dparamixel.parallelism={os.cpu_count() or 1}",
        "-Djacoco.skip=true",
    ]
    run_cmd(cmd, cwd=jmx_exporter_dir, env=env)

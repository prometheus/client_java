#!/usr/bin/env python3

from __future__ import annotations

import os
import subprocess
import xml.etree.ElementTree as ET
from pathlib import Path
from typing import Optional


DEFAULT_MICROMETER_DIR = Path(
    os.environ.get("MICROMETER_DIR", "/tmp/micrometer-compat")
)
DEFAULT_MICROMETER_REPOSITORY = os.environ.get(
    "MICROMETER_REPOSITORY", "micrometer-metrics/micrometer"
)
DEFAULT_MICROMETER_REMOTE = os.environ.get("MICROMETER_REMOTE", "origin")
DEFAULT_MICROMETER_REF = (
    os.environ.get("MICROMETER_REF")
    or os.environ.get("DEFAULT_MICROMETER_VERSION")
    or "main"
)
DEFAULT_INIT_SCRIPT = Path(
    os.environ.get("MICROMETER_INIT_SCRIPT", "/tmp/micrometer-prom-local.init.gradle")
)
DEFAULT_PROM_VERSION = os.environ.get("PROM_VERSION")


def run_cmd(cmd: list[str], cwd: Optional[Path] = None) -> None:
    subprocess.run(cmd, cwd=cwd, check=True)


def micrometer_repository_url(repository: str) -> str:
    return f"https://github.com/{repository}.git"


def check_clean_worktree(micrometer_dir: Path) -> None:
    result = subprocess.run(
        ["git", "status", "--short"],
        cwd=micrometer_dir,
        check=True,
        capture_output=True,
        text=True,
    )
    if result.stdout.strip():
        raise RuntimeError(
            f"{micrometer_dir} has uncommitted changes; use a clean clone or set MICROMETER_DIR"
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


def write_init_script(
    init_script: Path = DEFAULT_INIT_SCRIPT, prom_version: Optional[str] = None
) -> None:
    if prom_version is None:
        prom_version = get_prom_version()
    init_script.write_text(
        f"""allprojects {{
    repositories {{
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }}
    configurations.configureEach {{
        resolutionStrategy.eachDependency {{ details ->
            if (details.requested.group == 'io.prometheus') {{
                details.useVersion('{prom_version}')
                details.because(
                    'Use local prom_client_java artifacts for downstream compatibility testing'
                )
            }}
        }}
    }}
}}
""",
        encoding="utf-8",
    )


def prepare_repo(
    micrometer_dir: Path = DEFAULT_MICROMETER_DIR,
    repository: str = DEFAULT_MICROMETER_REPOSITORY,
    remote: str = DEFAULT_MICROMETER_REMOTE,
    ref: str = DEFAULT_MICROMETER_REF,
) -> None:
    repository_url = micrometer_repository_url(repository)
    if (micrometer_dir / ".git").is_dir():
        check_clean_worktree(micrometer_dir)
        run_cmd(
            ["git", "remote", "set-url", remote, repository_url], cwd=micrometer_dir
        )
        run_cmd(["git", "fetch", remote, ref], cwd=micrometer_dir)
    else:
        run_cmd(
            [
                "git",
                "clone",
                repository_url,
                str(micrometer_dir),
            ]
        )
        run_cmd(["git", "fetch", remote, ref], cwd=micrometer_dir)
    run_cmd(
        ["git", "checkout", "-B", "micrometer-compat", "FETCH_HEAD"],
        cwd=micrometer_dir,
    )


def install_local_artifacts(root_dir: Path = Path.cwd()) -> None:
    run_cmd(
        [
            "./mvnw",
            "install",
            "-DskipTests",
            "-Dcoverage.skip=true",
            "-Dcheckstyle.skip=true",
            "-Dwarnings=-nowarn",
        ],
        cwd=root_dir,
    )


def run_gradle_test(
    test_selector: Optional[str] = None,
    micrometer_dir: Path = DEFAULT_MICROMETER_DIR,
    init_script: Path = DEFAULT_INIT_SCRIPT,
) -> None:
    cmd = [
        "./gradlew",
        "--no-daemon",
        "-I",
        str(init_script),
        ":micrometer-registry-prometheus:test",
    ]
    if test_selector:
        cmd.extend(["--tests", test_selector])
    run_cmd(cmd, cwd=micrometer_dir)

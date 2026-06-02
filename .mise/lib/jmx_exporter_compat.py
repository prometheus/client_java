#!/usr/bin/env python3

from __future__ import annotations

import os
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
DEFAULT_JMX_EXPORTER_REF = (
    os.environ.get("JMX_EXPORTER_REF")
    or os.environ.get("DEFAULT_JMX_EXPORTER_REF")
    or "main"
)
DEFAULT_MAVEN_MODULES = os.environ.get(
    "JMX_EXPORTER_MAVEN_MODULES",
    "jmx_prometheus_common,jmx_prometheus_javaagent,jmx_prometheus_standalone",
)
DEFAULT_PROM_VERSION = os.environ.get("PROM_VERSION")


def run_cmd(cmd: list[str], cwd: Optional[Path] = None) -> None:
    subprocess.run(cmd, cwd=cwd, check=True)


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
            "-DskipTests",
            "-Dcoverage.skip=true",
            "-Dcheckstyle.skip=true",
            "-Dwarnings=-nowarn",
        ],
        cwd=root_dir,
    )


def run_maven_test(
    test_selector: Optional[str] = None,
    jmx_exporter_dir: Path = DEFAULT_JMX_EXPORTER_DIR,
    maven_modules: str = DEFAULT_MAVEN_MODULES,
    prom_version: Optional[str] = None,
) -> None:
    if prom_version is None:
        prom_version = get_prom_version()
    cmd = [
        "./mvnw",
        "-B",
        "-pl",
        maven_modules,
        "-am",
        f"-Dprometheus.metrics.version={prom_version}",
        "-Djacoco.skip=true",
    ]
    if test_selector:
        cmd.append(f"-Dtest={test_selector}")
    cmd.append("test")
    run_cmd(cmd, cwd=jmx_exporter_dir)

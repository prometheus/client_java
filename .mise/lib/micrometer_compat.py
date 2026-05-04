#!/usr/bin/env python3

from __future__ import annotations

import os
import subprocess
from pathlib import Path
from typing import Optional


ROOT_DIR = Path(__file__).resolve().parents[2]
DEFAULT_MICROMETER_DIR = Path(
    os.environ.get("MICROMETER_DIR", "/tmp/micrometer-compat")
)
DEFAULT_MICROMETER_REMOTE = os.environ.get("MICROMETER_REMOTE", "origin")
DEFAULT_MICROMETER_REF = os.environ.get("MICROMETER_REF", "main")
DEFAULT_INIT_SCRIPT = Path(
    os.environ.get("MICROMETER_INIT_SCRIPT", "/tmp/micrometer-prom-local.init.gradle")
)
DEFAULT_PROM_VERSION = os.environ.get("PROM_VERSION", "1.6.1")


def run_cmd(cmd: list[str], cwd: Optional[Path] = None) -> None:
    subprocess.run(cmd, cwd=cwd, check=True)


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


def write_init_script(
    init_script: Path = DEFAULT_INIT_SCRIPT, prom_version: str = DEFAULT_PROM_VERSION
) -> None:
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
                details.because('Use local prom_client_java artifacts for downstream compatibility testing')
            }}
        }}
    }}
}}
""",
        encoding="utf-8",
    )


def prepare_repo(
    micrometer_dir: Path = DEFAULT_MICROMETER_DIR,
    remote: str = DEFAULT_MICROMETER_REMOTE,
    ref: str = DEFAULT_MICROMETER_REF,
) -> None:
    if (micrometer_dir / ".git").is_dir():
        check_clean_worktree(micrometer_dir)
        run_cmd(["git", "fetch", remote, ref], cwd=micrometer_dir)
    else:
        run_cmd(
            [
                "git",
                "clone",
                "https://github.com/micrometer-metrics/micrometer.git",
                str(micrometer_dir),
            ]
        )
        run_cmd(["git", "fetch", remote, ref], cwd=micrometer_dir)
    run_cmd(
        ["git", "checkout", "-B", "codex-micrometer-compat", "FETCH_HEAD"],
        cwd=micrometer_dir,
    )


def install_local_artifacts(root_dir: Path = ROOT_DIR) -> None:
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

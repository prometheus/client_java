#!/usr/bin/env python3

# [MISE] description="Run Micrometer Prometheus registry tests against a target Micrometer ref"
# [MISE] alias="micrometer:test"

from pathlib import Path
import sys


ROOT = Path(__file__).resolve().parents[2]
if str(ROOT / "lib") not in sys.path:
    sys.path.insert(0, str(ROOT / "lib"))


def main() -> int:
    from micrometer_compat import (
        install_local_artifacts,
        prepare_repo,
        run_gradle_test,
        write_init_script,
    )

    install_local_artifacts()
    prepare_repo()
    write_init_script()
    run_gradle_test()
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

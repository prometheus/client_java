#!/usr/bin/env python3

# [MISE] description="Run Micrometer PrometheusMeterRegistryTest against a target Micrometer ref"
# [MISE] alias="micrometer:test-class"

import sys


sys.path.insert(0, ".mise/lib")


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
    run_gradle_test("io.micrometer.prometheusmetrics.PrometheusMeterRegistryTest")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

#!/usr/bin/env python3

# [MISE] description="Run JMX Exporter tests against a target ref"
# [MISE] alias="jmx-exporter:test"

import sys


sys.path.insert(0, ".mise/lib")


def main() -> int:
    from jmx_exporter_compat import (
        install_local_artifacts,
        prepare_repo,
        run_maven_test,
    )

    install_local_artifacts()
    prepare_repo()
    run_maven_test()
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

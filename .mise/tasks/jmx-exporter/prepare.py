#!/usr/bin/env python3

# [MISE] description="Install local artifacts and check out a target JMX Exporter ref"
# [MISE] alias="jmx-exporter:prepare"

import sys


sys.path.insert(0, ".mise/lib")


def main() -> int:
    from jmx_exporter_compat import install_local_artifacts, prepare_repo

    install_local_artifacts()
    prepare_repo()
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

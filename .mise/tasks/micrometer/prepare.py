#!/usr/bin/env python3

# [MISE] description="Install local artifacts and check out a target Micrometer ref"
# [MISE] alias="micrometer:prepare"

import sys


sys.path.insert(0, ".mise/lib")


def main() -> int:
    from micrometer_compat import (
        install_local_artifacts,
        prepare_repo,
        write_init_script,
    )

    install_local_artifacts()
    prepare_repo()
    write_init_script()
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

#!/usr/bin/env bash

#MISE description="Build release package"
#MISE env={TAG = "1.5.0-SNAPSHOT"}

set -euo pipefail

VERSION=${TAG#v}

./scripts/set-version.sh "$VERSION"
mvn -B package -P 'release,!default' -Dmaven.test.skip=true

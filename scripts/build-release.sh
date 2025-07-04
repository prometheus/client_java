#!/usr/bin/env bash

set -euo pipefail

VERSION=${TAG#v}

./scripts/set-version.sh "$VERSION"
mvn -B package -P 'release,!default' -Dmaven.test.skip=true -Drelease=true

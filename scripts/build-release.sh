#!/usr/bin/env bash

set -euo pipefail

VERSION=${TAG#v}

./setVersion "$VERSION"
mvn -B package -P release -Dmaven.test.skip=true

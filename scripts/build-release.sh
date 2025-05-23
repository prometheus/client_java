#!/usr/bin/env bash

set -euo pipefail

VERSION=${TAG#v}

mvn -B package -P release -Drevision="$VERSION" -Dmaven.test.skip=true

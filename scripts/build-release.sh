#!/usr/bin/env bash

set -euo pipefail

VERSION=${TAG#v}

mvn versions:set -DnewVersion="$VERSION"
mvn -B package -P release -Dmaven.test.skip=true

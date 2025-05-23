#!/usr/bin/env bash

set -euo pipefail

VERSION=${TAG#v}

mvn versions:set -P setVersion -DnewVersion="$VERSION"
mvn -B package -P release -Dmaven.test.skip=true

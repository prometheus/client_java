#!/usr/bin/env bash

set -euo pipefail

VERSION=${TAG#v}

mvn versions:set -DnewVersion="$VERSION"
mvn -B package -P 'release,!default' -Dmaven.test.skip=true

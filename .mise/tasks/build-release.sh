#!/usr/bin/env bash

#MISE description="Build release package"
#USAGE arg "<tag>" env="TAG" default="1.5.0-SNAPSHOT"

set -euo pipefail

# shellcheck disable=SC2154 # is set by mise
VERSION=${usage_tag#v}

mise run set-version "$VERSION"
mvn -B package \
  -P 'release,!default,!examples-and-integration-tests' \
  -Dmaven.test.skip=true \
  -Dgpg.skip=true

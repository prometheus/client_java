#!/usr/bin/env bash

#MISE description="Build release package"
#MISE arg "<tag>" env="TAG" default="1.5.0-SNAPSHOT"

set -euo pipefail

# shellcheck disable=SC2154 # is set by mise
VERSION=${usage_tag#v}

mise run set-version "$VERSION"
mvn -B package -P 'release,!default' -Dmaven.test.skip=true

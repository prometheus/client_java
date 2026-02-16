#!/usr/bin/env bash

#MISE description="Build release package"
#USAGE arg "[tag]" env="TAG"

set -euo pipefail

PARENT_POM="prometheus-metrics-parent/pom.xml"
CURRENT_VERSION=$(sed -n 's/.*<version>\(.*-SNAPSHOT\)<\/version>.*/\1/p' "$PARENT_POM" | head -1)

if [[ -z "$CURRENT_VERSION" ]]; then
	echo "ERROR: could not find SNAPSHOT version in $PARENT_POM" >&2
	exit 1
fi

# shellcheck disable=SC2154 # is set by mise
VERSION=${usage_tag:-$CURRENT_VERSION}
VERSION=${VERSION#v}

find . -name 'pom.xml' -exec \
	sed -i "s/<version>${CURRENT_VERSION}<\/version>/<version>${VERSION}<\/version>/g" {} +
mvn -B package -P 'release,!default,!examples-and-integration-tests' -Dmaven.test.skip=true -Dgpg.skip=true

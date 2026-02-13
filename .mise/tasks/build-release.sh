#!/usr/bin/env bash

#MISE description="Build release package"
#USAGE arg "[tag]" env="TAG"

set -euo pipefail

# shellcheck disable=SC2154 # is set by mise
if [[ -z "${usage_tag:-}" ]]; then
	PARENT_POM="prometheus-metrics-parent/pom.xml"
	usage_tag=$(sed -n 's/.*<version>\(.*-SNAPSHOT\)<\/version>.*/\1/p' "$PARENT_POM" | head -1)
	if [[ -z "$usage_tag" ]]; then
		echo "ERROR: could not find SNAPSHOT version in $PARENT_POM" >&2
		exit 1
	fi
fi

VERSION=${usage_tag#v}

mise run set-version "$VERSION"
mvn -B package -P 'release,!default,!examples-and-integration-tests' -Dmaven.test.skip=true -Dgpg.skip=true

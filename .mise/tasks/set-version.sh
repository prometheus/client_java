#!/usr/bin/env bash

#MISE description="Update version in all pom.xml files"
#USAGE arg "<version>" help="new version"

set -euo pipefail

PARENT_POM="prometheus-metrics-parent/pom.xml"
CURRENT_VERSION=$(sed -n 's/.*<version>\(.*-SNAPSHOT\)<\/version>.*/\1/p' "$PARENT_POM" | head -1)

if [[ -z "$CURRENT_VERSION" ]]; then
	echo "ERROR: could not find SNAPSHOT version in $PARENT_POM" >&2
	exit 1
fi

# shellcheck disable=SC2154 # is set by mise
find . -name 'pom.xml' -exec \
	sed -i "s/<version>${CURRENT_VERSION}<\/version>/<version>$usage_version<\/version>/g" {} +

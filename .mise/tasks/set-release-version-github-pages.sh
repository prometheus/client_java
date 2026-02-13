#!/usr/bin/env bash

#MISE description="Set release version in all GitHub Pages docs"

set -euox pipefail

version=$(git tag -l | grep 'v' | sort | tail -1 | sed 's/v//')
otelVersion=$(grep -oP '<otel.instrumentation.version>\K[^<]+' pom.xml | sed 's/-alpha$//')

find ./docs/content -name '*.md' \
	-exec sed -i "s/\$version/$version/g" {} + \
	-exec sed -i "s/\$otelVersion/$otelVersion/g" {} +

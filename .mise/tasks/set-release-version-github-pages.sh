#!/usr/bin/env bash

#MISE description="Set release version in GitHub Pages quickstart.md"

set -euox pipefail

version=$(git tag -l | grep 'v' | sort | tail -1 | sed 's/v//')
marker="\$version"
sed -i "s/$marker/$version/g" docs/content/getting-started/quickstart.md

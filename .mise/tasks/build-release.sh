#!/usr/bin/env bash

#MISE description="Build release package"

set -euo pipefail

mvn -B package -P 'release,!default,!examples-and-integration-tests' \
	-Dmaven.test.skip=true -Dgpg.skip=true

#!/usr/bin/env bash

#MISE description="Run GitHub Super Linter on the repository"

set -euo pipefail

# Super-linter doesn't publish ARM64 images, so force amd64 on Apple Silicon
PLATFORM_FLAG=""
if [[ "$(uname -m)" == "arm64" ]]; then
	PLATFORM_FLAG="--platform linux/amd64"
fi

# shellcheck disable=SC2086 # Intentional word splitting for PLATFORM_FLAG
docker pull $PLATFORM_FLAG ghcr.io/super-linter/super-linter:latest

# shellcheck disable=SC2086 # Intentional word splitting for PLATFORM_FLAG
docker run --rm \
	$PLATFORM_FLAG \
	-e RUN_LOCAL=true \
	-e DEFAULT_BRANCH=main \
	--env-file ".github/super-linter.env" \
	-v "$(pwd)":/tmp/lint \
	ghcr.io/super-linter/super-linter:latest

#!/usr/bin/env bash

#MISE description="Run GitHub Super Linter on the repository"

set -euo pipefail

# Super-linter doesn't publish ARM64 images, so force amd64 on Apple Silicon
PLATFORM_FLAG=""
if [[ "$(uname -m)" == "arm64" ]]; then
	PLATFORM_FLAG="--platform linux/amd64"
fi

# renovate: datasource=docker depName=ghcr.io/super-linter/super-linter
SUPER_LINTER_VERSION="v8.3.2@sha256:e9d1895a1bdc1f9d9df41f688b27aa891743f23f9fae0f22a3e25eeda8f102db"

# shellcheck disable=SC2086 # Intentional word splitting for PLATFORM_FLAG
docker pull $PLATFORM_FLAG "ghcr.io/super-linter/super-linter:${SUPER_LINTER_VERSION}"

# shellcheck disable=SC2086 # Intentional word splitting for PLATFORM_FLAG
docker run --rm \
	$PLATFORM_FLAG \
	-e RUN_LOCAL=true \
	-e DEFAULT_BRANCH=main \
	--env-file ".github/super-linter.env" \
	-v "$(pwd)":/tmp/lint \
	"ghcr.io/super-linter/super-linter:${SUPER_LINTER_VERSION}"

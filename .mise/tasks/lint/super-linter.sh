#!/usr/bin/env bash

#MISE description="Run GitHub Super Linter on the repository"

set -euo pipefail

# renovate: datasource=docker depName=ghcr.io/super-linter/super-linter
SUPER_LINTER_VERSION="v8.3.2@sha256:e9d1895a1bdc1f9d9df41f688b27aa891743f23f9fae0f22a3e25eeda8f102db"

# Super-linter doesn't publish ARM64 images, so always use amd64
docker pull --platform linux/amd64 "ghcr.io/super-linter/super-linter:${SUPER_LINTER_VERSION}"

docker run --rm \
	--platform linux/amd64 \
	-e RUN_LOCAL=true \
	-e DEFAULT_BRANCH=main \
	--env-file ".github/super-linter.env" \
	-v "$(pwd)":/tmp/lint \
	"ghcr.io/super-linter/super-linter:${SUPER_LINTER_VERSION}"

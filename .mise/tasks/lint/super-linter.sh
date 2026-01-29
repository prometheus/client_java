#!/usr/bin/env bash

#MISE description="Run GitHub Super Linter on the repository"

set -euo pipefail

# renovate: datasource=docker depName=ghcr.io/super-linter/super-linter
SUPER_LINTER_VERSION="v8.4.0@sha256:c5e3307932203ff9e1e8acfe7e92e894add6266605b5d7fb525fb371a59a26f4"

# Super-linter doesn't publish ARM64 images, so always use amd64
docker pull --platform linux/amd64 "ghcr.io/super-linter/super-linter:${SUPER_LINTER_VERSION}"

docker run --rm \
	--platform linux/amd64 \
	-e RUN_LOCAL=true \
	-e DEFAULT_BRANCH=main \
	--env-file ".github/super-linter.env" \
	-v "$(pwd)":/tmp/lint \
	"ghcr.io/super-linter/super-linter:${SUPER_LINTER_VERSION}"

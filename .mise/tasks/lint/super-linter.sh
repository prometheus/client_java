#!/usr/bin/env bash

#MISE description="Run GitHub Super Linter on the repository"

set -euo pipefail

docker pull ghcr.io/super-linter/super-linter:latest

docker run --rm \
	-e RUN_LOCAL=true \
	-e DEFAULT_BRANCH=main \
	--env-file ".github/super-linter.env" \
	-v "$(pwd)":/tmp/lint \
	ghcr.io/super-linter/super-linter:latest

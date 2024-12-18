#!/usr/bin/env bash

set -euo pipefail

cd oats/yaml
go install github.com/onsi/ginkgo/v2/ginkgo
export TESTCASE_TIMEOUT=5m
export TESTCASE_BASE_PATH=../../examples
ginkgo

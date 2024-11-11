#!/usr/bin/env bash

set -euo pipefail

./mvnw clean install -DskipTests
cd integration-tests/it-spring-boot-smoke-test
../../mvnw  test -PnativeTest

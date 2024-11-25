#!/usr/bin/env bash

set -euo pipefail

TAG=$1
VERSION=${TAG#v}

mvn versions:set -DnewVersion=$VERSION
mvn install -DskipTests=true # to find the new version in the next step
cd integration-tests/it-spring-boot-smoke-test
mvn versions:set -DnewVersion=$VERSION
mvn -B package -P release -Dmaven.test.skip=true

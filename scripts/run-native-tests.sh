#!/usr/bin/env bash

set -euo pipefail

./mvnw clean install -DskipTests

PROJECT_VERSION=$(sed -nE 's/<version>(.*)<\/version>/\1/p' pom.xml | head -1 | xargs)

cd integration-tests/it-spring-boot-smoke-test

# replace the version in the pom.xml
sed -i "s/<version>0.1<\/version>/<version>$PROJECT_VERSION<\/version>/" pom.xml

../../mvnw  test -PnativeTest

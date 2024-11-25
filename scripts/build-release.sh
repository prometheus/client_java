#!/usr/bin/env bash

set -euo pipefail

TAG=$1
VERSION=${TAG#v}

mvn versions:set -DnewVersion=$VERSION
mvn -B install -P release -DskipTests=true

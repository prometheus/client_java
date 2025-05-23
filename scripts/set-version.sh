#!/usr/bin/env bash

set -euo pipefail

if [ $# -ne 1 ]; then
    echo "Usage: $0 <newVersion>"
    exit 1
fi
newVersion=$1

mvn versions:set -DnewVersion="$newVersion" -DgenerateBackupPoms=false
pushd prometheus-metrics-parent
mvn versions:set -DnewVersion="$newVersion" -DgenerateBackupPoms=false
popd
pushd prometheus-metrics-bom
mvn versions:set -DnewVersion="$newVersion" -DgenerateBackupPoms=false
popd

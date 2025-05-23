#!/usr/bin/env bash

set -euo pipefail

if [ $# -ne 1 ]; then
    echo "Usage: $0 <newVersion>"
    exit 1
fi
newVersion=$1

# for each module into aggregator pom
grep "\<module\>" pom.xml | sed 's/<\/module>//g' | sed 's/.*<module>//g' | sed 's/.*\///g' | while IFS= read -r module
do
    # set the version of the module
    # and update reference to this module into others modules
    mvn versions:set -DgenerateBackupPoms=false -DartifactId="$module" \
        -DnewVersion="$newVersion" -DupdateMatchingVersions=true
done
# set the version of the aggregator pom
mvn versions:set -DnewVersion="$newVersion" -DgenerateBackupPoms=false

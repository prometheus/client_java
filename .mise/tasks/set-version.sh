#!/usr/bin/env bash

#MISE description="Update version in all pom.xml files"
#USAGE arg "<version>" help="new version"

set -euo pipefail

# replace all occurrences '<version>1.5.0-SNAPSHOT</version>' with
# '<version>$usage_version</version>' in all pom.xml files in the current directory and
# subdirectories

# shellcheck disable=SC2154 # is set by mise
find . -name 'pom.xml' -exec \
	sed -i "s/<version>1.5.0-SNAPSHOT<\/version>/<version>$usage_version<\/version>/g" {} +

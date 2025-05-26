#!/usr/bin/env bash

set -euo pipefail

VERSION=$1

if [ -z "$VERSION" ]; then
	echo "Usage: $0 <version>"
	exit 1
fi

# replace all occurrences '<version>1.4.0-SNAPSHOT</version>' with '<version>$VERSION</version>'
# in all pom.xml files in the current directory and subdirectories

find . -name 'pom.xml' -exec \
	sed -i "s/<version>1.4.0-SNAPSHOT<\/version>/<version>$VERSION<\/version>/g" {} +

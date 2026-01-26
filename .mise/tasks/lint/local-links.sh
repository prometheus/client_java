#!/usr/bin/env bash
#MISE description="Lint links in local files"

set -e

#USAGE arg "<file>" var=#true help="files to check" default="."

for f in $usage_file; do
    echo "Checking links in file: $f"
done

lychee --verbose --scheme file --include-fragments --config .github/config/lychee.toml $usage_file

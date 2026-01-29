#!/usr/bin/env bash
#MISE description="Lint links in modified files"

set -e

#USAGE flag "--base <base>" help="base branch to compare against (default: origin/main)" default="origin/main"
#USAGE flag "--head <head>" help="head branch to compare against (empty for local changes) (default: empty)" default=""

# shellcheck disable=SC2154
if [ "$usage_head" = "''" ]; then
	usage_head=""
fi

# Check if lychee config was modified
# - because usage_head may be empty
# shellcheck disable=SC2086,SC2154
config_modified=$(git diff --name-only --merge-base "$usage_base" $usage_head |
	grep -E '^(\.github/config/lychee\.toml|\.mise/tasks/lint/.*|mise\.toml)$' || true)

if [ -n "$config_modified" ]; then
	echo "config changes, checking all files."
	mise run lint:links
else
	# Using lychee's default extension filter here to match when it runs against all files
	# Note: --diff-filter=d filters out deleted files
	# - because usage_head may be empty
	# shellcheck disable=SC2086
	modified_files=$(git diff --name-only --diff-filter=d "$usage_base" $usage_head |
		grep -E '\.(md|mkd|mdx|mdown|mdwn|mkdn|mkdown|markdown|html|htm|txt)$' |
		tr '\n' ' ' || true)

	if [ -z "$modified_files" ]; then
		echo "No modified files, skipping link linting."
		exit 0
	fi

	# shellcheck disable=SC2086
	mise run lint:links $modified_files
fi

#!/usr/bin/env bash
# Refresh docs/apidiffs/current_vs_latest/ from the japicmp reports produced by
# `mvn verify -P api-diff`.
#
# Each StableApi module gets one committed <module>.txt describing how its
# published API surface differs from the baseline release
# (<api.diff.baseline.version> in pom.xml). Committing these makes every API
# change visible in the pull request diff. The CI check fails when the
# regenerated files differ from what is committed.
#
# The layout and format match opentelemetry-java's docs/apidiffs: the
# "Comparing ... .jar against ... .jar" header is kept, while the constant
# ignore-missing-classes warning and the semantic-versioning suggestion are
# dropped as noise.
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
out_dir="$repo_root/docs/apidiffs/current_vs_latest"

shopt -s nullglob globstar

reports=("$repo_root"/**/target/japicmp/api-diff.diff)
if [ "${#reports[@]}" -eq 0 ]; then
	echo "No japicmp reports found. Run 'mvn verify -P api-diff' first." >&2
	exit 1
fi

rm -rf "$out_dir"
mkdir -p "$out_dir"

for report in "${reports[@]}"; do
	# report path: <repo>/<module path>/target/japicmp/api-diff.diff
	module="$(basename "$(dirname "$(dirname "$(dirname "$report")")")")"
	grep -vE '^(WARNING: You are using the option|Semantic versioning suggestion)' \
		"$report" >"$out_dir/$module.txt"
done

echo "Wrote ${#reports[@]} API diff report(s) to docs/apidiffs/current_vs_latest/."

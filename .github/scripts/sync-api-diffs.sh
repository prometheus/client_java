#!/usr/bin/env bash
# Refresh docs/apidiffs/ from the japicmp reports produced by `mvn verify -P api-diff`.
#
# Each StableApi module gets one committed <module>.diff describing how its
# published API surface differs from the baseline release
# (<api.diff.baseline.version> in pom.xml). Committing these makes every API
# change visible in the pull request diff. The CI check fails when the
# regenerated files differ from what is committed.
#
# The two-line japicmp preamble (the "Comparing ...SNAPSHOT.jar against
# ...<baseline>.jar" line and the constant ignore-missing-classes warning) is
# stripped so the files only churn on real API changes, not on every version or
# snapshot bump.
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
out_dir="$repo_root/docs/apidiffs"

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
	grep -vE '^(Comparing source compatibility of|WARNING: You are using the option)' \
		"$report" >"$out_dir/$module.diff"
done

echo "Wrote ${#reports[@]} API diff report(s) to docs/apidiffs/."

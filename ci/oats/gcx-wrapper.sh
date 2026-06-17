#!/usr/bin/env bash
set -euo pipefail

real_gcx="${REAL_GCX_BIN:-gcx}"
args=()
skip_next=false
for arg in "$@"; do
	if [ "$skip_next" = true ]; then
		skip_next=false
		continue
	fi
	if [ "$arg" = "--context" ]; then
		skip_next=true
		continue
	fi
	args+=("$arg")
done

for _ in $(seq 1 120); do
	token="$(docker exec lgtm cat /tmp/grafana-sa-token 2>/dev/null || true)"
	if [ -n "$token" ]; then
		export GRAFANA_SERVER="${GRAFANA_SERVER:-http://localhost:3000}"
		export GRAFANA_TOKEN="$token"
		exec "$real_gcx" "${args[@]}"
	fi
	sleep 1
done

printf 'gcx-wrapper: timed out waiting for grafana service-account token\n' >&2
exit 1

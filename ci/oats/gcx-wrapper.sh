#!/usr/bin/env bash
set -euo pipefail

real_gcx="${REAL_GCX_BIN:-gcx}"

for _ in $(seq 1 180); do
	token="$(docker exec lgtm cat /tmp/grafana-sa-token 2>/dev/null || true)"
	if [ -n "$token" ]; then
		export GRAFANA_SERVER="${GRAFANA_SERVER:-http://localhost:3000}"
		export GRAFANA_TOKEN="$token"
		exec "$real_gcx" "$@"
	fi
	sleep 1
done

printf 'gcx-wrapper: timed out waiting for grafana service-account token\n' >&2
exit 1

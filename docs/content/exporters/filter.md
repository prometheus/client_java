---
title: Filter
weight: 3
---

All exporters support a `name[]` URL parameter for querying only specific metric names. Examples:

- `/metrics?name[]=jvm_threads_current` will query the metric named `jvm_threads_current`.
- `/metrics?name[]=jvm_threads_current&name[]=jvm_threads_daemon` will query two metrics,
  `jvm_threads_current` and `jvm_threads_daemon`.

Add the following to the scape job configuration in `prometheus.yml`
to make the Prometheus server send the `name[]` parameter:

```yaml
params:
  name[]:
    - jvm_threads_current
    - jvm_threads_daemon
```

## Query parameter limits

For safety, exporters limit the query string to 65,536 characters and accept at most 1,024
query parameters. The parameter limit counts every `&`-separated pair, including repeated
parameters and empty pairs. These are fixed implementation limits and cannot be changed through
runtime configuration.

If a request exceeds either limit or contains invalid percent-encoding, the `/metrics` endpoint
returns HTTP `400 Bad Request` with the plain-text response `Invalid query parameters`.

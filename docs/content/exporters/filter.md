---
title: Filter
weight: 2
---

All exporters support a `name[]` URL parameter for querying only specific metric names. Examples:

* `/metrics?name[]=jvm_threads_current` will query the metric named `jvm_threads_current`.
* `/metrics?name[]=jvm_threads_current&name[]=jvm_threads_daemon` will query two metrics, `jvm_threads_current` and `jvm_threads_daemon`.

Add the following to the scape job configuration in `prometheus.yml` to make the Prometheus server send the `name[]` parameter:

```yaml
params:
    name[]:
        - jvm_threads_current
        - jvm_threads_daemon
```

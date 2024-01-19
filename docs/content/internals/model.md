---
title: Model
weight: 1
---

The illustration below shows the internal architecture of the Prometheus Java client library.

![Internal architecture of the Prometheus Java client library](/client_java/images/model.png)

prometheus-metrics-core
-----------------------

This is the user facing metrics library, implementing the core metric types, like [Counter](/client_java/api/io/prometheus/metrics/core/metrics/Counter.html), [Gauge](/client_java/api/io/prometheus/metrics/core/metrics/Gauge.html) [Histogram](/client_java/api/io/prometheus/metrics/core/metrics/Histogram.html), and so on.

All metric types implement the [Collector](/client_java/api/io/prometheus/metrics/model/registry/Collector.html) interface, i.e. they provide a [collect()](/client_java/api/io/prometheus/metrics/model/registry/Collector.html#collect()) method to produce snapshots.

prometheus-metrics-model
------------------------

The model is an internal library, implementing read-only immutable snapshots. These snapshots are returned by the [Collector.collect()](/client_java/api/io/prometheus/metrics/model/registry/Collector.html#collect()) method.

There is no need for users to use `prometheus-metrics-model` directly. Users should use the API provided by `prometheus-metrics-core`, which includes the core metrics as well as callback metrics.

However, maintainers of 3rd party metrics libraries might want to use `prometheus-metrics-model` if they want to add Prometheus exposition formats to their metrics library.

exporters and exposition formats
--------------------------------

The `prometheus-metrics-exposition-formats` module converts snapshots to Prometheus exposition formats, like text format, OpenMetrics text format, or Prometheus protobuf format.

The exporters like `prometheus-metrics-exporter-httpserver` or `prometheus-metrics-exporter-servlet-jakarta` use this to convert snapshots into the right format depending on the `Accept` header in the scrape request.

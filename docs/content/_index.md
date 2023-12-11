---
title: "client_java"
---

This is the documentation for the [Prometheus Java client library](https://github.com/prometheus/client_java) version 1.0.0 and higher.

The main new features of the 1.0.0 release are:

* **Prometheus native histograms:** Support for the new Prometheus histogram type.
* **OpenTelemetry Exporter:** Push metrics in OTLP format to an OpenTelemetry endpoint.
* **Runtime configuration:** Configure metrics, exporters, and more at runtime using a properties file or system properties.

**Documentation and Examples**

In addition to this documentation page we created an [examples/](https://github.com/prometheus/client_java/tree/main/examples) directory with end-to-end scenarios (Docker compose) illustrating new features.

**Performance Benchmarks**

Initial performance benchmarks are looking great: All core metric types (including native histograms) allow concurrent updates, so if you instrument a performance critical Web service that utilizes all processor cores in parallel the metrics library will not introduce additional synchronization. See Javadoc comments in [benchmarks/](https://github.com/prometheus/client_java/tree/main/benchmarks) for benchmark results.

**More Info**

The Grafana Labs Blog has a post [Introducing the Prometheus Java Client 1.0.0](https://grafana.com/blog/2023/09/27/introducing-the-prometheus-java-client-1.0.0/) with a good overview of the release.

There will also be a presentation at the [PromCon](https://promcon.io) conference on 29 Sep 2023. Tune in to the live stream on [https://promcon.io](https://promcon.io) or watch the recording on YouTube.

**For users of the 0.16.0 version and older**

Updating to the 1.0.0 version is a breaking change. However, there's a `prometheus-metrics-simpleclient-bridge` module available that allows you to use your existing simpleclient 0.16.0 metrics with the new 1.0.0 `PrometheusRegistry`. So you don't need to upgrade your instrumentation code, you can keep using your existing metrics. See the [compatibility > simpleclient](https://prometheus.github.io/client_java/migration/simpleclient/) in the menu on the left.

The pre 1.0.0 code is now maintained on the [simpleclient](https://github.com/prometheus/client_java/tree/simpleclient) feature branch.

Not all `simpleclient` modules from 0.16.0 are included in the initial 1.0.0 release. Over the next couple of weeks we will work on porting the remaining modules, starting with `pushgateway` and the Servlet filter.

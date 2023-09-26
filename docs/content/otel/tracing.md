---
title: Tracing
weight: 2
---

It is a common scenario that you use the Prometheus Java client for metrics, and the OpenTelemetry Java instrumentation agent for tracing.

The Prometheus Java client has some awesome features under the hood to support integration with OpenTelemetry tracing:

* `service.name` and `service.instance.id` are used in OpenTelemetry to uniquely identify a service instance. If an OpenTelemetry Java agent is attached, the Prometheus library will automatically use the same `service.name` and `service.instance.id` as the agent when pushing metrics in OpenTelemetry format. That way the monitoring backend will see that the metrics and the traces are coming from the same instance.
* Exemplars are added automatically if a Prometheus metric is updated in the context of a distributed OpenTelemetry trace.
* If a Span is used as an Exemplar, the Span is marked with the Span attribute `exemplar="true"`. This can be used in the OpenTelemetry's sampling policy to make sure Exemplars are always sampled.

TODO: We will add more information on integration OTel tracing with Prometheus metrics here.
In the meantime, have a look at the tail sampling end-to-end example in [examples/example-exemplars-tail-sampling](https://github.com/prometheus/client_java/tree/main/examples/example-exemplars-tail-sampling).

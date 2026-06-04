---
title: Tracing
weight: 2
---

OpenTelemetry’s
[vision statement](https://github.com/open-telemetry/community/blob/main/mission-vision-values.md)
says that
[telemetry should be loosely coupled](https://github.com/open-telemetry/community/blob/main/mission-vision-values.md#telemetry-should-be-loosely-coupled),
allowing end users to pick and choose from the pieces they want without having to bring in the rest
of the project, too. In that spirit, you might choose to instrument your Java application with the
Prometheus Java client library for metrics, and attach the
[OpenTelemetry Java agent](https://github.com/open-telemetry/opentelemetry-java-instrumentation/)
to get distributed tracing.

First, if you attach the
[OpenTelemetry Java agent](https://github.com/open-telemetry/opentelemetry-java-instrumentation/)
you might want to turn off OTel's built-in metrics, because otherwise you get metrics from both the
Prometheus Java client library and the OpenTelemetry agent (technically it's no problem to get both
metrics, it's just not a common use case).

```bash
# This will tell the OpenTelemetry agent not to send metrics, just traces and logs.
export OTEL_METRICS_EXPORTER=none
```

Now, start your application with the OpenTelemetry Java agent attached for traces and logs.

```bash
java -javaagent:path/to/opentelemetry-javaagent.jar -jar myapp.jar
```

With the OpenTelemetry Java agent attached, the Prometheus client library will do a lot of magic
under the hood.

- `service.name` and `service.instance.id` are used in OpenTelemetry to uniquely identify a service
  instance. The Prometheus client library will automatically use the same `service.name` and
  `service.instance.id` as the agent when pushing metrics in OpenTelemetry format. That way the
  monitoring backend will see that the metrics and the traces are coming from the same instance.
- Exemplars are added automatically if a Prometheus metric is updated in the context of a
  distributed OpenTelemetry trace.
- If a Span is used as an Exemplar, the Span is marked with the Span attribute `exemplar="true"`.
  This can be used in the OpenTelemetry's sampling policy to make sure Exemplars are always sampled.

Here's more context on the `exemplar="true"` Span attribute: Many users of tracing libraries don't
keep 100% of their trace data, because traces are very repetitive. It is very common to sample only
10% of traces and discard 90%. However, this can be an issue with Exemplars: In 90% of the cases
Exemplars would point to a trace that has been thrown away.

To solve this, the Prometheus Java client library annotates each Span that has been used as an
Exemplar with the `exemplar="true"` Span attribute.

The sampling policy in the OpenTelemetry collector can be configured to keep traces with this
attribute. There's no risk that this results in a significant increase in trace data, because new
Exemplars are only selected every
[`minRetentionPeriodSeconds`]({{< relref "../config/config.md#exemplar-properties" >}}) seconds.

Here's an example of how to configure OpenTelemetry's
[tail sampling processor](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/processor/tailsamplingprocessor/)
to sample all Spans marked with `exemplar="true"`, and then discard 90% of the traces:

```yaml
policies:
  [
    {
      name: keep-exemplars,
      type: string_attribute,
      string_attribute: { key: "exemplar", values: ["true"] },
    },
    { name: keep-10-percent, type: probabilistic, probabilistic: { sampling_percentage: 10 } },
  ]
```

The [examples/example-exemplar-tail-sampling/](https://github.com/prometheus/client_java/tree/main/examples/example-exemplars-tail-sampling)
directory has a complete end-to-end example, with a distributed Java application with two services,
an OpenTelemetry collector, Prometheus, Tempo as a trace database, and Grafana dashboards. Use
docker-compose as described in the example's readme to run the example and explore the results.

## Adding custom labels to exemplars

Automatically-sampled exemplars carry the `trace_id` and `span_id` labels. You can attach
additional, custom labels (for example an internal identifier) to every automatically-sampled
exemplar. There are two options.

### Global (all metrics)

Register a global supplier to add custom labels to the exemplars of _all_ metrics, including
metrics registered by third-party libraries that you do not control. This is the right option when
you cannot modify the code that creates the metric:

```java
ExemplarLabelsSupplier.setExemplarLabelsSupplier(
    () -> Labels.of("management_id", currentManagementId()));
```

### Per metric

If you only want the extra labels on a specific metric you define yourself, use the builder:

```java
Counter counter =
    Counter.builder()
        .name("requests_total")
        .exemplarLabelsSupplier(() -> Labels.of("management_id", currentManagementId()))
        .build();
```

### Notes

- The supplier is invoked on the (rate-limited) hot path each time an exemplar is sampled, so it
  should be cheap. It may return dynamic, request-scoped values (e.g. read from a thread-local).
- Custom labels are only added when a valid, sampled span context is present; the supplier never
  causes an exemplar to be created on its own.
- Precedence on a label-name collision: the reserved `trace_id`/`span_id` labels always win, then
  the per-metric supplier, then the global supplier. Colliding labels are silently dropped.
- If the supplier throws, the exception is swallowed and the exemplar is created without the
  additional labels, so a misbehaving supplier never breaks metric collection.

Exemplars with OpenTelemetry's Tail Sampling
--------------------------------------------

## Background: What are Exemplars?

Each Prometheus time series may contain Exemplars. Exemplars are additional metadata attached to a Prometheus time series.
Exemplars are often used to reference trace IDs when distributed tracing is used.
The following shows an example of a histogram in OpenMetrics text format where each non-empty bucket has an Exemplar:

```
# TYPE request_duration_seconds histogram
# UNIT request_duration_seconds seconds
# HELP request_duration_seconds request duration in seconds
request_duration_seconds_bucket{http_status="200",le="0.005"} 0
request_duration_seconds_bucket{http_status="200",le="0.01"} 5 # {span_id="826c6f49a0d6c931",trace_id="d2cf83f9f1fd22f793855edc4ae53320"} 0.007341934 1689971637.765
request_duration_seconds_bucket{http_status="200",le="0.025"} 64 # {span_id="9ae17d91c9822c88",trace_id="320e53f894781db26fa0eb5fb0640426"} 0.014081662 1689971677.851
request_duration_seconds_bucket{http_status="200",le="0.05"} 281 # {span_id="32c9706b1f798fce",trace_id="636a2bb9fe4aabea0f412b8cd6da2290"} 0.047227664 1689971700.484
request_duration_seconds_bucket{http_status="200",le="0.1"} 1203 # {span_id="04902c82f56fb441",trace_id="c5cf7c1a4805e4b7a991c4e99fb38f87"} 0.094949368 1689971706.913
request_duration_seconds_bucket{http_status="200",le="0.25"} 6338 # {span_id="81ffa3cbe3c8bf7e",trace_id="106a0d13ca1578a4c3ae4ec987b7f8f7"} 0.226545183 1689971700.004
request_duration_seconds_bucket{http_status="200",le="0.5"} 10969 # {span_id="b7ac3d0a2b9570c3",trace_id="79b641c3d96bdba5554395e1521ffb13"} 0.439355954 1689971700.997
request_duration_seconds_bucket{http_status="200",le="1.0"} 11180 # {span_id="b0cb8480a8b5c1ac",trace_id="4c57125265c4a7caa83c4d354f94bd40"} 0.625977473 1689971700.263
request_duration_seconds_bucket{http_status="200",le="2.5"} 11209 # {span_id="4bce9aa28f1f8a65",trace_id="7fa30e3153f5129694bd58a23bcc96a8"} 1.529906213 1689971659.228
request_duration_seconds_bucket{http_status="200",le="5.0"} 11236 # {span_id="e629a7d546da77e1",trace_id="20a43851621f908eab359217df2b5eed"} 2.936415405 1689971661.293
request_duration_seconds_bucket{http_status="200",le="10.0"} 11243 # {span_id="024e3119605ab66e",trace_id="84721fdc42ef4d55e8416d3191b4d19e"} 5.163273443 1689971663.080
request_duration_seconds_bucket{http_status="200",le="+Inf"} 11243
request_duration_seconds_count{http_status="200"} 11243
request_duration_seconds_sum{http_status="200"} 2843.3178731140015
```

In Grafana Exemplars can be visualized as little green dots. The following shows an example of the 95th [quantile](https://prometheus.io/docs/prometheus/latest/querying/functions/#histogram_quantile) for the histogram above.

![Screenshot of a Latency Graph with Exemplars](https://github.com/prometheus/client_java/assets/330535/68aada3d-f55b-4a7b-90be-222481f0ec79)

If you move the mouse over an Exemplar, an overlay pops up with a link to a tracing tool like [Tempo](https://github.com/grafana/tempo).

## Automatic Exemplar Generation

The Prometheus Java client library automatically detects OpenTelemetry tracing. The library automatically produces Exemplars with OpenTelemetry trace IDs if applicable.

## Sampling

Distributed tracing produces a lot of data, and it is quite common to apply a sampling strategy to reduce the number of traces.

There are two ways how to do this:

1. _Head-based sampling_ is performed by the tracing library in the application itself.
2. _Tail-based sampling_ is performed by an external infrastructure component like the [OpenTelemetry collector](https://opentelemetry.io/docs/collector/).

When generating Exemplars, the Prometheus Java client libray must make sure that the Exemplar points to a trace that is actually sampled. Otherwise, Exemplar's trace ID will not be available in the monitoring backend, i.e. the Exemplar will link to a non-existing trace ID.

With _head-based sampling_ this works out-of-the-box: The Prometheus client library calls OpenTelemetry's `Span.current().getSpanContext().isSampled()` function and selects Exemplars only if the current Span is sampled.

With _tail-based sampling_ there is a trick: The Prometheus client library adds an attribute `exemplar="true"` to each Span that is used as an Exemplar. The remainder of this README shows how you can configure the `tail_sampling` processor of the OpenTelemetry collector to sample Spans marked with `exemplar="true"`.

## Build the Example

The example is built as part of the `client_java` parent project with

```shell
./mvnw package
```

This should generate two Java services `.../example-greeting-service/target/example-greeting-service.jar` and `.../example-hello-world-app/target/example-hello-world-app.jar`.

## Run the Example

The `docker-compose` file will run the example with the following containers:

* `hello-world-app`: Java service described above, with the OpenTelemetry Java instrumentation agent attached for distrubted tracing.
* `greeting-service`: Java service described above, with the OpenTelemetry Java instrumentation agent attached for distrubted tracing.
* `collector`: OpenTelemetry collector for receiving the distributed traces, performing _tail sampling_, and forwarding them to `tempo`.
* `prometheus`: Prometheus server scraping metrics from `hello-world-app`, `greeting-service`, and `tempo`.
* `tempo`: Trace database.
* `grafana`: Has `prometheus` and `tempo` configured as data sources, and is configured with an example dashboard with Exemplars enabled.
* `k6`: The [k6](https://k6.io/) load test tool for generating 50 requests / second on the Java services.

With the Java services described above available in the `target/` directories, you can run the example with:

```shell
cd example-exemplars-tail-sampling
docker-compose up
```

Grafana will run on [http://localhost:3000](ttp://localhost:3000). The default user is _admin_ with password _admin_.

## Example Dashboard

The example dashboard shows 50 requests / second for the Java serices:

![Screenshot showing the request rate on the Java services](https://github.com/prometheus/client_java/assets/330535/9f8dc92e-c9aa-40b6-8fda-a0f7e98560ba)

The Tempo metrics show that only ~5 traces / second are received:

![Screenshot showing the number of traces per second received by Tempo](https://github.com/prometheus/client_java/assets/330535/5e439ac5-3c5c-4d40-a4cd-6737c2c82dfd)

The reason is that the OpenTelemetry collector is configured to sample only 10% of the traces. Yet, all Exemplars in the latency graph point to traces that actually made it to Tempo.

## OpenTelemetry Collector Config

The OpenTelemetry collector is configured to keep all Spans with the attribute `exemplar="true"`. Probabilistic sampling applies only to the remainder of the Spans.

```yaml
processors:
  batch:
  tail_sampling:
    expected_new_traces_per_sec: 10
    policies:
      [
        {
          name: keep-exemplars,
          type: string_attribute,
          string_attribute: { key: "exemplar", values: [ "true" ] }
        },
        {
          name: keep-10-percent,
          type: probabilistic,
          probabilistic: { sampling_percentage: 10 }
        },
      ]
```

The Prometheus Java client library automatically sets the `exemplar="true"` attribute for each Span that is used as an Exemplar.

The Exemplar sampler of the Prometheus Java client library is rate-limited, the `DEFAULT_MIN_RETENTION_PERIOD_SECONDS` is 7s.
So only one Span per 7 seconds per time series will be marked with `exemplar="true"`.

## Conclusion

The Prometheus Java client library automatically generates Exemplars if OpenTelemetry tracing is detected.
In order to make that work with _tail-based sampling_, the Prometheus Java client library adds an attribute `exemplar="true"` to each Span that's used as an Exemplar.
The `tail_sampling` processor in the OpenTelemetry collector can be configured to always sample Spans marked as `exemplar="true"`.

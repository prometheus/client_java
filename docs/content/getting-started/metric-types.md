---
title: 'Metric Types'
weight: 4
---

The Prometheus Java metrics library implements the metric types defined in the [OpenMetrics](https://openmetrics.io) standard:

{{< toc >}}

Counter
-------

Counter is the most common and useful metric type. Counters can only increase, but never decrease. In the Prometheus query language, the [rate()](https://prometheus.io/docs/prometheus/latest/querying/functions/#rate) function is often used for counters to calculate the average increase per second.

{{< hint type=note >}}
Counter values do not need to be integers. In many cases counters represent a number of events (like the number of requests), and in that case the counter value is an integer. However, counters can also be used for something like "total time spent doing something" in which case the counter value is a floating point number.
{{< /hint >}}

Here's an example of a counter:

```java
Counter serviceTimeSeconds = Counter.builder()
    .name("service_time_seconds_total")
    .help("total time spent serving requests")
    .unit(Unit.SECONDS)
    .register();

serviceTimeSeconds.inc(Unit.millisToSeconds(200));
```

The resulting counter has the value `0.2`. As `SECONDS` is the standard time unit in Prometheus, the `Unit` utility class has methods to convert other time units to seconds.

As defined in [OpenMetrics](https://openmetrics.io/), counter metric names must have the `_total` suffix. If you create a counter without the `_total` suffix the suffix will be appended automatically.

Gauge
-----

Gauges are current measurements, such as the current temperature in Celsius.

```java
Gauge temperature = Gauge.builder()
    .name("temperature_celsius")
    .help("current temperature")
    .labelNames("location")
    .unit(Unit.CELSIUS)
    .register();

temperature.labelValues("Berlin").set(22.3);
```

Histogram
---------

Histograms are for observing distributions, like latency distributions for HTTP services or the distribution of request sizes.
Unlike with counters and gauges, each histogram data point has a complex data structure representing different aspects of the distribution:

* Count: The total number of observations.
* Sum: The sum of all observed values, e.g. the total time spent serving requests.
* Buckets: The histogram buckets representing the distribution.

Prometheus supports two flavors of histograms:

* Classic histograms: Bucket boundaries are explicitly defined when the histogram is created.
* Native histograms (exponential histograms): Infinitely many virtual buckets.

By default, histograms maintain both flavors. Which one is used depends on the scrape request from the Prometheus server.
* By default, the Prometheus server will scrape metrics in OpenMetrics format and get the classic histogram flavor.
* If the Prometheus server is started with `--enable-feature=native-histograms`, it will request metrics in Prometheus protobuf format and ingest the native histogram.
* If the Prometheus server is started with `--enable-feature=native-histogram` and the scrape config has the option `scrape_classic_histograms: true`, it will request metrics in Prometheus protobuf format and ingest both, the classic and the native flavor. This is great for migrating from classic histograms to native histograms.

See [examples/example-native-histogram](https://github.com/prometheus/client_java/tree/1.0.x/examples/example-native-histogram) for an example.

```java
Histogram duration = Histogram.builder()
    .name("http_request_duration_seconds")
    .help("HTTP request service time in seconds")
    .unit(Unit.SECONDS)
    .labelNames("method", "path", "status_code")
    .register();

long start = System.nanoTime();
// do something
duration.labelValues("GET", "/", "200").observe(Unit.nanosToSeconds(System.nanoTime() - start));
```

Histograms implement the [TimerApi](/client_java/api/io/prometheus/metrics/core/datapoints/TimerApi.html) interface, which provides convenience methods for measuring durations.

The histogram builder provides a lot of configuration for fine-tuning the histogram behavior. In most cases you don't need them, defaults are good. The following is an incomplete list showing the most important options:

* `nativeOnly()` / `classicOnly()`: Create a histogram with one representation only.
* `classicBuckets(...)`: Set the classic bucket boundaries. Default buckets are `.005`, `.01`, `.025`, `.05`, `.1`, `.25`, `.5`, `1`, `2.5`, `5`, `and 10`. The default bucket boundaries are designed for measuring request durations in seconds.
* `nativeMaxNumberOfBuckets()`: Upper limit for the number of native histogram buckets. Default is 160. When the maximum is reached, the native histogram automatically reduces resolution to stay below the limit.

See Javadoc for [Histogram.Builder](/client_java/api/io/prometheus/metrics/core/metrics/Histogram.Builder.html) for a complete list of options. Some options can be configured at runtime, see [config](../../config/config).

Histograms and summaries are both used for observing distributions. Therefore, the both implement the `DistributionDataPoint` interface. Using the `DistributionDataPoint` interface directly gives you the option to switch between histograms and summaries later with minimal code changes.

Example of using the `DistributionDataPoint` interface for a histogram without labels:

```java
DistributionDataPoint eventDuration = Histogram.builder()
    .name("event_duration_seconds")
    .help("event duration in seconds")
    .unit(Unit.SECONDS)
    .register();

// The following still works perfectly fine if eventDuration
// is backed by a summary rather than a histogram.
eventDuration.observe(0.2);
```

Example of using the `DistributionDataPoint` interface for a histogram with labels:

```java
Histogram eventDuration = Histogram.builder()
    .name("event_duration_seconds")
    .help("event duration in seconds")
    .labelNames("status")
    .unit(Unit.SECONDS)
    .register();

DistributionDataPoint successfulEvents = eventDuration.labelValues("ok");
DistributionDataPoint erroneousEvents = eventDuration.labelValues("error");

// Like in the example above, the following still works perfectly fine
// if the successfulEvents and erroneousEvents are backed by a summary rather than a histogram.
successfulEvents.observe(0.7);
erroneousEvents.observe(0.2);
```

Summary
-------

Like histograms, summaries are for observing distributions. Each summary data point has a count and a sum like a histogram data point.
However, rather than histogram buckets summaries maintain quantiles.

```java
Summary requestLatency = Summary.builder()
    .name("request_latency_seconds")
    .help("Request latency in seconds.")
    .unit(Unit.SECONDS)
    .quantile(0.5, 0.01)
    .quantile(0.95, 0.005)
    .quantile(0.99, 0.005)
    .labelNames("status")
    .register();

requestLatency.labelValues("ok").observe(2.7);
```

The example above creates a summary with the 50th percentile (median), the 95th percentile, and the 99th percentile. Quantiles are optional, you can create a summary without quantiles if all you need is the count and the sum.

{{< hint type=note >}}
The terms "percentile" and "quantile" mean the same thing. We use percentile when we express it as a number in [0, 100], and we use quantile when we express it as a number in [0.0, 1.0].
{{< /hint >}}

The second parameter to `quantile()` is the maximum acceptable error. The call `.quantile(0.5, 0.01)` means that the actual quantile is somewhere in [0.49, 0.51]. Higher precision means higher memory usage.

The 0.0 quantile (min value) and the 1.0 quantile (max value) are special cases because you can get the precise values (error 0.0) with almost no memory overhead.

Quantile values are calculated based on a 5 minutes moving time window. The default time window can be changed with `maxAgeSeconds()` and `numberOfAgeBuckets()`.

Some options can be configured at runtime, see [config](../../config/config).

In general you should prefer histograms over summaries. The Prometheus query language has a function [histogram_quantile()](https://prometheus.io/docs/prometheus/latest/querying/functions/#histogram_quantile) for calculating quantiles from histograms. The advantage of query-time quantile calculation is that you can aggregate histograms before calculating the quantile. With summaries you must use the quantile with all its labels as it is.

Info
----

Info metrics are used to expose textual information which should not change during process lifetime. The value of an Info metric is always `1`.

```java
Info info = Info.builder()
    .name("jvm_runtime_info")
    .help("JVM runtime info")
    .labelNames("version", "vendor", "runtime")
    .register();

String version = System.getProperty("java.runtime.version", "unknown");
String vendor = System.getProperty("java.vm.vendor", "unknown");
String runtime = System.getProperty("java.runtime.name", "unknown");

info.setLabelValues(version, vendor, runtime);
```

The info above looks as follows in OpenMetrics text format:

```
# TYPE jvm_runtime info
# HELP jvm_runtime JVM runtime info
jvm_runtime_info{runtime="OpenJDK Runtime Environment",vendor="Oracle Corporation",version="1.8.0_382-b05"} 1
```

The example is taken from the `prometheus-metrics-instrumentation-jvm` module, so if you have `JvmMetrics` registered you should have a `jvm_runtime_info` metric out-of-the-box.

As defined in [OpenMetrics](https://openmetrics.io/), info metric names must have the `_info` suffix. If you create a counter without the `_info` suffix the suffix will be appended automatically.

StateSet
--------

StateSet are a niche metric type in the OpenMetrics standard that is rarely used. The main use case is to signal which feature flags are enabled.

```java
StateSet stateSet = StateSet.builder()
    .name("feature_flags")
    .help("Feature flags")
    .labelNames("env")
    .states("feature1", "feature2")
    .register();

stateSet.labelValues("dev").setFalse("feature1");
stateSet.labelValues("dev").setTrue("feature2");
```

The OpenMetrics text format looks like this:

```
# TYPE feature_flags stateset
# HELP feature_flags Feature flags
feature_flags{env="dev",feature_flags="feature1"} 0
feature_flags{env="dev",feature_flags="feature2"} 1
```

GaugeHistogram and Unknown
--------------------------

These types are defined in the [OpenMetrics](https://openmetrics.io/) standard but not implemented in the `prometheus-metrics-core` API.
However, `prometheus-metrics-model` implements the underlying data model for these types.
To use these types, you need to implement your own `Collector` where the `collect()` method returns an `UnknownSnapshot` or a `HistogramSnapshot` with `.gaugeHistogram(true)`.

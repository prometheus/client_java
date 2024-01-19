---
title: Performance
weight: 6
---

This section has tips on how to use the Prometheus Java client in high performance applications.

Specify Label Values Only Once
------------------------------

For high performance applications, we recommend to specify label values only once, and then use the data point directly.

This applies to all metric types. Let's use a counter as an example here:

```java
Counter requestCount = Counter.builder()
    .name("requests_total")
    .help("total number of requests")
    .labelNames("path", "status")
    .register();
```

You could increment the counter above like this:

```java
requestCount.labelValue("/", "200").inc();
```

However, the line above does not only increment the counter, it also looks up the label values to find the right data point.

In high performance applications you can optimize this by looking up the data point only once:

```java
CounterDataPoint successfulCalls = requestCount.labelValues("/", "200");
```

Now, you can increment the data point directly, which is a highly optimized operation:

```java
successfulCalls.inc();
```

Enable Only One Histogram Representation
----------------------------------------

By default, histograms maintain two representations under the hood: The classic histogram representation with static buckets, and the native histogram representation with dynamic buckets.

While this default provides the flexibility to scrape different representations at runtime, it comes at a cost, because maintaining multiple representations causes performance overhead.

In performance critical applications we recommend to use either the classic representation or the native representation, but not both.

You can either configure this in code for each histogram by calling [classicOnly()](/client_java/api/io/prometheus/metrics/core/metrics/Histogram.Builder.html#classicOnly()) or [nativeOnly()](/client_java/api/io/prometheus/metrics/core/metrics/Histogram.Builder.html#nativeOnly()), or you use the corresponding [config options](../../config/config/).

One way to do this is with system properties in the command line when you start your application

```sh
java -Dio.prometheus.metrics.histogramClassicOnly=true my-app.jar
```

or

```sh
java -Dio.prometheus.metrics.histogramNativeOnly=true my-app.jar
```

If you don't want to add a command line parameter every time you start your application, you can add a `prometheus.properties` file to your classpath (put it in the `src/main/resources/` directory so that it gets packed into your JAR file). The `prometheus.properties` file should have the following line:

```properties
io.prometheus.metrics.histogramClassicOnly=true
```

or

```properties
io.prometheus.metrics.histogramNativeOnly=true
```

Future releases will add more configuration options, like support for configuration via environment variable`IO_PROMETHEUS_METRICS_HISTOGRAM_NATIVE_ONLY=true`.

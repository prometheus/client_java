---
title: Labels
weight: 3
---

The following shows an example of a Prometheus metric in text format:

```
# HELP payments_total total number of payments
# TYPE payments_total counter
payments_total{status="error",type="paypal"} 1.0
payments_total{status="success",type="credit card"} 3.0
payments_total{status="success",type="paypal"} 2.0
```

The example shows a counter metric named `payments_total` with two labels: `status` and `type`. Each individual data point (each line in text format) is identified by the unique combination of its metric name and its label name/value pairs.

Creating a Metric with Labels
-----------------------------

Labels are supported for all metric types. We are using counters in this example, however the `labelNames()` and `labelValues()` methods are the same for other metric types.

The following code creates the counter above.

```java
Counter counter = Counter.builder()
    .name("payments_total")
    .help("total number of payments")
    .labelNames("type", "status")
    .register();

counter.labelValues("credit card", "success").inc(3.0);
counter.labelValues("paypal", "success").inc(2.0);
counter.labelValues("paypal", "error").inc(1.0);
```

The label names have to be specified when the metric is created and cannot change. The label values are created on demand when values are observed.

Creating a Metric without Labels
--------------------------------

Labels are optional. The following example shows a metric without labels:

```java
Counter counter = Counter.builder()
    .name("payments_total")
    .help("total number of payments")
    .register();

counter.inc(3.0);
```

Cardinality Explosion
---------------------

Each combination of label names and values will result in a new data point, i.e. a new line in text format.
Therefore, a good label should have only a small number of possible values.
If you select labels with many possible values, like unique IDs or timestamps, you may end up with an enormous number of data points.
This is called cardinality explosion.

Here's a bad example, don't do this:

```java
Counter loginCount = Counter.builder()
    .name("logins_total")
    .help("total number of logins")
    .labelNames("user_id", "timestamp") // not a good idea, this will result in too many data points
    .register();

String userId = UUID.randomUUID().toString();
String timestamp = Long.toString(System.currentTimeMillis());

loginCount.labelValues(userId, timestamp).inc();
```

Initializing Label Values
-------------------------

If you register a metric without labels, it will show up immediately with initial value of zero.

However, metrics with labels only show up after the label values are first used. In the example above

```java
counter.labelValues("paypal", "error").inc();
```

The data point

```
payments_total{status="error",type="paypal"} 1.0
```

will jump from non-existent to value 1.0. You will never see it with value 0.0.

This is usually not an issue. However, if you find this annoying and want to see all possible label values from the start, you can initialize label values with `initLabelValues()` like this:

```java
Counter counter = Counter.builder()
    .name("payments_total")
    .help("total number of payments")
    .labelNames("type", "status")
    .register();

counter.initLabelValues("credit card", "success");
counter.initLabelValues("credit card", "error");
counter.initLabelValues("paypal", "success");
counter.initLabelValues("paypal", "error");
```

Now the four combinations will be visible from the start with initial value zero.

```
# HELP payments_total total number of payments
# TYPE payments_total counter
payments_total{status="error",type="credit card"} 0.0
payments_total{status="error",type="paypal"} 0.0
payments_total{status="success",type="credit card"} 0.0
payments_total{status="success",type="paypal"} 0.0
```

Expiring Unused Label Values
----------------------------

There is no automatic expiry of unused label values (yet). Once a set of label values is used, it will remain there forever.

However, you can programmatically remove label values like this:

```java
counter.remove("paypal", "error");
counter.remove("paypal", "success");
```

Const Labels
------------

If you have labels values that never change, you can specify them in the builder as `constLabels()`:

```java
Counter counter = Counter.builder()
    .name("payments_total")
    .help("total number of payments")
    .constLabels(Labels.of("env", "dev"))
    .labelNames("type", "status")
    .register();
```

However, most use cases for `constLabels()` are better covered by target labels set by the scraping Prometheus server,
or by one specific metric (e.g. a `build_info` or a `machine_role` metric). See also
[target labels, not static scraped labels](https://prometheus.io/docs/instrumenting/writing_exporters/#target-labels-not-static-scraped-labels).

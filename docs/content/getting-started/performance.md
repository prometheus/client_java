---
title: Performance
weight: 6
---

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

However, the line above does not only increment the counter, it also lookus up the label values to find the right data point.

In high performance applications you can optimize this by looking up the data point only once:

```java
CounterDataPoint successfulCalls = requestCount.labelValues("/", "200");
```

Now, you can increment the data point directly, which is a highly optimized operation:

```java
successfulCalls.inc();
```

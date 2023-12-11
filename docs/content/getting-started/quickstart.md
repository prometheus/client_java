---
title: Quickstart
weight: 1
---

This tutorial shows the quickest way to get started with the Prometheus Java metrics library.

# Dependencies

We use the following dependencies:

* `prometheus-metrics-core` is the actual metrics library.
* `prometheus-metrics-instrumentation-jvm` provides out-of-the-box JVM metrics.
* `prometheus-metrics-exporter-httpserver` is a standalone HTTP server for exposing Prometheus metrics.
{{< tabs "uniqueid" >}}
{{< tab "Gradle" >}}
```
implementation 'io.prometheus:prometheus-metrics-core:1.0.0'
implementation 'io.prometheus:prometheus-metrics-instrumentation-jvm:1.0.0'
implementation 'io.prometheus:prometheus-metrics-exporter-httpserver:1.0.0'
```
{{< /tab >}}
{{< tab "Maven" >}}
```xml
<dependency>
    <groupId>io.prometheus</groupId>
    <artifactId>prometheus-metrics-core</artifactId>
    <version>1.0.0</version>
</dependency>
<dependency>
    <groupId>io.prometheus</groupId>
    <artifactId>prometheus-metrics-instrumentation-jvm</artifactId>
    <version>1.0.0</version>
</dependency>
<dependency>
    <groupId>io.prometheus</groupId>
    <artifactId>prometheus-metrics-exporter-httpserver</artifactId>
    <version>1.0.0</version>
</dependency>
```
{{< /tab >}}
{{< /tabs >}}

There are alternative exporters as well, for example if you are using a Servlet container like Tomcat or Undertow you might want to use `prometheus-exporter-servlet-jakarta` rather than a standalone HTTP server.


# Example Application

```java
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;

import java.io.IOException;

public class App {

    public static void main(String[] args) throws InterruptedException, IOException {

        JvmMetrics.builder().register(); // initialize the out-of-the-box JVM metrics

        Counter counter = Counter.builder()
                .name("my_count_total")
                .help("example counter")
                .labelNames("status")
                .register();

        counter.labelValues("ok").inc();
        counter.labelValues("ok").inc();
        counter.labelValues("error").inc();

        HTTPServer server = HTTPServer.builder()
                .port(9400)
                .buildAndStart();

        System.out.println("HTTPServer listening on port http://localhost:" + server.getPort() + "/metrics");

        Thread.currentThread().join(); // sleep forever
    }
}
```

# Result

Run the application and view [http://localhost:9400/metrics](http://localhost:9400/metrics) with your browser to see the raw metrics. You should see the `my_count_total` metric as shown below plus the `jvm_` and `process_` metrics coming from `JvmMetrics`.

```
# HELP my_count_total example counter
# TYPE my_count_total counter
my_count_total{status="error"} 1.0
my_count_total{status="ok"} 2.0
```

# Prometheus Configuration

To scrape the metrics with a Prometheus server, download the latest Prometheus server [release](https://github.com/prometheus/prometheus/releases), and configure the `prometheus.yml` file as follows:

```yaml
global:
  scrape_interval: 10s # short interval for manual testing

scrape_configs:

  - job_name: "java-example"
    static_configs:
      - targets: ["localhost:9400"]
```

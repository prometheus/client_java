---
title: Quickstart
weight: 0
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
implementation 'io.prometheus:prometheus-metrics-core:$version'
implementation 'io.prometheus:prometheus-metrics-instrumentation-jvm:$version'
implementation 'io.prometheus:prometheus-metrics-exporter-httpserver:$version'
```
{{< /tab >}}
{{< tab "Maven" >}}
```xml
<dependency>
    <groupId>io.prometheus</groupId>
    <artifactId>prometheus-metrics-core</artifactId>
    <version>$version</version>
</dependency>
<dependency>
    <groupId>io.prometheus</groupId>
    <artifactId>prometheus-metrics-instrumentation-jvm</artifactId>
    <version>$version</version>
</dependency>
<dependency>
    <groupId>io.prometheus</groupId>
    <artifactId>prometheus-metrics-exporter-httpserver</artifactId>
    <version>$version</version>
</dependency>
```
{{< /tab >}}
{{< /tabs >}}

There are alternative exporters as well, for example if you are using a Servlet container like Tomcat or Undertow you might want to use `prometheus-exporter-servlet-jakarta` rather than a standalone HTTP server.

# Dependency management

A Bill of Material
([BOM](https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html#bill-of-materials-bom-poms))
ensures that versions of dependencies (including transitive ones) are aligned.
This is especially important when using Spring Boot, which manages some of the dependencies of the project.

You should omit the version number of the dependencies in your build file if you are using a BOM.

{{< tabs "uniqueid" >}}
{{< tab "Gradle" >}}

You have two ways to import a BOM.

First, you can use the Gradle’s native BOM support by adding `dependencies`:

```kotlin
import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
  id("java")
  id("org.springframework.boot") version "3.2.O" // if you are using Spring Boot
}

dependencies {
  implementation(platform(SpringBootPlugin.BOM_COORDINATES)) // if you are using Spring Boot
  implementation(platform("io.prometheus:prometheus-metrics-bom:$version"))
}
```

The other way with Gradle is to use `dependencyManagement`:

```kotlin
plugins {
  id("java")
  id("org.springframework.boot") version "3.2.O" // if you are using Spring Boot
  id("io.spring.dependency-management") version "1.1.0" // if you are using Spring Boot
}

dependencyManagement {
  imports {
    mavenBom("io.prometheus:prometheus-metrics-bom:$version")
  }
}
```

{{% alert title="Note" color="info" %}}

Be careful not to mix up the different ways of configuring things with Gradle.
For example, don't use
`implementation(platform("io.prometheus:prometheus-metrics-bom:$version"))`
with the `io.spring.dependency-management` plugin.

{{% /alert %}}    

{{< /tab >}}
{{< tab "Maven" >}}

{{% alert title="Note" color="info" %}}

Import the Prometheus Java metrics BOMs before any other BOMs in your
project. For example, if you import the `spring-boot-dependencies` BOM, you have
to declare it after the Prometheus Java metrics BOMs.

{{% /alert %}}

The following example shows how to import the Prometheus Java metrics BOMs using Maven:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.prometheus</groupId>
            <artifactId>prometheus-metrics-bom</artifactId>
            <version>$version</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

{{< /tab >}}
{{< /tabs >}}

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

---
title: Pushgateway
weight: 5
---

The [Prometheus Pushgateway](https://github.com/prometheus/pushgateway) exists to allow ephemeral and batch jobs to expose their metrics to Prometheus.
Since these kinds of jobs may not exist long enough to be scraped, they can instead push their metrics to a Pushgateway.
The Pushgateway then exposes these metrics to Prometheus.

The [PushGateway](/client_java/api/io/prometheus/metrics/exporter/pushgateway/PushGateway.html) Java class allows you to push metrics to a Prometheus Pushgateway.

Example
-------

{{< tabs "uniqueid" >}}
{{< tab "Gradle" >}}
```
implementation 'io.prometheus:prometheus-metrics-core:1.3.0'
implementation 'io.prometheus:prometheus-metrics-exporter-pushgateway:1.3.0'
```
{{< /tab >}}
{{< tab "Maven" >}}
```xml
<dependency>
    <groupId>io.prometheus</groupId>
    <artifactId>prometheus-metrics-core</artifactId>
    <version>1.3.0</version>
</dependency>
<dependency>
    <groupId>io.prometheus</groupId>
    <artifactId>prometheus-metrics-exporter-pushgateway</artifactId>
    <version>1.3.0</version>
</dependency>
```
{{< /tab >}}
{{< /tabs >}}

```java
public class ExampleBatchJob {

    private static PushGateway pushGateway = PushGateway.builder()
            .address("localhost:9091") // not needed as localhost:9091 is the default
            .job("example")
            .build();

    private static Gauge dataProcessedInBytes = Gauge.builder()
            .name("data_processed")
            .help("data processed in the last batch job run")
            .unit(Unit.BYTES)
            .register();

    public static void main(String[] args) throws Exception {
        try {
            long bytesProcessed = processData();
            dataProcessedInBytes.set(bytesProcessed);
        } finally {
            pushGateway.push();
        }
    }

    public static long processData() {
        // Imagine a batch job here that processes data
        // and returns the number of Bytes processed.
        return 42;
    }
}
```

Basic Auth
----------

The [PushGateway](/client_java/api/io/prometheus/metrics/exporter/pushgateway/PushGateway.html) supports basic authentication.

```java
PushGateway pushGateway = PushGateway.builder()
    .job("example")
    .basicAuth("my_user", "my_password")
    .build();
```

The `PushGatewayTestApp` in `integration-tests/it-pushgateway` has a complete example of this.

Bearer token
----------

The [PushGateway](/client_java/api/io/prometheus/metrics/exporter/pushgateway/PushGateway.html) supports Bearer token authentication.

```java
PushGateway pushGateway = PushGateway.builder()
    .job("example")
    .bearerToken("my_token")
    .build();
```

The `PushGatewayTestApp` in `integration-tests/it-pushgateway` has a complete example of this.


SSL
---

The [PushGateway](/client_java/api/io/prometheus/metrics/exporter/pushgateway/PushGateway.html) supports SSL.

```java
PushGateway pushGateway = PushGateway.builder()
    .job("example")
    .scheme(Scheme.HTTPS)
    .build();
```

However, this requires that the JVM can validate the server certificate.

If you want to skip certificate verification, you need to provide your own  [HttpConnectionFactory](/client_java/api/io/prometheus/metrics/exporter/pushgateway/HttpConnectionFactory.html).
The `PushGatewayTestApp` in `integration-tests/it-pushgateway` has a complete example of this.

Configuration Properties
------------------------

The [PushGateway](/client_java/api/io/prometheus/metrics/exporter/pushgateway/PushGateway.html) supports a couple of properties that can be configured at runtime. See [config](../../config/config).

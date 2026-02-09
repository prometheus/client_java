# OTel JVM Runtime Metrics with Prometheus HTTPServer

## Build

This example is built as part of the `client_java` project.

```shell
./../../mvnw package
```

## Run

The build creates a JAR file with the example application in
`./examples/example-otel-jvm-runtime-metrics/target/`.

```shell
java -jar ./examples/example-otel-jvm-runtime-metrics/target/example-otel-jvm-runtime-metrics.jar
```

## Manually Testing the Metrics Endpoint

Accessing
[http://localhost:9400/metrics](http://localhost:9400/metrics)
with a Web browser should yield both a Prometheus counter metric
and OTel JVM runtime metrics on the same endpoint.

Prometheus counter:

```text
# HELP uptime_seconds_total total number of seconds since this application was started
# TYPE uptime_seconds_total counter
uptime_seconds_total 42.0
```

OTel JVM runtime metrics (excerpt):

```text
# HELP jvm_memory_used_bytes Measure of memory used.
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{jvm_memory_pool_name="G1 Eden Space",jvm_memory_type="heap"} 4194304.0
```

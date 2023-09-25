# Built-in HTTPServer for Exposing Metrics

## Build

This example is built as part of the `client_java` project.

```
./mvnw package
```

## Run

The build creates a JAR file with the example application in `./examples/example-exporter-httpserver/target/`.

```
java -jar ./examples/example-exporter-httpserver/target/example-exporter-httpserver.jar
```

## Manually testing the Metrics Endpoint

Accessing [http://localhost:9400/metrics](http://localhost:9400/metrics) with a Web browser should yield an example of a counter metric.

```
# HELP uptime_seconds_total total number of seconds since this application was started
# TYPE uptime_seconds_total counter
uptime_seconds_total 301.0
```

The exporter supports a `debug` URL parameter to quickly view other formats in your Web browser:

* [http://localhost:9400/metrics?debug=text](http://localhost:9400/metrics?debug=text): Prometheus text format, same as without the `debug` option.
* [http://localhost:9400/metrics?debug=openmetrics](http://localhost:9400/metrics?debug=openmetrics): OpenMetrics text format.
* [http://localhost:9400/metrics?debug=prometheus-protobuf](http://localhost:9400/metrics?debug=prometheus-protobuf): Text representation of the Prometheus protobuf format.

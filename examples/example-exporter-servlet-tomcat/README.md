# Tomcat Servlet Example

## Build

This example is built as part of the `client_java` project.

```
./mvnw package
```

## Run

The build creates a JAR file with the example application in `./examples/example-exporter-servlet-tomcat/target/`.

```
java -jar ./examples/example-exporter-servlet-tomcat/target/example-exporter-servlet-tomcat.jar
```

Accessing [http://localhost:8080/](http://localhost:8080/) with a Web browser should yield `Hello, World!`.

## Manually testing the Metrics Endpoint

Metrics are available on [http://localhost:8080/metrics](http://localhost:8080/metrics). The default Prometheus text format looks like this:

```
# HELP request_duration_seconds request duration in seconds
# TYPE request_duration_seconds histogram
request_duration_seconds_bucket{http_status="200",le="0.005"} 0
request_duration_seconds_bucket{http_status="200",le="0.01"} 1
request_duration_seconds_bucket{http_status="200",le="0.025"} 1
request_duration_seconds_bucket{http_status="200",le="0.05"} 2
request_duration_seconds_bucket{http_status="200",le="0.1"} 5
request_duration_seconds_bucket{http_status="200",le="0.25"} 11
request_duration_seconds_bucket{http_status="200",le="0.5"} 12
request_duration_seconds_bucket{http_status="200",le="1.0"} 12
request_duration_seconds_bucket{http_status="200",le="2.5"} 12
request_duration_seconds_bucket{http_status="200",le="5.0"} 12
request_duration_seconds_bucket{http_status="200",le="10.0"} 12
request_duration_seconds_bucket{http_status="200",le="+Inf"} 12
request_duration_seconds_count{http_status="200"} 12
request_duration_seconds_sum{http_status="200"} 1.513772412
# HELP requests_total total number of requests
# TYPE requests_total counter
requests_total{http_status="200"} 12.0
```

The exporter servlet supports a `debug` URL parameter to quickly view other formats in your Web browser:

* [http://localhost:8080/metrics?debug=text](http://localhost:8080/metrics?debug=text): Prometheus text format, same as without the `debug` option.
* [http://localhost:8080/metrics?debug=openmetrics](http://localhost:8080/metrics?debug=openmetrics): OpenMetrics text format.
* [http://localhost:8080/metrics?debug=prometheus-protobuf](http://localhost:8080/metrics?debug=prometheus-protobuf): Text representation of the Prometheus protobuf format.

## Testing with the Prometheus Server

1. Download the latest Prometheus server release from [https://github.com/prometheus/prometheus/releases](https://github.com/prometheus/prometheus/releases).
2. Extract the archive
3. Edit `prometheus.yml` and append the following snippet at the end:
   ```yaml
    job_name: "tomcat-servlet-example"
    static_configs:
      - targets: ["localhost:8080"]
   ```
4. Run with native histograms and examplars enabled:
   ```shell
   ./prometheus --enable-feature=native-histograms --enable-feature=exemplar-storage
   ```
   
Verify that the `tomcat-servlet-example` target is up on [http://localhost:9090/targets](http://localhost:9090/targets).

Prometheus is now scraping metrics in Protobuf format. If you type the name `request_duration_seconds` you will see a non-human-readable representation of the histogram including the native buckets:

![Screenshot showing a Prometheus Native Histogram in Text Format](https://github.com/prometheus/client_java/assets/330535/863efe0b-a9eb-40ae-a078-72497abc6f04)

Note: You have to run at least one GET request on the Hello World endpoint [http://localhost:8080](http://localhost:8080) before you see the metric.

Use the `histogram_quantile()` function to calculate quantiles from the native histogram:

```
histogram_quantile(0.95, rate(request_duration_seconds[10m]))
```

![Screenshot showing the 95th Percentile Calculated from a Prometheus Native Histogram](https://github.com/prometheus/client_java/assets/330535/889fb769-9445-4f6f-8540-2b1ddffca55e)

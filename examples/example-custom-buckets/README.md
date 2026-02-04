# Native Histograms with Custom Buckets (NHCB) Example

This example demonstrates how to use native histograms with custom bucket boundaries (NHCB) in
Prometheus Java client. It shows three different types of custom bucket configurations and how
Prometheus converts them to native histograms with schema -53.

## What are Native Histograms with Custom Buckets?

Native Histograms with Custom Buckets (NHCB) is a Prometheus feature that combines the benefits of:

- **Custom bucket boundaries**: Precisely defined buckets optimized for your specific use case
- **Native histograms**: Efficient storage and querying capabilities of native histograms

When you configure Prometheus with `convert_classic_histograms_to_nhcb: true`, it converts classic
histograms with custom buckets into native histograms using schema -53, preserving the custom
bucket boundaries.

## Example Metrics

This example application generates three different histogram metrics demonstrating different
bucket configuration strategies:

### 1. API Latency - Arbitrary Custom Boundaries

```java
Histogram apiLatency = Histogram.builder()
    .name("api_request_duration_seconds")
    .classicUpperBounds(0.01, 0.05, 0.1, 0.5, 1.0, 5.0, 10.0)
    .register();
```

**Use case**: Optimized for typical API response times in seconds.

### 2. Queue Size - Linear Boundaries

```java
Histogram queueSize = Histogram.builder()
    .name("message_queue_size")
    .classicLinearUpperBounds(10, 10, 10) // 10, 20, 30, ..., 100
    .register();
```

**Use case**: Equal-width buckets for monitoring queue depth or other discrete values.

### 3. Response Size - Exponential Boundaries

```java
Histogram responseSize = Histogram.builder()
    .name("http_response_size_bytes")
    .classicExponentialUpperBounds(100, 10, 6) // 100, 1k, 10k, 100k, 1M, 10M
    .register();
```

**Use case**: Data spanning multiple orders of magnitude (bytes, milliseconds, etc).

## Build

This example is built as part of the `client_java` project:

```shell
./mvnw package
```

This creates `./examples/example-custom-buckets/target/example-custom-buckets.jar`.

## Run

With the JAR file present, run:

```shell
cd ./examples/example-custom-buckets/
docker-compose up
```

This starts three Docker containers:

- **[http://localhost:9400/metrics](http://localhost:9400/metrics)** - Example application
- **[http://localhost:9090](http://localhost:9090)** - Prometheus server (with NHCB enabled)
- **[http://localhost:3000](http://localhost:3000)** - Grafana (user: _admin_, password: _admin_)

You might need to replace `localhost` with `host.docker.internal` on macOS or Windows.

## Verify NHCB Conversion

### 1. Check Prometheus Configuration

The Prometheus configuration enables NHCB conversion:

```yaml
scrape_configs:
  - job_name: "custom-buckets-demo"
    scrape_protocols: ["PrometheusProto"]
    convert_classic_histograms_to_nhcb: true
    scrape_classic_histograms: true
```

### 2. Verify in Prometheus

Visit [http://localhost:9090](http://localhost:9090) and run queries:

```promql
# View histogram metadata (should show schema -53 for NHCB)
prometheus_tsdb_head_series

# Calculate quantiles from custom buckets
histogram_quantile(0.95, rate(api_request_duration_seconds[1m]))

# View raw histogram structure
api_request_duration_seconds
```

### 3. View in Grafana

The Grafana dashboard at [http://localhost:3000](http://localhost:3000) shows:

- p95 and p50 latencies for API endpoints (arbitrary custom buckets)
- Queue size distribution (linear buckets)
- Response size distribution (exponential buckets)

## Key Observations

1. **Custom Buckets Preserved**: The custom bucket boundaries you define are preserved when
   converted to NHCB (schema -53).

2. **Dual Representation**: By default, histograms maintain both classic and native
   representations, allowing gradual migration.

3. **Efficient Storage**: Native histograms provide more efficient storage than classic histograms
   while preserving your custom bucket boundaries.

4. **Flexible Bucket Strategies**: You can choose arbitrary, linear, or exponential buckets based
   on your specific monitoring needs.

## When to Use Custom Buckets

Consider using custom buckets (and NHCB) when:

- **Precise boundaries needed**: You know the expected distribution and want specific bucket edges
- **Migrating from classic histograms**: You want to preserve existing bucket boundaries
- **Specific use cases**: Default exponential bucketing doesn't fit your distribution well
  - Temperature ranges (might include negative values)
  - Queue depths (discrete values with linear growth)
  - File sizes (exponential growth but with specific thresholds)
  - API latencies (specific SLA boundaries)

## Differences from Standard Native Histograms

| Feature           | Standard Native Histograms            | NHCB (Schema -53)                      |
| ----------------- | ------------------------------------- | -------------------------------------- |
| Bucket boundaries | Exponential (base 2^(2^-scale))       | Custom boundaries                      |
| Use case          | General-purpose                       | Specific distributions                 |
| Mergeability      | Can merge histograms with same schema | Cannot merge with different boundaries |
| Configuration     | Schema level (0-8)                    | Explicit boundary list                 |

## Cleanup

Stop the containers:

```shell
docker-compose down
```

## Further Reading

- [Prometheus Native Histograms Specification](https://prometheus.io/docs/specs/native_histograms/)
- [Prometheus Java Client Documentation](https://prometheus.github.io/client_java/)
- [OpenTelemetry Exponential Histograms](https://opentelemetry.io/docs/specs/otel/metrics/data-model/#exponentialhistogram)

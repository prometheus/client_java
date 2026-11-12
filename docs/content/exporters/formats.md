---
title: Formats
weight: 1
---

All exporters the following exposition formats:

- OpenMetrics text format
- Prometheus text format
- Prometheus protobuf format

Moreover, gzip encoding is supported for each of these formats.

## OpenMetrics 2.0 Preview

The library also includes an experimental OpenMetrics 2.0 writer. It is disabled by default and
must be enabled explicitly. See [OpenMetrics 2.0 Preview]({{< relref "./openmetrics2.md" >}}).

## Scraping with a Prometheus server

The Prometheus server sends an `Accept` header to specify which format is requested. By default, the
Prometheus server will scrape OpenMetrics text format with gzip encoding. If the Prometheus server
is started with `--enable-feature=native-histograms`, it will scrape Prometheus protobuf format
instead.

## Writing a negotiated response

Applications that write exposition responses themselves can use `findWriterWithEscaping` to select
the format and escaping scheme together from one `Accept` header. Create and reuse an
`ExpositionFormats` instance instead of initializing it for each response:

```java
import io.prometheus.metrics.expositionformats.ExpositionFormatWriterWithEscaping;
import io.prometheus.metrics.expositionformats.ExpositionFormats;

ExpositionFormats formats = ExpositionFormats.init(); // Create once and reuse.

// In response-writing code, acceptHeader, registry, and outputStream are supplied by the app.
ExpositionFormatWriterWithEscaping writer = formats.findWriterWithEscaping(acceptHeader);
String contentType = writer.getContentType(); // Set the response header before writing the body.
writer.write(outputStream, registry.scrape());
```

The returned writer binds the negotiated format and escaping scheme, so there is no need to parse
the `Accept` header again when writing. `getContentType()` returns the selected format's standard
content type; it does not append an escaping parameter. The existing `findWriter` and writer methods
remain available when you need to choose the escaping scheme explicitly. For details, see the
[Unicode escaping documentation]({{< relref "./unicode.md" >}}) and
[OpenMetrics 2.0 preview]({{< relref "./openmetrics2.md" >}}).

## Viewing with a Web Browser

If you view the `/metrics` endpoint with your Web browser you will see Prometheus text format. For
quick debugging of the other formats, exporters provide a `debug` URL parameter:

- `/metrics?debug=openmetrics`: View OpenMetrics text format.
- `/metrics?debug=text`: View Prometheus text format.
- `/metrics?debug=prometheus-protobuf`: View a text representation of the Prometheus protobuf
  format.

## Exclude protobuf exposition format

You can exclude the protobuf exposition format by including the
`prometheus-metrics-exposition-textformats` module and excluding the
`prometheus-metrics-exposition-formats` module in your build file.

For example, in Maven:

```xml
<dependencies>
  <dependency>
      <groupId>io.prometheus</groupId>
      <artifactId>prometheus-metrics-exporter-httpserver</artifactId>
      <exclusions>
          <exclusion>
              <groupId>io.prometheus</groupId>
              <artifactId>prometheus-metrics-exposition-formats</artifactId>
          </exclusion>
      </exclusions>
  </dependency>
  <dependency>
      <groupId>io.prometheus</groupId>
      <artifactId>prometheus-metrics-exposition-textformats</artifactId>
  </dependency>
</dependencies>
```

## Exclude the shaded protobuf classes

You can exclude the shaded protobuf classes including the
`prometheus-metrics-exposition-formats-no-protobuf` module and excluding the
`prometheus-metrics-exposition-formats` module in your build file.

If you are using the PushGateway in a shaded jar with `minimizeJar=true`, do not exclude the protobuf classes.
The PushGateway loads the protobuf writer implementation via reflection, so the full
`prometheus-metrics-exposition-formats` artifact must stay on the classpath and the relevant
packages must be preserved during shading. See the [PushGateway docs]({{< relref
"./pushgateway.md" >}}) for the recommended Maven Shade configuration.

For example, in Maven:

```xml
<dependencies>
  <dependency>
      <groupId>io.prometheus</groupId>
      <artifactId>prometheus-metrics-exporter-httpserver</artifactId>
      <exclusions>
          <exclusion>
              <groupId>io.prometheus</groupId>
              <artifactId>prometheus-metrics-exposition-formats</artifactId>
          </exclusion>
      </exclusions>
  </dependency>
  <dependency>
      <groupId>io.prometheus</groupId>
      <artifactId>prometheus-metrics-exposition-formats-no-protobuf</artifactId>
  </dependency>
</dependencies>
```

## Exclude the shaded otel classes

You can exclude the shaded otel classes including the
`prometheus-metrics-exporter-opentelemetry-no-otel` module and excluding the
`prometheus-metrics-exporter-opentelemetry` module in your build file.

For example, in Maven:

```xml
<dependencies>
  <dependency>
      <groupId>io.prometheus</groupId>
      <artifactId>prometheus-metrics-exporter-opentelemetry</artifactId>
      <exclusions>
          <exclusion>
              <groupId>io.prometheus</groupId>
              <artifactId>prometheus-metrics-exporter-opentelemetry</artifactId>
          </exclusion>
      </exclusions>
  </dependency>
  <dependency>
      <groupId>io.prometheus</groupId>
      <artifactId>prometheus-metrics-exporter-opentelemetry-no-otel</artifactId>
  </dependency>
</dependencies>
```

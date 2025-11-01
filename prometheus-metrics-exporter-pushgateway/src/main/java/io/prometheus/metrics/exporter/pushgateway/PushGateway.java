package io.prometheus.metrics.exporter.pushgateway;

import static io.prometheus.metrics.exporter.pushgateway.Scheme.HTTP;
import static io.prometheus.metrics.model.snapshots.PrometheusNaming.escapeName;
import static java.util.Objects.requireNonNull;

import io.prometheus.metrics.config.EscapingScheme;
import io.prometheus.metrics.config.ExporterPushgatewayProperties;
import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.config.PrometheusPropertiesException;
import io.prometheus.metrics.expositionformats.ExpositionFormatWriter;
import io.prometheus.metrics.expositionformats.PrometheusProtobufWriter;
import io.prometheus.metrics.expositionformats.PrometheusTextFormatWriter;
import io.prometheus.metrics.model.registry.Collector;
import io.prometheus.metrics.model.registry.MultiCollector;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Optional;
import javax.annotation.Nullable;

/**
 * Export metrics via the <a href="https://github.com/prometheus/pushgateway">Prometheus
 * Pushgateway</a>
 *
 * <p>The Prometheus Pushgateway exists to allow ephemeral and batch jobs to expose their metrics to
 * Prometheus. Since these kinds of jobs may not exist long enough to be scraped, they can instead
 * push their metrics to a Pushgateway. This Java class allows pushing the contents of a {@link
 * PrometheusRegistry} to a Pushgateway.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * void executeBatchJob() throws Exception {
 *     PrometheusRegistry registry = new PrometheusRegistry();
 *     Gauge duration = Gauge.builder()
 *             .name("my_batch_job_duration_seconds")
 *             .help("Duration of my batch job in seconds.")
 *             .register(registry);
 *     Timer durationTimer = duration.startTimer();
 *     try {
 *         // Your code here.
 *
 *         // This is only added to the registry after success,
 *         // so that a previous success in the Pushgateway isn't overwritten on failure.
 *         Gauge lastSuccess = Gauge.builder()
 *                 .name("my_batch_job_last_success")
 *                 .help("Last time my batch job succeeded, in unixtime.")
 *                 .register(registry);
 *         lastSuccess.set(System.currentTimeMillis());
 *     } finally {
 *         durationTimer.observeDuration();
 *         PushGateway pg = PushGateway.builder()
 *                 .address("127.0.0.1:9091")
 *                 .job("my_batch_job")
 *                 .registry(registry)
 *                 .build();
 *         pg.pushAdd();
 *     }
 * }
 * }</pre>
 *
 * <p>See <a
 * href="https://github.com/prometheus/pushgateway">https://github.com/prometheus/pushgateway</a>.
 */
public class PushGateway {

  private static final int MILLISECONDS_PER_SECOND = 1000;

  private final URL url;
  private final ExpositionFormatWriter writer;
  private final boolean prometheusTimestampsInMs;
  private final Map<String, String> requestHeaders;
  private final PrometheusRegistry registry;
  private final HttpConnectionFactory connectionFactory;
  private final EscapingScheme escapingScheme;
  private final Integer connectionTimeout;
  private final Integer readTimeout;

  private PushGateway(
      PrometheusRegistry registry,
      Format format,
      URL url,
      HttpConnectionFactory connectionFactory,
      Map<String, String> requestHeaders,
      boolean prometheusTimestampsInMs,
      EscapingScheme escapingScheme,
      @Nullable Integer connectionTimeout,
      @Nullable Integer readTimeout) {
    this.registry = registry;
    this.url = url;
    this.requestHeaders = Collections.unmodifiableMap(new HashMap<>(requestHeaders));
    this.connectionFactory = connectionFactory;
    this.prometheusTimestampsInMs = prometheusTimestampsInMs;
    this.escapingScheme = escapingScheme;
    this.connectionTimeout = Optional.ofNullable(connectionTimeout).orElse(10 * MILLISECONDS_PER_SECOND);
    this.readTimeout = Optional.ofNullable(readTimeout).orElse(10 * MILLISECONDS_PER_SECOND);
    writer = getWriter(format);
    if (!writer.isAvailable()) {
      throw new RuntimeException(writer.getClass() + " is not available");
    }
  }

  @SuppressWarnings("deprecation")
  private ExpositionFormatWriter getWriter(Format format) {
    if (format == Format.PROMETHEUS_TEXT) {
      return PrometheusTextFormatWriter.builder()
          .setTimestampsInMs(this.prometheusTimestampsInMs)
          .build();
    } else {
      // use reflection to avoid a compile-time dependency on the expositionformats module
      return new PrometheusProtobufWriter();
    }
  }

  /**
   * Push all metrics. All metrics with the same job and grouping key are replaced.
   *
   * <p>This uses the PUT HTTP method.
   */
  public void push() throws IOException {
    doRequest(registry, "PUT");
  }

  /**
   * Push a single metric. All metrics with the same job and grouping key are replaced.
   *
   * <p>This is useful for pushing a single Gauge.
   *
   * <p>This uses the PUT HTTP method.
   */
  public void push(Collector collector) throws IOException {
    PrometheusRegistry registry = new PrometheusRegistry();
    registry.register(collector);
    doRequest(registry, "PUT");
  }

  /**
   * Push a single collector. All metrics with the same job and grouping key are replaced.
   *
   * <p>This uses the PUT HTTP method.
   */
  public void push(MultiCollector collector) throws IOException {
    PrometheusRegistry registry = new PrometheusRegistry();
    registry.register(collector);
    doRequest(registry, "PUT");
  }

  /**
   * Like {@link #push()}, but only metrics with the same name as the newly pushed metrics are
   * replaced.
   *
   * <p>This uses the POST HTTP method.
   */
  public void pushAdd() throws IOException {
    doRequest(registry, "POST");
  }

  /**
   * Like {@link #push(Collector)}, but only the specified metric will be replaced.
   *
   * <p>This uses the POST HTTP method.
   */
  public void pushAdd(Collector collector) throws IOException {
    PrometheusRegistry registry = new PrometheusRegistry();
    registry.register(collector);
    doRequest(registry, "POST");
  }

  /**
   * Like {@link #push(MultiCollector)}, but only the metrics from the collector will be replaced.
   *
   * <p>This uses the POST HTTP method.
   */
  public void pushAdd(MultiCollector collector) throws IOException {
    PrometheusRegistry registry = new PrometheusRegistry();
    registry.register(collector);
    doRequest(registry, "POST");
  }

  /**
   * Deletes metrics from the Pushgateway.
   *
   * <p>This uses the DELETE HTTP method.
   */
  public void delete() throws IOException {
    doRequest(null, "DELETE");
  }

  private void doRequest(@Nullable PrometheusRegistry registry, String method) throws IOException {
    try {
      HttpURLConnection connection = connectionFactory.create(url);
      requestHeaders.forEach(connection::setRequestProperty);
      connection.setRequestProperty("Content-Type", writer.getContentType());
      if (!method.equals("DELETE")) {
        connection.setDoOutput(true);
      }
      connection.setRequestMethod(method);

      connection.setConnectTimeout(this.connectionTimeout);
      connection.setReadTimeout(this.readTimeout);
      connection.connect();

      try {
        if (!method.equals("DELETE")) {
          OutputStream outputStream = connection.getOutputStream();
          writer.write(outputStream, requireNonNull(registry).scrape(), this.escapingScheme);
          outputStream.flush();
          outputStream.close();
        }

        int response = connection.getResponseCode();
        if (response / 100 != 2) {
          String errorMessage;
          InputStream errorStream = connection.getErrorStream();
          if (errorStream != null) {
            String errBody = readFromStream(errorStream);
            errorMessage =
                "Response code from " + url + " was " + response + ", response body: " + errBody;
          } else {
            errorMessage = "Response code from " + url + " was " + response;
          }
          throw new IOException(errorMessage);
        }

      } finally {
        connection.disconnect();
      }
    } catch (IOException e) {
      String baseUrl = url.getProtocol() + "://" + url.getHost();
      if (url.getPort() != -1) {
        baseUrl += ":" + url.getPort();
      }
      throw new IOException(
          "Failed to push metrics to the Prometheus Pushgateway on "
              + baseUrl
              + ": "
              + e.getMessage(),
          e);
    }
  }

  private static String readFromStream(InputStream is) throws IOException {
    ByteArrayOutputStream result = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int length;
    while ((length = is.read(buffer)) != -1) {
      result.write(buffer, 0, length);
    }
    return result.toString("UTF-8");
  }

  public static Builder builder() {
    return builder(PrometheusProperties.get());
  }

  /**
   * The {@link PrometheusProperties} will be used to override what is set in the {@link Builder}.
   */
  public static Builder builder(PrometheusProperties config) {
    return new Builder(config);
  }

  public static class Builder {

    private final PrometheusProperties config;
    @Nullable private Format format;
    @Nullable private String address;
    @Nullable private Scheme scheme;
    @Nullable private String job;
    @Nullable private Integer connectionTimeout;
    @Nullable private Integer readTimeout;
    private boolean prometheusTimestampsInMs;
    private final Map<String, String> requestHeaders = new HashMap<>();
    private PrometheusRegistry registry = PrometheusRegistry.defaultRegistry;
    private HttpConnectionFactory connectionFactory = new DefaultHttpConnectionFactory();
    private final Map<String, String> groupingKey = new TreeMap<>();
    @Nullable private EscapingScheme escapingScheme;

    private Builder(PrometheusProperties config) {
      this.config = config;
    }

    /** Default is {@link Format#PROMETHEUS_PROTOBUF}. */
    public Builder format(Format format) {
      this.format = requireNonNull(format, "format must not be null");
      return this;
    }

    /**
     * Address of the Pushgateway in format {@code host:port}. Default is {@code localhost:9091}.
     * Can be overwritten at runtime with the {@code io.prometheus.exporter.pushgateway.address}
     * property.
     */
    public Builder address(String address) {
      this.address = requireNonNull(address, "address must not be null");
      return this;
    }

    /** Username and password for HTTP basic auth when pushing to the Pushgateway. */
    public Builder basicAuth(String user, String password) {
      byte[] credentialsBytes =
          (requireNonNull(user, "user must not be null")
                  + ":"
                  + requireNonNull(password, "password must not be null"))
              .getBytes(StandardCharsets.UTF_8);
      String encoded = Base64.getEncoder().encodeToString(credentialsBytes);
      requestHeaders.put("Authorization", String.format("Basic %s", encoded));
      return this;
    }

    /** Bearer token authorization when pushing to the Pushgateway. */
    public Builder bearerToken(String token) {
      requestHeaders.put(
          "Authorization",
          String.format("Bearer %s", requireNonNull(token, "token must not be null")));
      return this;
    }

    /**
     * Specify if metrics should be pushed using HTTP or HTTPS. Default is HTTP. Can be overwritten
     * at runtime with the {@code io.prometheus.exporter.pushgateway.scheme} property.
     */
    public Builder scheme(Scheme scheme) {
      this.scheme = requireNonNull(scheme, "scheme must not be null");
      return this;
    }

    /**
     * Custom connection factory. Default is {@link DefaultHttpConnectionFactory}.
     *
     * <p>The {@code PushGatewayTestApp} in {@code integration-tests/it-pushgateway/} has an example
     * of a custom connection factory that skips SSL certificate validation for HTTPS connections.
     */
    public Builder connectionFactory(HttpConnectionFactory connectionFactory) {
      this.connectionFactory =
          requireNonNull(connectionFactory, "connectionFactory must not be null");
      return this;
    }

    /**
     * The {@code job} label to be used when pushing metrics. If not provided, the name of the JAR
     * file will be used by default. Can be overwritten at runtime with the {@code
     * io.prometheus.exporter.pushgateway.job} property.
     */
    public Builder job(String job) {
      this.job = requireNonNull(job, "job must not be null");
      return this;
    }

    /**
     * Grouping keys to be used when pushing/deleting metrics. Call this method multiple times for
     * adding multiple grouping keys.
     */
    public Builder groupingKey(String name, String value) {
      groupingKey.put(
          requireNonNull(name, "name must not be null"),
          requireNonNull(value, "value must not be null"));
      return this;
    }

    /** Convenience method for adding the current IP address as an "instance" label. */
    public Builder instanceIpGroupingKey() throws UnknownHostException {
      return groupingKey("instance", InetAddress.getLocalHost().getHostAddress());
    }

    /** Push metrics from this registry instead of {@link PrometheusRegistry#defaultRegistry}. */
    public Builder registry(PrometheusRegistry registry) {
      this.registry = requireNonNull(registry, "registry must not be null");
      return this;
    }

    /**
     * Specify the escaping scheme to be used when pushing metrics. Default is {@link
     * EscapingScheme#UNDERSCORE_ESCAPING}.
     */
    public Builder escapingScheme(EscapingScheme escapingScheme) {
      this.escapingScheme = requireNonNull(escapingScheme, "escapingScheme must not be null");
      return this;
    }

    /**
     * Use milliseconds for timestamps in text format? Default is {@code false}. Can be overwritten
     * at runtime with the {@code io.prometheus.exporter.timestampsInMs} property.
     */
    public Builder prometheusTimestampsInMs(boolean prometheusTimestampsInMs) {
      this.prometheusTimestampsInMs = prometheusTimestampsInMs;
      return this;
    }

    /**
     * Specify the connection timeout (in milliseconds) for HTTP connections to the PushGateway.
     * Default is {@code 10000} (10 seconds).
     *
     * @param connectionTimeout timeout value in milliseconds
     * @return this {@link Builder} instance
     */
    public Builder connectionTimeout(Integer connectionTimeout) {
      this.connectionTimeout = connectionTimeout;
      return this;
    }

    /**
     * Specify the read timeout (in milliseconds) for reading the response from the PushGateway.
     * Default is {@code 10000} (10 seconds).
     *
     * @param readTimeout timeout value in milliseconds
     * @return this {@link Builder} instance
     */
    public Builder readTimeout(Integer readTimeout) {
      this.readTimeout = readTimeout;
      return this;
    }

    private boolean getPrometheusTimestampsInMs() {
      // accept either to opt in to timestamps in milliseconds
      return config.getExporterProperties().getPrometheusTimestampsInMs()
          || this.prometheusTimestampsInMs;
    }

    private Scheme getScheme(@Nullable ExporterPushgatewayProperties properties) {
      if (properties != null && properties.getScheme() != null) {
        return Scheme.valueOf(properties.getScheme());
      } else if (this.scheme != null) {
        return this.scheme;
      } else {
        return HTTP;
      }
    }

    private String getAddress(@Nullable ExporterPushgatewayProperties properties) {
      if (properties != null && properties.getAddress() != null) {
        return properties.getAddress();
      } else if (this.address != null) {
        return this.address;
      } else {
        return "localhost:9091";
      }
    }

    private String getJob(@Nullable ExporterPushgatewayProperties properties) {
      if (properties != null && properties.getJob() != null) {
        return properties.getJob();
      } else if (this.job != null) {
        return this.job;
      } else {
        return DefaultJobLabelDetector.getDefaultJobLabel();
      }
    }

    private EscapingScheme getEscapingScheme(@Nullable ExporterPushgatewayProperties properties) {
      if (properties != null && properties.getEscapingScheme() != null) {
        return properties.getEscapingScheme();
      } else if (this.escapingScheme != null) {
        return this.escapingScheme;
      }
      return EscapingScheme.UNDERSCORE_ESCAPING;
    }

    private Format getFormat() {
      // currently not configurable via properties
      if (this.format != null) {
        return this.format;
      }
      return Format.PROMETHEUS_PROTOBUF;
    }

    private URL makeUrl(@Nullable ExporterPushgatewayProperties properties)
        throws UnsupportedEncodingException, MalformedURLException {
      StringBuilder url =
          new StringBuilder(getScheme(properties) + "://" + getAddress(properties) + "/metrics/");
      String job = getJob(properties);
      if (job.contains("/")) {
        url.append("job@base64/").append(base64url(job));
      } else {
        url.append("job/").append(URLEncoder.encode(job, "UTF-8"));
      }
      for (Map.Entry<String, String> entry : groupingKey.entrySet()) {
        if (entry.getValue().isEmpty()) {
          url.append("/")
              .append(escapeName(entry.getKey(), EscapingScheme.VALUE_ENCODING_ESCAPING))
              .append("@base64/=");
        } else if (entry.getValue().contains("/")) {
          url.append("/")
              .append(escapeName(entry.getKey(), EscapingScheme.VALUE_ENCODING_ESCAPING))
              .append("@base64/")
              .append(base64url(entry.getValue()));
        } else {
          url.append("/")
              .append(escapeName(entry.getKey(), EscapingScheme.VALUE_ENCODING_ESCAPING))
              .append("/")
              .append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
      }
      return URI.create(url.toString()).normalize().toURL();
    }

    private String base64url(String v) {
      return Base64.getEncoder()
          .encodeToString(v.getBytes(StandardCharsets.UTF_8))
          .replace("+", "-")
          .replace("/", "_");
    }

    public PushGateway build() {
      ExporterPushgatewayProperties properties =
          config == null ? null : config.getExporterPushgatewayProperties();
      try {
        return new PushGateway(
            registry,
            getFormat(),
            makeUrl(properties),
            connectionFactory,
            requestHeaders,
            getPrometheusTimestampsInMs(),
            getEscapingScheme(properties),
            connectionTimeout,
            readTimeout);
      } catch (MalformedURLException e) {
        throw new PrometheusPropertiesException(
            address + ": Invalid address. Expecting <host>:<port>");
      } catch (UnsupportedEncodingException e) {
        throw new RuntimeException(e); // cannot happen, UTF-8 is always supported
      }
    }
  }
}

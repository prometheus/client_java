package io.prometheus.metrics.config;

import java.time.Duration;
import java.util.Map;
import javax.annotation.Nullable;

public class ExporterPushgatewayProperties {

  private static final String ADDRESS = "address";
  private static final String JOB = "job";
  private static final String SCHEME = "scheme";
  private static final String ESCAPING_SCHEME = "escaping_scheme";
  private static final String READ_TIMEOUT = "read_timeout_seconds";
  private static final String CONNECT_TIMEOUT = "connect_timeout_seconds";
  private static final String PREFIX = "io.prometheus.exporter.pushgateway";
  @Nullable private final String scheme;
  @Nullable private final String address;
  @Nullable private final String job;
  @Nullable private final EscapingScheme escapingScheme;
  @Nullable private final Duration connectTimeout;
  @Nullable private final Duration readTimeout;

  private ExporterPushgatewayProperties(
      @Nullable String address,
      @Nullable String job,
      @Nullable String scheme,
      @Nullable EscapingScheme escapingScheme,
      @Nullable Duration connectTimeout,
      @Nullable Duration readTimeout) {
    this.address = address;
    this.job = job;
    this.scheme = scheme;
    this.escapingScheme = escapingScheme;
    this.connectTimeout = connectTimeout;
    this.readTimeout = readTimeout;
  }

  /** Address of the Pushgateway in the form {@code host:port}. Default is {@code localhost:9091} */
  @Nullable
  public String getAddress() {
    return address;
  }

  /**
   * {@code job} label for metrics being pushed. Default is the name of the JAR file that is
   * running.
   */
  @Nullable
  public String getJob() {
    return job;
  }

  /**
   * Scheme to be used when pushing metrics to the pushgateway. Must be "http" or "https". Default
   * is "http".
   */
  @Nullable
  public String getScheme() {
    return scheme;
  }

  /** Escaping scheme to be used when pushing metric data to the pushgateway. */
  @Nullable
  public EscapingScheme getEscapingScheme() {
    return escapingScheme;
  }

  /** Connection timeout for connections to the Pushgateway. */
  @Nullable
  public Duration getConnectTimeout() {
    return connectTimeout;
  }

  /** Read timeout for connections to the Pushgateway. */
  @Nullable
  public Duration getReadTimeout() {
    return readTimeout;
  }

  /**
   * Note that this will remove entries from {@code properties}. This is because we want to know if
   * there are unused properties remaining after all properties have been loaded.
   */
  static ExporterPushgatewayProperties load(Map<Object, Object> properties)
      throws PrometheusPropertiesException {
    String address = Util.loadString(PREFIX + "." + ADDRESS, properties);
    String job = Util.loadString(PREFIX + "." + JOB, properties);
    String scheme = Util.loadString(PREFIX + "." + SCHEME, properties);
    String escapingScheme = Util.loadString(PREFIX + "." + ESCAPING_SCHEME, properties);
    Duration connectTimeout = Util.loadOptionalDuration(PREFIX + "." + CONNECT_TIMEOUT, properties);
    Duration readTimeout = Util.loadOptionalDuration(PREFIX + "." + READ_TIMEOUT, properties);

    if (scheme != null) {
      if (!scheme.equals("http") && !scheme.equals("https")) {
        throw new PrometheusPropertiesException(
            String.format(
                "%s.%s: Illegal value. Expecting 'http' or 'https'. Found: %s",
                PREFIX, SCHEME, scheme));
      }
    }

    return new ExporterPushgatewayProperties(
        address, job, scheme, parseEscapingScheme(escapingScheme), connectTimeout, readTimeout);
  }

  private static @Nullable EscapingScheme parseEscapingScheme(@Nullable String scheme) {
    if (scheme == null) {
      return null;
    }
    switch (scheme) {
      case "allow-utf-8":
        return EscapingScheme.ALLOW_UTF8;
      case "values":
        return EscapingScheme.VALUE_ENCODING_ESCAPING;
      case "underscores":
        return EscapingScheme.UNDERSCORE_ESCAPING;
      case "dots":
        return EscapingScheme.DOTS_ESCAPING;
      default:
        throw new PrometheusPropertiesException(
            String.format(
                "%s.%s: Illegal value. Expecting 'allow-utf-8', 'values', 'underscores', "
                    + "or 'dots'. Found: %s",
                PREFIX, ESCAPING_SCHEME, scheme));
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    @Nullable private String address;
    @Nullable private String job;
    @Nullable private String scheme;
    @Nullable private EscapingScheme escapingScheme;
    @Nullable private Duration connectTimeout;
    @Nullable private Duration readTimeout;

    private Builder() {}

    public Builder address(String address) {
      this.address = address;
      return this;
    }

    public Builder job(String job) {
      this.job = job;
      return this;
    }

    public Builder scheme(String scheme) {
      this.scheme = scheme;
      return this;
    }

    public Builder escapingScheme(EscapingScheme escapingScheme) {
      this.escapingScheme = escapingScheme;
      return this;
    }

    public Builder connectTimeout(Duration connectTimeout) {
      this.connectTimeout = connectTimeout;
      return this;
    }

    public Builder readTimeout(Duration readTimeout) {
      this.readTimeout = readTimeout;
      return this;
    }

    public ExporterPushgatewayProperties build() {
      return new ExporterPushgatewayProperties(
          address, job, scheme, escapingScheme, connectTimeout, readTimeout);
    }
  }
}

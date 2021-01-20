package io.prometheus.client.exporter.common.bridge;

import io.prometheus.client.exporter.common.format.MetricFamilySamplesTextFormatter;

/**
 * Configuration for bridge.
 */
public class BridgeConfig {
  /**
   * The hostname.
   */
  private final String host;

  /**
   * The host's port number.
   */
  private final int port;

  /**
   * The metric formatter.
   */
  private final MetricFamilySamplesTextFormatter formatter;

  /**
   * Construct a brdige config with the given host:port.
   * @param builder The builder.
   */
  private BridgeConfig(final Builder builder) {
    this.host = builder.host;
    this.port = builder.port;
    this.formatter = builder.formatter;
  }

  /**
   * Gets the host name.
   * @return The host name.
   */
  public String getHost() {
    return this.host;
  }

  /**
   * Gets the port number.
   * @return The port number.
   */
  public int getPort() {
    return this.port;
  }

  /**
   * Gets the metric formatter.
   * @return The metric formatter.
   */
  public MetricFamilySamplesTextFormatter getFormatter() {
    return this.formatter;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return new StringBuilder()
        .append(super.toString()).append("{")
        .append("host=").append(getHost()).append(", ")
        .append("port=").append(getPort()).append(", ")
        .append("formatter=").append(getFormatter())
        .append("}")
        .toString();
  }

  /**
   * Creates a new {@code BridgeConfig} builder.
   * @return The builder.
   */
  public static final Builder builder() {
    return new Builder();
  }

  /**
   * Builder class for {@code BridgeConfig}.
   */
  public static final class Builder {
    /**
     * The hostname.
     */
    private String host;

    /**
     * The host's port number.
     */
    private Integer port;

    /**
     * The metric foromatter.
     */
    private MetricFamilySamplesTextFormatter formatter;

    /**
     * Adds the {@code host} property to the builder.
     * @param host The host name.
     * @return The builder instance.
     */
    public Builder host(final String host) {
      this.host = host;
      return this;
    }

    /**
     * Adds the {@code port} property to the builder.
     * @param port The port number.
     * @return The builder instance.
     */
    public Builder port(final int port) {
      this.port = port;
      return this;
    }

    /**
     * Adds the {@code foramtter} property to the builder.
     * @param formatter The metric formatter.
     * @return The builder instance.
     */
    public Builder formatter(final MetricFamilySamplesTextFormatter formatter) {
      this.formatter = formatter;
      return this;
    }

    /**
     * Builds the {@code BrdigeConfig}.
     * @return The config.
     */
    public BridgeConfig build() {
      return new BridgeConfig(this);
    }
  }
}

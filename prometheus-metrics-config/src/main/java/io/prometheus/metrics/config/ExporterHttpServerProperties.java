package io.prometheus.metrics.config;

import java.util.Map;

/** Properties starting with io.prometheus.exporter.httpServer */
public class ExporterHttpServerProperties {

  private static final String PORT = "port";
  private static final String PREFIX = "io.prometheus.exporter.httpServer";
  private final Integer port;

  private ExporterHttpServerProperties(Integer port) {
    this.port = port;
  }

  public Integer getPort() {
    return port;
  }

  /**
   * Note that this will remove entries from {@code properties}. This is because we want to know if
   * there are unused properties remaining after all properties have been loaded.
   */
  static ExporterHttpServerProperties load(Map<Object, Object> properties)
      throws PrometheusPropertiesException {
    Integer port = Util.loadInteger(PREFIX + "." + PORT, properties);
    Util.assertValue(port, t -> t > 0, "Expecting value > 0.", PREFIX, PORT);
    return new ExporterHttpServerProperties(port);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private Integer port;

    private Builder() {}

    public Builder port(int port) {
      this.port = port;
      return this;
    }

    public ExporterHttpServerProperties build() {
      return new ExporterHttpServerProperties(port);
    }
  }
}

package io.prometheus.metrics.config;

import javax.annotation.Nullable;

/** Properties starting with io.prometheus.exporter.http_server */
public class ExporterHttpServerProperties {

  private static final String PORT = "port";
  private static final String PREFER_UNCOMPRESSED_RESPONSE = "prefer_uncompressed_response";
  private static final String PREFIX = "io.prometheus.exporter.http_server";
  @Nullable private final Integer port;
  private final boolean preferUncompressedResponse;

  private ExporterHttpServerProperties(@Nullable Integer port, boolean preferUncompressedResponse) {
    this.port = port;
    this.preferUncompressedResponse = preferUncompressedResponse;
  }

  @Nullable
  public Integer getPort() {
    return port;
  }

  public boolean isPreferUncompressedResponse() {
    return preferUncompressedResponse;
  }

  /**
   * Note that this will remove entries from {@code propertySource}. This is because we want to know
   * if there are unused properties remaining after all properties have been loaded.
   */
  static ExporterHttpServerProperties load(PropertySource propertySource)
      throws PrometheusPropertiesException {
    Integer port = Util.loadInteger(PREFIX, PORT, propertySource);
    Util.assertValue(port, t -> t > 0, "Expecting value > 0.", PREFIX, PORT);

    Boolean preferUncompressedResponse =
        Util.loadBoolean(PREFIX, PREFER_UNCOMPRESSED_RESPONSE, propertySource);

    return new ExporterHttpServerProperties(
        port, preferUncompressedResponse != null && preferUncompressedResponse);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    @Nullable private Integer port;
    private boolean preferUncompressedResponse = false;

    private Builder() {}

    public Builder port(int port) {
      this.port = port;
      return this;
    }

    public Builder preferUncompressedResponse(boolean preferUncompressedResponse) {
      this.preferUncompressedResponse = preferUncompressedResponse;
      return this;
    }

    public ExporterHttpServerProperties build() {
      return new ExporterHttpServerProperties(port, preferUncompressedResponse);
    }
  }
}

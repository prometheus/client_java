package io.prometheus.metrics.config;

import java.util.Map;

public class NamingProperties {

  private static final String PREFIX = "io.prometheus.naming";
  private static final String VALIDATION_SCHEME = "validationScheme";
  private final String validationScheme;

  private NamingProperties(String validation) {
    this.validationScheme = validation;
  }

  public String getValidationScheme() {
    return validationScheme;
  }

  static NamingProperties load(Map<Object, Object> properties) throws PrometheusPropertiesException {
    String validationScheme = Util.loadString(PREFIX + "." + VALIDATION_SCHEME, properties);
    return new NamingProperties(validationScheme);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String validationScheme;

    private Builder() {}

    public Builder validation(String validationScheme) {
      this.validationScheme = validationScheme;
      return this;
    }

    public NamingProperties build() {
      return new NamingProperties(validationScheme);
    }
  }

}
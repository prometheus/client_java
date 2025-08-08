package io.prometheus.metrics.config;

import java.util.Map;
import javax.annotation.Nullable;

public class NamingProperties {

  private static final String PREFIX = "io.prometheus.naming";
  private static final String VALIDATION_SCHEME = "validationScheme";
  private final ValidationScheme validationScheme;

  private NamingProperties(ValidationScheme validation) {
    this.validationScheme = validation;
  }

  public ValidationScheme getValidationScheme() {
    return validationScheme;
  }

  static NamingProperties load(Map<Object, Object> properties)
      throws PrometheusPropertiesException {
    String validationScheme = Util.loadString(PREFIX + "." + VALIDATION_SCHEME, properties);
    return new NamingProperties(parseValidationScheme(validationScheme));
  }

  static ValidationScheme parseValidationScheme(@Nullable String scheme) {
    if (scheme == null || scheme.isEmpty()) {
      return ValidationScheme.LEGACY_VALIDATION;
    }

    switch (scheme) {
      case "utf-8":
        return ValidationScheme.UTF_8_VALIDATION;
      case "legacy":
        return ValidationScheme.LEGACY_VALIDATION;
      default:
        throw new PrometheusPropertiesException(
            "Unknown validation scheme: " + scheme + ". Valid values are: utf-8, legacy.");
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private ValidationScheme validationScheme;

    private Builder() {}

    public Builder validation(ValidationScheme validationScheme) {
      this.validationScheme = validationScheme;
      return this;
    }

    public NamingProperties build() {
      return new NamingProperties(validationScheme);
    }
  }
}

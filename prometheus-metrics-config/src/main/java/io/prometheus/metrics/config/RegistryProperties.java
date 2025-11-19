package io.prometheus.metrics.config;

import java.util.Map;
import javax.annotation.Nullable;

/** Properties starting with io.prometheus.registry */
public class RegistryProperties {

  private static final String ALLOW_DUPLICATE_REGISTRATION = "allowDuplicateRegistration";
  private static final String PREFIX = "io.prometheus.registry";

  @Nullable private final Boolean allowDuplicateRegistration;

  private RegistryProperties(@Nullable Boolean allowDuplicateRegistration) {
    this.allowDuplicateRegistration = allowDuplicateRegistration;
  }

  /**
   * If true, allows registering multiple collectors with the same metric name but potentially
   * different label sets. Default is false for backward compatibility.
   *
   * <p>When enabled, metrics with the same name but different labels can coexist, which is useful
   * for integration with frameworks like Micrometer that may create multiple collectors for the
   * same metric with varying label sets.
   *
   * <p>Note: Registering metrics with the same name AND the same label set may produce confusing
   * results in Prometheus queries, as multiple time series with identical labels will be present.
   *
   * <p>This property only affects registries created with {@code new
   * PrometheusRegistry(allowDuplicateRegistration)}. The static {@code
   * PrometheusRegistry.defaultRegistry} always uses the default value (false).
   *
   * <p>Property: {@code io.prometheus.registry.allowDuplicateRegistration}
   *
   * @return true if duplicate registration is allowed, false otherwise (default: false)
   */
  public boolean getAllowDuplicateRegistration() {
    return allowDuplicateRegistration != null && allowDuplicateRegistration;
  }

  /**
   * Note that this will remove entries from {@code properties}. This is because we want to know if
   * there are unused properties remaining after all properties have been loaded.
   */
  static RegistryProperties load(Map<Object, Object> properties)
      throws PrometheusPropertiesException {
    Boolean allowDuplicateRegistration =
        Util.loadBoolean(PREFIX + "." + ALLOW_DUPLICATE_REGISTRATION, properties);
    return new RegistryProperties(allowDuplicateRegistration);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    @Nullable private Boolean allowDuplicateRegistration;

    private Builder() {}

    /** See {@link RegistryProperties#getAllowDuplicateRegistration()} */
    public Builder allowDuplicateRegistration(boolean allowDuplicateRegistration) {
      this.allowDuplicateRegistration = allowDuplicateRegistration;
      return this;
    }

    public RegistryProperties build() {
      return new RegistryProperties(allowDuplicateRegistration);
    }
  }
}

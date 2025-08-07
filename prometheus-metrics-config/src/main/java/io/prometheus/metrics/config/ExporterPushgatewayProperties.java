package io.prometheus.metrics.config;

import java.util.Map;

public class ExporterPushgatewayProperties {

  private static final String ADDRESS = "address";
  private static final String JOB = "job";
  private static final String SCHEME = "scheme";
  private static final String ESCAPING_SCHEME = "escapingScheme";
  private static final String PREFIX = "io.prometheus.exporter.pushgateway";
  private final String scheme;
  private final String address;
  private final String job;
  private final String escapingScheme;

  private ExporterPushgatewayProperties(String address, String job, String scheme, String escapingScheme) {
    this.address = address;
    this.job = job;
    this.scheme = scheme;
    this.escapingScheme = escapingScheme;
  }

  /** Address of the Pushgateway in the form {@code host:port}. Default is {@code localhost:9091} */
  public String getAddress() {
    return address;
  }

  /**
   * {@code job} label for metrics being pushed. Default is the name of the JAR file that is
   * running.
   */
  public String getJob() {
    return job;
  }

  /**
   * Scheme to be used when pushing metrics to the pushgateway. Must be "http" or "https". Default
   * is "http".
   */
  public String getScheme() {
    return scheme;
  }

  /**
   * Escaping scheme to be used when pushing metric data to the pushgateway.
   * Valid values: "no-escaping", "values", "underscores", "dots". Default is "no-escaping".
   */
  public String getEscapingScheme() {
    return escapingScheme;
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
    
    if (scheme != null) {
      if (!scheme.equals("http") && !scheme.equals("https")) {
        throw new PrometheusPropertiesException(
            String.format(
                "%s.%s: Illegal value. Expecting 'http' or 'https'. Found: %s",
                PREFIX, SCHEME, scheme));
      }
    }
    
    if (escapingScheme != null) {
      if (!escapingScheme.equals("no-escaping") && !escapingScheme.equals("values") 
          && !escapingScheme.equals("underscores") && !escapingScheme.equals("dots")) {
        throw new PrometheusPropertiesException(
            String.format(
                "%s.%s: Illegal value. Expecting 'no-escaping', 'values', 'underscores', or 'dots'. Found: %s",
                PREFIX, ESCAPING_SCHEME, escapingScheme));
      }
    }
    
    return new ExporterPushgatewayProperties(address, job, scheme, escapingScheme);
  }
}

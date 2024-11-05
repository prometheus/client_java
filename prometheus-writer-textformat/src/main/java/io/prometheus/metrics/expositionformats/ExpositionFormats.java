package io.prometheus.metrics.expositionformats;

import io.prometheus.metrics.config.ExporterProperties;
import io.prometheus.metrics.config.PrometheusProperties;
import javax.annotation.Nullable;

public class ExpositionFormats {

  @Nullable private final ExpositionFormatWriter prometheusProtobufWriter;
  private final ExpositionFormatWriter prometheusTextFormatWriter;
  private final ExpositionFormatWriter openMetricsTextFormatWriter;

  private ExpositionFormats(
      ExpositionFormatWriter prometheusProtobufWriter,
      ExpositionFormatWriter prometheusTextFormatWriter,
      ExpositionFormatWriter openMetricsTextFormatWriter) {
    this.prometheusProtobufWriter = prometheusProtobufWriter;
    this.prometheusTextFormatWriter = prometheusTextFormatWriter;
    this.openMetricsTextFormatWriter = openMetricsTextFormatWriter;
  }

  public static ExpositionFormats init() {
    return init(PrometheusProperties.get().getExporterProperties());
  }

  public static ExpositionFormats init(ExporterProperties properties) {
    return new ExpositionFormats(
        createProtobufWriter(),
        new PrometheusTextFormatWriter(properties.getIncludeCreatedTimestamps()),
        new OpenMetricsTextFormatWriter(
            properties.getIncludeCreatedTimestamps(), properties.getExemplarsOnAllMetricTypes()));
  }

  @Nullable
  public static ExpositionFormatWriter createProtobufWriter() {
    try {
      return Class.forName("io.prometheus.metrics.expositionformats.PrometheusProtobufWriter")
          .asSubclass(ExpositionFormatWriter.class)
          .getDeclaredConstructor()
          .newInstance();
    } catch (Exception e) {
      // not in classpath
      return null;
    }
  }

  public ExpositionFormatWriter findWriter(String acceptHeader) {
    if (prometheusProtobufWriter.accepts(acceptHeader)) {
      return prometheusProtobufWriter;
    }
    if (openMetricsTextFormatWriter.accepts(acceptHeader)) {
      return openMetricsTextFormatWriter;
    }
    return prometheusTextFormatWriter;
  }

  public ExpositionFormatWriter getPrometheusProtobufWriter() {
    return prometheusProtobufWriter;
  }

  public ExpositionFormatWriter getPrometheusTextFormatWriter() {
    return prometheusTextFormatWriter;
  }

  public ExpositionFormatWriter getOpenMetricsTextFormatWriter() {
    return openMetricsTextFormatWriter;
  }
}

package io.prometheus.metrics.expositionformats;

import io.prometheus.metrics.config.ExporterProperties;
import io.prometheus.metrics.config.PrometheusProperties;

public class ExpositionFormats {

  private final PrometheusProtobufWriter prometheusProtobufWriter;
  private final PrometheusTextFormatWriter prometheusTextFormatWriter;
  private final OpenMetricsTextFormatWriter openMetricsTextFormatWriter;

  private ExpositionFormats(
      PrometheusProtobufWriter prometheusProtobufWriter,
      PrometheusTextFormatWriter prometheusTextFormatWriter,
      OpenMetricsTextFormatWriter openMetricsTextFormatWriter) {
    this.prometheusProtobufWriter = prometheusProtobufWriter;
    this.prometheusTextFormatWriter = prometheusTextFormatWriter;
    this.openMetricsTextFormatWriter = openMetricsTextFormatWriter;
  }

  public static ExpositionFormats init() {
    return init(PrometheusProperties.get().getExporterProperties());
  }

  @SuppressWarnings("deprecation")
  public static ExpositionFormats init(ExporterProperties properties) {
    return new ExpositionFormats(
        new PrometheusProtobufWriter(),
        PrometheusTextFormatWriter.builder()
            .setIncludeCreatedTimestamps(properties.getIncludeCreatedTimestamps())
            .setTimestampsInMs(properties.getPrometheusTimestampsInMs())
            .build(),
        OpenMetricsTextFormatWriter.builder()
            .setCreatedTimestampsEnabled(properties.getIncludeCreatedTimestamps())
            .setExemplarsOnAllMetricTypesEnabled(properties.getExemplarsOnAllMetricTypes())
            .build());
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

  public PrometheusProtobufWriter getPrometheusProtobufWriter() {
    return prometheusProtobufWriter;
  }

  public PrometheusTextFormatWriter getPrometheusTextFormatWriter() {
    return prometheusTextFormatWriter;
  }

  public OpenMetricsTextFormatWriter getOpenMetricsTextFormatWriter() {
    return openMetricsTextFormatWriter;
  }
}

package io.prometheus.metrics.expositionformats;

import io.prometheus.metrics.config.ExporterProperties;
import io.prometheus.metrics.config.OpenMetrics2Properties;
import io.prometheus.metrics.config.PrometheusProperties;
import javax.annotation.Nullable;

public class ExpositionFormats {

  private final PrometheusProtobufWriter prometheusProtobufWriter;
  private final PrometheusTextFormatWriter prometheusTextFormatWriter;
  private final OpenMetricsTextFormatWriter openMetricsTextFormatWriter;
  private final OpenMetrics2TextFormatWriter openMetrics2TextFormatWriter;

  private ExpositionFormats(
      PrometheusProtobufWriter prometheusProtobufWriter,
      PrometheusTextFormatWriter prometheusTextFormatWriter,
      OpenMetricsTextFormatWriter openMetricsTextFormatWriter,
      OpenMetrics2TextFormatWriter openMetrics2TextFormatWriter) {
    this.prometheusProtobufWriter = prometheusProtobufWriter;
    this.prometheusTextFormatWriter = prometheusTextFormatWriter;
    this.openMetricsTextFormatWriter = openMetricsTextFormatWriter;
    this.openMetrics2TextFormatWriter = openMetrics2TextFormatWriter;
  }

  public static ExpositionFormats init() {
    return init(PrometheusProperties.get());
  }

  @SuppressWarnings("deprecation")
  public static ExpositionFormats init(PrometheusProperties properties) {
    ExporterProperties exporterProps = properties.getExporterProperties();
    OpenMetrics2Properties om2Props = properties.getOpenMetrics2Properties();

    return new ExpositionFormats(
        new PrometheusProtobufWriter(),
        PrometheusTextFormatWriter.builder()
            .setIncludeCreatedTimestamps(exporterProps.getIncludeCreatedTimestamps())
            .setTimestampsInMs(exporterProps.getPrometheusTimestampsInMs())
            .build(),
        OpenMetricsTextFormatWriter.builder()
            .setCreatedTimestampsEnabled(exporterProps.getIncludeCreatedTimestamps())
            .setExemplarsOnAllMetricTypesEnabled(exporterProps.getExemplarsOnAllMetricTypes())
            .build(),
        OpenMetrics2TextFormatWriter.builder()
            .setOpenMetrics2Properties(om2Props)
            .setCreatedTimestampsEnabled(exporterProps.getIncludeCreatedTimestamps())
            .setExemplarsOnAllMetricTypesEnabled(exporterProps.getExemplarsOnAllMetricTypes())
            .build());
  }

  /**
   * @deprecated Use {@link #init(PrometheusProperties)} instead.
   */
  @Deprecated
  @SuppressWarnings({"InlineMeSuggester"})
  public static ExpositionFormats init(ExporterProperties properties) {
    return init(PrometheusProperties.builder().exporterProperties(properties).build());
  }

  public ExpositionFormatWriter findWriter(@Nullable String acceptHeader) {
    if (prometheusProtobufWriter.accepts(acceptHeader)) {
      return prometheusProtobufWriter;
    }

    // Prefer OM2 over OM1 when any OM2 feature is enabled
    if (isOpenMetrics2Enabled() && openMetrics2TextFormatWriter.accepts(acceptHeader)) {
      return openMetrics2TextFormatWriter;
    }

    if (openMetricsTextFormatWriter.accepts(acceptHeader)) {
      return openMetricsTextFormatWriter;
    }

    return prometheusTextFormatWriter;
  }

  private boolean isOpenMetrics2Enabled() {
    OpenMetrics2Properties props = openMetrics2TextFormatWriter.getOpenMetrics2Properties();
    return props.getContentNegotiation()
        || props.getCompositeValues()
        || props.getExemplarCompliance()
        || props.getNativeHistograms();
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

  public OpenMetrics2TextFormatWriter getOpenMetrics2TextFormatWriter() {
    return openMetrics2TextFormatWriter;
  }
}

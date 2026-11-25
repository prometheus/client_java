package io.prometheus.metrics.expositionformats;

import io.prometheus.metrics.annotations.StableApi;
import io.prometheus.metrics.config.EscapingScheme;
import io.prometheus.metrics.config.ExporterProperties;
import io.prometheus.metrics.config.OpenMetrics2Properties;
import io.prometheus.metrics.config.PrometheusProperties;
import javax.annotation.Nullable;

@StableApi
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

  /**
   * Finds the exposition format writer for the given Accept header.
   *
   * @see #findWriterWithEscaping(String) to select a format and bind its escaping scheme together
   */
  public ExpositionFormatWriter findWriter(@Nullable String acceptHeader) {
    if (prometheusProtobufWriter.accepts(acceptHeader)) {
      return prometheusProtobufWriter;
    }

    if (isOpenMetrics2Enabled() && openMetrics2TextFormatWriter.accepts(acceptHeader)) {
      if (openMetrics2TextFormatWriter.getOpenMetrics2Properties().getContentNegotiation()) {
        String version = parseOpenMetricsVersion(findOpenMetricsMediaType(acceptHeader));
        if ("2.0.0".equals(version)) {
          return openMetrics2TextFormatWriter;
        }
        // version=1.0.0 or no version: fall through to OM1.
      } else {
        // contentNegotiation=false: OM2 handles all OpenMetrics requests.
        return openMetrics2TextFormatWriter;
      }
    }

    if (openMetricsTextFormatWriter.accepts(acceptHeader)) {
      return openMetricsTextFormatWriter;
    }

    return prometheusTextFormatWriter;
  }

  /**
   * Finds the exposition format writer and escaping scheme negotiated by the given Accept header.
   *
   * <p>Use this method when writing a scrape response to select the format and its escaping scheme
   * from the same {@code Accept} header. Only parameters on the media range accepted by the
   * selected format affect the escaping scheme. The returned writer's content type is the format's
   * standard content type; as with {@link #findWriter(String)}, it does not include an escaping
   * parameter.
   *
   * <pre>{@code
   * ExpositionFormats.init().findWriterWithEscaping(acceptHeader).write(out, registry.scrape());
   * }</pre>
   *
   * @see #findWriter(String) to select only the format writer
   */
  public ExpositionFormatWriterWithEscaping findWriterWithEscaping(@Nullable String acceptHeader) {
    ExpositionFormatWriter writer = findWriter(acceptHeader);
    String mediaType = findMatchingMediaType(writer, acceptHeader);
    EscapingScheme fallback = EscapingScheme.DEFAULT;
    // OpenMetrics 2.0 has no escaping parameter and permits UTF-8 names. Only an explicit request
    // for version 2.0.0 selects unescaped names; the OM2 writer may also serve OM1/unversioned
    // requests when content negotiation is disabled.
    if (writer instanceof OpenMetrics2TextFormatWriter
        && "2.0.0".equals(parseOpenMetricsVersion(mediaType))) {
      fallback = EscapingScheme.ALLOW_UTF8;
    }
    return new ExpositionFormatWriterWithEscaping(
        writer, EscapingScheme.fromAcceptHeader(mediaType, fallback));
  }

  @Nullable
  private static String findMatchingMediaType(
      ExpositionFormatWriter writer, @Nullable String acceptHeader) {
    if (acceptHeader == null) {
      return null;
    }
    for (String mediaType : acceptHeader.split(",")) {
      if (writer.accepts(mediaType)) {
        return mediaType;
      }
    }
    return null;
  }

  @Nullable
  private static String findOpenMetricsMediaType(@Nullable String acceptHeader) {
    if (acceptHeader == null) {
      return null;
    }
    for (String mediaType : acceptHeader.split(",")) {
      if (mediaType.contains("application/openmetrics-text")) {
        return mediaType;
      }
    }
    return null;
  }

  private boolean isOpenMetrics2Enabled() {
    return openMetrics2TextFormatWriter.getOpenMetrics2Properties().getEnabled();
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

  @Nullable
  private static String parseOpenMetricsVersion(@Nullable String mediaType) {
    if (mediaType == null) {
      return null;
    }
    for (String param : mediaType.split(";")) {
      String[] tokens = param.split("=");
      if (tokens.length == 2) {
        String key = tokens[0].trim();
        String value = tokens[1].trim();
        if (key.equals("version")) {
          return value;
        }
      }
    }
    return null;
  }
}

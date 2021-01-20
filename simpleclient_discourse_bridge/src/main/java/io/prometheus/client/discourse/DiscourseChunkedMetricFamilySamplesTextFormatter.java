package io.prometheus.client.discourse;

import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.prometheus.client.Collector;
import io.prometheus.client.exporter.common.format.AbstractMetricFamilySamplesTextFormatter;

/**
 * Implementation of {@code MetricFamilySamplesTextFormatter} for discourse's {@code prometheus_exporter}.
 *
 * @see {@link https://tools.ietf.org/html/rfc7230#section-4.1}
 */
public class DiscourseChunkedMetricFamilySamplesTextFormatter extends AbstractMetricFamilySamplesTextFormatter {
  /**
   * The logging utility.
   */
  protected static final Logger LOGGER = Logger.getLogger(DiscourseChunkedMetricFamilySamplesTextFormatter.class.getName());

  /**
   * The host name.
   */
  private final String host;

  /**
   * Construct a {@code DiscourseChunkedMetricFamilySamplesTextFormatter}.
   * @param host The host.
   */
  public DiscourseChunkedMetricFamilySamplesTextFormatter(final String host) {
    this.host = host;
  }

  /**
   * Gets the host name.
   * @return The host name.
   */
  protected String getHost() {
    return this.host;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void write(final Writer writer, final Enumeration<Collector.MetricFamilySamples> mfs) throws IOException {
    writer.write("POST /send-metrics HTTP/1.1\r\n");
    writer.write("Transfer-Encoding: chunked\r\n");
    writer.write("Host: " + getHost() + "\r\n");
    writer.write("Connection: Close\r\n");
    writer.write("Content-Type: application/octet-stream\r\n");
    writer.write("\r\n");

    while (mfs.hasMoreElements()) {
      final Collector.MetricFamilySamples metricFamilySamples = mfs.nextElement();
      for (final Collector.MetricFamilySamples.Sample sample : metricFamilySamples.samples) {
        if (!metricFamilySamples.name.matches("[a-zA-Z_:][a-zA-Z0-9_:]*")) {
          LOGGER.log(Level.WARNING, "Skipping, invalid metric name: " + metricFamilySamples.name);
          continue;
        }

        final StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");
        jsonBuilder.append("\"help\":\"").append(metricFamilySamples.help.replace("\"", "\\\"")).append("\",");
        jsonBuilder.append("\"type\":\"").append(formatTypeToString(metricFamilySamples.type)).append("\",");
        jsonBuilder.append("\"name\":\"").append(metricFamilySamples.name).append("\",");
        jsonBuilder.append("\"keys\":{");
        for (int i = 0; i < sample.labelNames.size(); ++i) {
          if (i > 0) {
            jsonBuilder.append(",");
          }
          jsonBuilder.append("\"")
              .append(sample.labelNames.get(i))
              .append("\":\"")
              .append(sample.labelValues.get(i).replace("\"", "\\\""))
              .append("\"");
        }
        jsonBuilder.append("},");
        jsonBuilder.append("\"value\":").append(sample.value);
        jsonBuilder.append("}");

        final String chunk = jsonBuilder.toString();
        writer.write(Integer.toHexString(chunk.getBytes(getCharset()).length).toUpperCase());
        writer.write("\r\n");
        writer.write(chunk);
        writer.write("\r\n");
      }
    }

    writer.write("0\r\n");
    writer.write("\r\n");
  }
}

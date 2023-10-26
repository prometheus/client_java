package io.prometheus.metrics.exporter.common;

import io.prometheus.metrics.config.ExporterFilterProperties;
import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.expositionformats.ExpositionFormatWriter;
import io.prometheus.metrics.expositionformats.ExpositionFormats;
import io.prometheus.metrics.model.registry.MetricNameFilter;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.zip.GZIPOutputStream;

/**
 * Prometheus scrape endpoint.
 */
public class PrometheusScrapeHandler {

    private final PrometheusRegistry registry;
    private final ExpositionFormats expositionFormats;
    private final Predicate<String> nameFilter;
    private AtomicInteger lastResponseSize = new AtomicInteger(2 << 9); //  0.5 MB

    public PrometheusScrapeHandler() {
        this(PrometheusProperties.get(), PrometheusRegistry.defaultRegistry);
    }

    public PrometheusScrapeHandler(PrometheusRegistry registry) {
        this(PrometheusProperties.get(), registry);
    }

    public PrometheusScrapeHandler(PrometheusProperties config) {
        this(config, PrometheusRegistry.defaultRegistry);
    }

    public PrometheusScrapeHandler(PrometheusProperties config, PrometheusRegistry registry) {
        this.expositionFormats = ExpositionFormats.init(config.getExporterProperties());
        this.registry = registry;
        this.nameFilter = makeNameFilter(config.getExporterFilterProperties());
    }

    public void handleRequest(PrometheusHttpExchange exchange) throws IOException {
        try {
            PrometheusHttpRequest request = exchange.getRequest();
            PrometheusHttpResponse response = exchange.getResponse();
            MetricSnapshots snapshots = scrape(request);
            if (writeDebugResponse(snapshots, exchange)) {
                return;
            }
            ByteArrayOutputStream responseBuffer = new ByteArrayOutputStream(lastResponseSize.get() + 1024);
            String acceptHeader = request.getHeader("Accept");
            ExpositionFormatWriter writer = expositionFormats.findWriter(acceptHeader);
            writer.write(responseBuffer, snapshots);
            lastResponseSize.set(responseBuffer.size());
            response.setHeader("Content-Type", writer.getContentType());

            if (shouldUseCompression(request)) {
                response.setHeader("Content-Encoding", "gzip");
                try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(response.sendHeadersAndGetBody(200, 0))) {
                    responseBuffer.writeTo(gzipOutputStream);
                }
            } else {
                int contentLength = responseBuffer.size();
                if (contentLength > 0) {
                    response.setHeader("Content-Length", String.valueOf(contentLength));
                }
                if (request.getMethod().equals("HEAD")) {
                    // The HTTPServer implementation will throw an Exception if we close the output stream
                    // without sending a response body, so let's not close the output stream in case of a HEAD response.
                    response.sendHeadersAndGetBody(200, -1);
                } else {
                    try (OutputStream outputStream = response.sendHeadersAndGetBody(200, contentLength)) {
                        responseBuffer.writeTo(outputStream);
                    }
                }
            }
        } catch (IOException e) {
            exchange.handleException(e);
        } catch (RuntimeException e) {
            exchange.handleException(e);
        } finally {
            exchange.close();
        }
    }

    private Predicate<String> makeNameFilter(ExporterFilterProperties props) {
        if (props.getAllowedMetricNames() == null && props.getExcludedMetricNames() == null && props.getAllowedMetricNamePrefixes() == null && props.getExcludedMetricNamePrefixes() == null) {
            return null;
        } else {
            return MetricNameFilter.builder()
                    .nameMustBeEqualTo(props.getAllowedMetricNames())
                    .nameMustNotBeEqualTo(props.getExcludedMetricNames())
                    .nameMustStartWith(props.getAllowedMetricNamePrefixes())
                    .nameMustNotStartWith(props.getExcludedMetricNamePrefixes())
                    .build();
        }
    }

    private MetricSnapshots scrape(PrometheusHttpRequest request) {

        Predicate<String> filter = makeNameFilter(request.getParameterValues("name[]"));
        if (filter != null) {
            return registry.scrape(filter, request);
        } else {
            return registry.scrape(request);
        }
    }

    private Predicate<String> makeNameFilter(String[] includedNames) {
        Predicate<String> result = null;
        if (includedNames != null && includedNames.length > 0) {
            result = MetricNameFilter.builder().nameMustBeEqualTo(includedNames).build();
        }
        if (result != null && nameFilter != null) {
            result = result.and(nameFilter);
        } else if (nameFilter != null) {
            result = nameFilter;
        }
        return result;
    }

    private boolean writeDebugResponse(MetricSnapshots snapshots, PrometheusHttpExchange exchange) throws IOException {
        String debugParam = exchange.getRequest().getParameter("debug");
        PrometheusHttpResponse response = exchange.getResponse();
        if (debugParam == null) {
            return false;
        } else {
            response.setHeader("Content-Type", "text/plain; charset=utf-8");
            boolean supportedFormat = Arrays.asList("openmetrics", "text", "prometheus-protobuf").contains(debugParam);
            int responseStatus = supportedFormat ? 200 : 500;
            OutputStream body = response.sendHeadersAndGetBody(responseStatus, 0);
            switch (debugParam) {
                case "openmetrics":
                    expositionFormats.getOpenMetricsTextFormatWriter().write(body, snapshots);
                    break;
                case "text":
                    expositionFormats.getPrometheusTextFormatWriter().write(body, snapshots);
                    break;
                case "prometheus-protobuf":
                    String debugString = expositionFormats.getPrometheusProtobufWriter().toDebugString(snapshots);
                    body.write(debugString.getBytes(StandardCharsets.UTF_8));
                    break;
                default:
                    body.write(("debug=" + debugParam + ": Unsupported query parameter. Valid values are 'openmetrics', 'text', and 'prometheus-protobuf'.").getBytes(StandardCharsets.UTF_8));
                    break;
            }
            return true;
        }
    }

    private boolean shouldUseCompression(PrometheusHttpRequest request) {
        Enumeration<String> encodingHeaders = request.getHeaders("Accept-Encoding");
        if (encodingHeaders == null) {
            return false;
        }
        while (encodingHeaders.hasMoreElements()) {
            String encodingHeader = encodingHeaders.nextElement();
            String[] encodings = encodingHeader.split(",");
            for (String encoding : encodings) {
                if (encoding.trim().equalsIgnoreCase("gzip")) {
                    return true;
                }
            }
        }
        return false;
    }
}

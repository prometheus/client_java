package io.prometheus.metrics.exporter.httpserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.prometheus.metrics.expositionformats.ExpositionFormatWriter;
import io.prometheus.metrics.model.registry.PrometheusRegistry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.zip.GZIPOutputStream;

/**
 * Handler for the /metrics endpoint
 */
public class MetricsHandler implements HttpHandler {

    private final List<ExpositionFormatWriter> expositionFormatWriters = new ArrayList<>();

    private static class LocalByteArray extends ThreadLocal<ByteArrayOutputStream> {
        @Override
        protected ByteArrayOutputStream initialValue() {
            return new ByteArrayOutputStream(1 << 20); // 1 MB
        }
    }

    private final PrometheusRegistry registry;
    private final LocalByteArray response = new LocalByteArray();
    private final Predicate<String> sampleNameFilter;

    public MetricsHandler(PrometheusRegistry registry, List<ExpositionFormatWriter> expositionFormatWriters) {
        this(registry, expositionFormatWriters, null);
    }

    public MetricsHandler(PrometheusRegistry registry,
                          List<ExpositionFormatWriter> expositionFormatWriters,
                          Predicate<String> sampleNameFilter) {
        this.registry = registry;
        this.sampleNameFilter = sampleNameFilter;
        this.expositionFormatWriters.addAll(expositionFormatWriters);
        if (expositionFormatWriters.isEmpty()) {
            throw new IllegalStateException("Need at least one exposition format");
        }
    }

    private ExpositionFormatWriter findExpositionFormatWriter(String acceptHeader) {
        for (ExpositionFormatWriter writer : expositionFormatWriters) {
            if (writer.accepts(acceptHeader)) {
                return writer;
            }
        }
        return expositionFormatWriters.get(0);
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        String query = t.getRequestURI().getRawQuery();
        ByteArrayOutputStream response = this.response.get();
        response.reset();
        String acceptHeader = t.getRequestHeaders().getFirst("Accept");
        ExpositionFormatWriter writer = findExpositionFormatWriter(acceptHeader);
        t.getResponseHeaders().set("Content-Type", writer.getContentType());
        Predicate<String> nameFilterFromQueryParameter = getNamesFilterFromQueryParameter(query);
        Predicate<String> filter = sampleNameFilter;
        if (filter != null && nameFilterFromQueryParameter != null) {
            filter = filter.and(nameFilterFromQueryParameter);
        } else if (nameFilterFromQueryParameter != null) {
            filter = nameFilterFromQueryParameter;
        }
        if (filter == null) {
            writer.write(response, registry.scrape());
        } else {
            writer.write(response, registry.scrape(filter)); // TODO: filter is "includes", but expected parameter is "excludes"
        }
        response.close();
        if (shouldUseCompression(t)) {
            t.getResponseHeaders().set("Content-Encoding", "gzip");
            t.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
            final GZIPOutputStream os = new GZIPOutputStream(t.getResponseBody());
            try {
                response.writeTo(os);
            } finally {
                os.close();
            }
        } else {
            long contentLength = response.size();
            if (contentLength > 0) {
                t.getResponseHeaders().set("Content-Length", String.valueOf(contentLength));
            }
            if (t.getRequestMethod().equals("HEAD")) {
                contentLength = -1;
            }
            t.sendResponseHeaders(HttpURLConnection.HTTP_OK, contentLength);
            response.writeTo(t.getResponseBody());
        }
        t.close();
    }

    private boolean shouldUseCompression(HttpExchange exchange) {
        List<String> encodingHeaders = exchange.getRequestHeaders().get("Accept-Encoding");
        if (encodingHeaders == null) return false;

        for (String encodingHeader : encodingHeaders) {
            String[] encodings = encodingHeader.split(",");
            for (String encoding : encodings) {
                if (encoding.trim().equalsIgnoreCase("gzip")) {
                    return true;
                }
            }
        }
        return false;
    }

    private Predicate<String> getNamesFilterFromQueryParameter(String query) throws IOException {
        Set<String> names = new HashSet<>();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                if (idx != -1 && URLDecoder.decode(pair.substring(0, idx), "UTF-8").equals("name[]")) {
                    names.add(URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
                }
            }
        }
        if (!names.isEmpty()) {
        return names::contains;
        } else {
            return null;
        }
    }
}

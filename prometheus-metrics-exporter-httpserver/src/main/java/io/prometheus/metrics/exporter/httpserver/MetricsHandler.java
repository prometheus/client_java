package io.prometheus.metrics.exporter.httpserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Handler for the /metrics endpoint
 */
public class MetricsHandler implements HttpHandler {

    private static class LocalByteArray extends ThreadLocal<ByteArrayOutputStream> {
        @Override
        protected ByteArrayOutputStream initialValue()
        {
            return new ByteArrayOutputStream(1 << 20); // 1 MB
        }
    }

    private final Supplier<MetricSnapshots> registry;
    private final LocalByteArray response = new LocalByteArray();
    private final Supplier<Predicate<String>> sampleNameFilterSupplier;

    public MetricsHandler(Supplier<MetricSnapshots> registry) {
        this(registry, null);
    }

    public MetricsHandler(Supplier<MetricSnapshots> registry, Supplier<Predicate<String>> sampleNameFilterSupplier) {
        this.registry = registry;
        this.sampleNameFilterSupplier = sampleNameFilterSupplier;
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        String query = t.getRequestURI().getRawQuery();
        String contextPath = t.getHttpContext().getPath();
        ByteArrayOutputStream response = this.response.get();
        response.reset();
        OutputStreamWriter osw = new OutputStreamWriter(response, Charset.forName("UTF-8"));
            String contentType = TextFormat.chooseContentType(t.getRequestHeaders().getFirst("Accept"));
            t.getResponseHeaders().set("Content-Type", contentType);
            Predicate<String> filter = sampleNameFilterSupplier == null ? null : sampleNameFilterSupplier.get();
            filter = SampleNameFilter.restrictToNamesEqualTo(filter, parseQuery(query));
            if (filter == null) {
                TextFormat.writeFormat(contentType, osw, registry.metricFamilySamples());
            } else {
                TextFormat.writeFormat(contentType, osw, registry.filteredMetricFamilySamples(filter));
            }

        osw.close();

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
}

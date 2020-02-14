package io.prometheus.client.exporter;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.util.zip.GZIPOutputStream;

public class HTTPMetricHandler implements HttpHandler {

    private static class LocalByteArray extends ThreadLocal<ByteArrayOutputStream> {
        protected ByteArrayOutputStream initialValue()
        {
            return new ByteArrayOutputStream(1 << 20);
        }
    }
    private CollectorRegistry registry;
    private final LocalByteArray response = new LocalByteArray();

    public HTTPMetricHandler(CollectorRegistry registry) {
      this.registry = registry;
    }


    public void handle(HttpExchange t) throws IOException {
        String query = t.getRequestURI().getRawQuery();

        ByteArrayOutputStream response = this.response.get();
        response.reset();
        OutputStreamWriter osw = new OutputStreamWriter(response);
        TextFormat.write004(osw,
                registry.filteredMetricFamilySamples(HTTPServer.parseQuery(query)));
        osw.flush();
        osw.close();
        response.flush();
        response.close();

        t.getResponseHeaders().set("Content-Type",
                TextFormat.CONTENT_TYPE_004);
        if (HTTPServer.shouldUseCompression(t)) {
            t.getResponseHeaders().set("Content-Encoding", "gzip");
            t.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
            final GZIPOutputStream os = new GZIPOutputStream(t.getResponseBody());
            response.writeTo(os);
            os.close();
        } else {
            t.getResponseHeaders().set("Content-Length",
                    String.valueOf(response.size()));
            t.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.size());
            response.writeTo(t.getResponseBody());
        }
        t.close();
    }

}

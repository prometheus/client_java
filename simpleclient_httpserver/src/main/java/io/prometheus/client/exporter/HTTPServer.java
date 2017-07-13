package io.prometheus.client.exporter;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

/**
 * Expose Prometheus metrics using a plain Java HttpServer.
 * <p>
 * Example Usage:
 * <pre>
 * {@code
 * HTTPServer server = new HTTPServer(1234);
 * }
 * </pre>
 * */
public class HTTPServer {
    static class HTTPMetricHandler implements HttpHandler {
        private CollectorRegistry registry;

        HTTPMetricHandler(CollectorRegistry registry) {
          this.registry = registry;
        }


        public void handle(HttpExchange t) throws IOException {
            String query = t.getRequestURI().getRawQuery();

            ByteArrayOutputStream response = new ByteArrayOutputStream(1 << 20);
            OutputStreamWriter osw = new OutputStreamWriter(response);
            TextFormat.write004(osw,
                    registry.filteredMetricFamilySamples(parseQuery(query)));
            osw.flush();
            osw.close();
            response.flush();
            response.close();

            t.getResponseHeaders().set("Content-Type",
                    TextFormat.CONTENT_TYPE_004);
            t.getResponseHeaders().set("Content-Length",
                    String.valueOf(response.size()));
            t.sendResponseHeaders(200, response.size());
            response.writeTo(t.getResponseBody());
            t.close();
        }

    }

    protected static Set<String> parseQuery(String query) throws IOException {
        Set<String> names = new HashSet<String>();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                if (idx != -1 && URLDecoder.decode(pair.substring(0, idx), "UTF-8").equals("name[]")) {
                    names.add(URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
                }
            }
        }
        return names;
    }

    protected HttpServer server;


    /**
     * Start a HTTP server serving Prometheus metrics from the given registry.
     */
    public HTTPServer(InetSocketAddress addr, CollectorRegistry registry) throws IOException {
        server = HttpServer.create();
        server.bind(addr, 3);
        HttpHandler mHandler = new HTTPMetricHandler(registry);
        server.createContext("/", mHandler);
        server.createContext("/metrics", mHandler);
        server.setExecutor(Executors.newFixedThreadPool(5));
        server.start();
    }

    /**
     * Start a HTTP server serving the default Prometheus registry.
     */
    public HTTPServer(int port) throws IOException {
        this(new InetSocketAddress(port), CollectorRegistry.defaultRegistry);
    }

    /**
     * Start a HTTP server serving the default Prometheus registry.
     */
    public HTTPServer(String host, int port) throws IOException {
        this(new InetSocketAddress(host, port), CollectorRegistry.defaultRegistry);
    }

    /**
     * Stop the HTTP server.
     */
    public void stop() {
        server.stop(0);
    }
}


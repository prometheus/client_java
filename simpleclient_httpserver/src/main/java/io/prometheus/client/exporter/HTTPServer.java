package io.prometheus.client.exporter;

import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

public class HTTPServer {
    static class HTTPMetricHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            ByteArrayOutputStream response = dump();
            t.getResponseHeaders().set("Content-Type",
                    TextFormat.CONTENT_TYPE_004);
            t.getResponseHeaders().set("Content-Length",
                    String.valueOf(response.size()));
            t.sendResponseHeaders(200, response.size());
            response.writeTo(t.getResponseBody());
            t.close();
        }

    }

    public static ByteArrayOutputStream dump() throws java.io.IOException {
        ByteArrayOutputStream response = new ByteArrayOutputStream(1 << 20);
        OutputStreamWriter osw = new OutputStreamWriter(response);
        TextFormat.write004(osw,
                CollectorRegistry.defaultRegistry.metricFamilySamples());
        osw.flush();
        osw.close();
        response.flush();
        response.close();
        return response;
    }

    public HTTPServer(int port) throws java.io.IOException {
        HttpServer mServer = HttpServer.create();
        mServer.bind(new java.net.InetSocketAddress(port), 3);
        HttpHandler mHandler = new HTTPMetricHandler();
        mServer.createContext("/", mHandler);
        mServer.createContext("/metrics", mHandler);
        mServer.setExecutor(Executors.newSingleThreadExecutor());
        mServer.start();
    }
}

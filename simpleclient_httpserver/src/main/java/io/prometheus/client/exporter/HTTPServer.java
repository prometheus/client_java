package io.prometheus.client.exporter;

import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
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

    private static ByteArrayOutputStream dump() throws IOException {
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

    public HTTPServer(InetSocketAddress addr) throws IOException {
        HttpServer mServer = HttpServer.create();
        mServer.bind(addr, 3);
        HttpHandler mHandler = new HTTPMetricHandler();
        mServer.createContext("/", mHandler);
        mServer.createContext("/metrics", mHandler);
        mServer.setExecutor(Executors.newFixedThreadPool(5));
        mServer.start();
    }

    public HTTPServer(int port) throws IOException {
        this(new InetSocketAddress(port));
    }

    public HTTPServer(String host, int port) throws IOException {
        this(new InetSocketAddress(host, port));
    }
}


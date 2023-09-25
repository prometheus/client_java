package io.prometheus.client.exporter;

import io.prometheus.client.Gauge;

import io.prometheus.client.servlet.jakarta.exporter.MetricsServlet;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class ExampleBenchmark {

    public static void main(String[] args) throws Exception {

        Gauge gauge = Gauge.build().name("labels").help("foo").labelNames("bar").register();
        for (int i = 0; i < 10000; i++) {
            gauge.labels(UUID.randomUUID().toString()).set(Math.random());
        }

        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        context.addServlet(new ServletHolder(new MetricsServlet()), "/metrics");

        Server server = new Server(0);
        server.setHandler(context);
        server.start();
        Thread.sleep(1000);

        ServerConnector connector =  (ServerConnector) server.getConnectors()[0];
        byte[] bytes = new byte[8192];
        URL url = new URL("http", "localhost", connector.getLocalPort(), "/metrics");

        long start = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStream in = connection.getInputStream();
            try {
                while (in.read(bytes) != -1) in.available();
            } finally {
                in.close();
            }
            connection.disconnect();
        }
        System.out.println(String.format("%,3d ns", System.nanoTime() - start));

        server.stop();
        server.join();

    }

}

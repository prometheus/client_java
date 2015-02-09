package io.prometheus.client.hotspot;

import io.prometheus.client.exporter.MetricsServlet;
import io.prometheus.client.hotspot.StandardExports;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;


public class ExampleExporter {

  public static void main(String[] args) throws Exception {
    DefaultExports.initialize();
    Server server = new Server(1234);
    ServletContextHandler context = new ServletContextHandler();
    context.setContextPath("/");
    server.setHandler(context);
    context.addServlet(new ServletHolder(new MetricsServlet()), "/metrics");
    server.start();
    server.join();
  }

}

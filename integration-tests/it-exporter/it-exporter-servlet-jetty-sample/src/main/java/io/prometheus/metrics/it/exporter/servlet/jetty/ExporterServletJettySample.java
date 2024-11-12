package io.prometheus.metrics.it.exporter.servlet.jetty;

import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.core.metrics.Gauge;
import io.prometheus.metrics.core.metrics.Info;
import io.prometheus.metrics.exporter.servlet.jakarta.PrometheusMetricsServlet;
import io.prometheus.metrics.model.registry.Collector;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.Unit;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

/** Sample application using the {@link PrometheusMetricsServlet} in Jetty. */
public class ExporterServletJettySample {
  enum Mode {
    success,
    error
  }

  public static void main(String[] args) throws Exception {

    if (args.length != 2) {
      System.err.println("Usage: java -jar exporter-servlet-jetty-sample.jar <port> <mode>");
      System.err.println("Where mode is \"success\" or \"error\".");
      System.exit(1);
    }

    int port = parsePortOrExit(args[0]);
    Mode mode = parseModeOrExit(args[1]);
    run(mode, port);
  }

  private static void run(Mode mode, int port) throws Exception {
    Counter counter =
        Counter.builder()
            .name("uptime_seconds_total")
            .help("total number of seconds since this application was started")
            .unit(Unit.SECONDS)
            .register();
    counter.inc(17);

    Info info =
        Info.builder()
            .name("integration_test_info")
            .help("Info metric on this integration test")
            .labelNames("test_name")
            .register();
    info.addLabelValues("exporter-servlet-jetty-sample");

    Gauge gauge =
        Gauge.builder()
            .name("temperature_celsius")
            .help("Temperature in Celsius")
            .unit(Unit.CELSIUS)
            .labelNames("location")
            .register();
    gauge.labelValues("inside").set(23.0);
    gauge.labelValues("outside").set(27.0);

    if (mode == Mode.error) {
      Collector failingCollector =
          () -> {
            throw new RuntimeException("Simulating an error.");
          };

      PrometheusRegistry.defaultRegistry.register(failingCollector);
    }

    Server server = new Server();

    // set port
    ServerConnector connector = new ServerConnector(server);
    connector.setPort(port);
    server.setConnectors(new Connector[] {connector});

    // register servlet
    ServletContextHandler servletHandler = new ServletContextHandler("/");
    servletHandler.addServlet(PrometheusMetricsServlet.class, "/metrics");
    server.setHandler(servletHandler);

    System.out.println("Running on http://localhost:" + port + "/metrics");

    // run
    server.start();
  }

  private static int parsePortOrExit(String port) {
    try {
      return Integer.parseInt(port);
    } catch (NumberFormatException e) {
      System.err.println("\"" + port + "\": Invalid port number.");
      System.exit(1);
    }
    return 0; // this won't happen
  }

  private static Mode parseModeOrExit(String mode) {
    try {
      return Mode.valueOf(mode);
    } catch (IllegalArgumentException e) {
      System.err.println(
          "\"" + mode + "\": Invalid mode. Legal values are \"success\" and \"error\".");
      System.exit(1);
    }
    return null; // this won't happen
  }
}

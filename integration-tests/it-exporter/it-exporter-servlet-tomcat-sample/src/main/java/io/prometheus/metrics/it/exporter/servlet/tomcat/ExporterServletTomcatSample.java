package io.prometheus.metrics.it.exporter.servlet.tomcat;

import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.core.metrics.Gauge;
import io.prometheus.metrics.core.metrics.Info;
import io.prometheus.metrics.exporter.servlet.jakarta.PrometheusMetricsServlet;
import io.prometheus.metrics.model.registry.Collector;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.Unit;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Sample application using the {@link PrometheusMetricsServlet} in Tomcat.
 */
public class ExporterServletTomcatSample {
    enum Mode {
        success,
        error
    }

    public static void main(String[] args) throws LifecycleException, IOException {

        if (args.length != 2) {
            System.err.println("Usage: java -jar exporter-servlet-tomcat-sample.jar <port> <mode>");
            System.err.println("Where mode is \"success\" or \"error\".");
            System.exit(1);
        }

        int port = parsePortOrExit(args[0]);
        Mode mode = parseModeOrExit(args[1]);

        Counter counter = Counter.builder()
                .name("uptime_seconds_total")
                .help("total number of seconds since this application was started")
                .unit(Unit.SECONDS)
                .register();
        counter.inc(17);

        Info info = Info.builder()
                .name("integration_test_info")
                .help("Info metric on this integration test")
                .labelNames("test_name")
                .register();
        info.addLabelValues("exporter-servlet-tomcat-sample");

        Gauge gauge = Gauge.builder()
                .name("temperature_celsius")
                .help("Temperature in Celsius")
                .unit(Unit.CELSIUS)
                .labelNames("location")
                .register();
        gauge.labelValues("inside").set(23.0);
        gauge.labelValues("outside").set(27.0);

        if (mode == Mode.error) {
            Collector failingCollector = () -> {
                throw new RuntimeException("Simulating an error.");
            };

            PrometheusRegistry.defaultRegistry.register(failingCollector);
        }

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);

        Path tmpDir = Files.createTempDirectory("exporter-servlet-tomcat-sample-");
        tomcat.setBaseDir(tmpDir.toFile().getAbsolutePath());
        Context ctx = tomcat.addContext("", new File(".").getAbsolutePath());
        Tomcat.addServlet(ctx, "metrics", new PrometheusMetricsServlet());
        ctx.addServletMappingDecoded("/metrics", "metrics");

        tomcat.getConnector();
        tomcat.start();
        tomcat.getServer().await();
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
            System.err.println("\"" + mode + "\": Invalid mode. Legal values are \"success\" and \"error\".");
            System.exit(1);
        }
        return null; // this won't happen
    }
}

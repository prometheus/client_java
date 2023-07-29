package io.prometheus.metrics.it.exporter.httpserver;

import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.core.metrics.Gauge;
import io.prometheus.metrics.core.metrics.Info;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.model.registry.Collector;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.Unit;

import java.io.IOException;

public class HTTPServerSample {

    enum Mode {
        success,
        error
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        if (args.length != 2) {
            System.err.println("Usage: java -jar exporter-httpserver-sample.jar <port> <mode>");
            System.err.println("Where mode is \"success\" or \"error\".");
            System.exit(1);
        }

        int port = parsePortOrExit(args[0]);
        Mode mode = parseModeOrExit(args[1]);

        Counter counter = Counter.newBuilder()
                .withName("uptime_seconds_total")
                .withHelp("total number of seconds since this application was started")
                .withUnit(Unit.SECONDS)
                .register();
        counter.inc(17);

        Info info = Info.newBuilder()
                .withName("integration_test_info")
                .withHelp("Info metric on this integration test")
                .withLabelNames("test_name")
                .register();
        info.infoLabelValues("exporter-httpserver-sample");

        Gauge gauge = Gauge.newBuilder()
                .withName("temperature_celsius")
                .withHelp("Temperature in Celsius")
                .withUnit(Unit.CELSIUS)
                .withLabelNames("location")
                .register();
        gauge.withLabelValues("inside").set(23.0);
        gauge.withLabelValues("outside").set(27.0);

        if (mode == Mode.error) {
            Collector failingCollector = new Collector() {

                @Override
                public String getPrometheusName() {
                    return null;
                }

                @Override
                public MetricSnapshot collect() {
                    throw new RuntimeException("Simulating an error.");
                }
            };

            PrometheusRegistry.defaultRegistry.register(failingCollector);
        }

        HTTPServer server = HTTPServer.newBuilder()
                .withPort(port)
                .buildAndStart();

        System.out.println("HTTPServer listening on port http://localhost:" + server.getPort() + "/metrics");
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

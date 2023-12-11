package io.prometheus.metrics.examples.otel_exemplars.greeting;

import io.prometheus.metrics.core.metrics.Histogram;
import io.prometheus.metrics.model.snapshots.Unit;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Random;

import static io.prometheus.metrics.model.snapshots.Unit.nanosToSeconds;

/**
 * Hello World REST servlet, with an example counter and an example histogram.
 */
public class GreetingServlet extends HttpServlet {

    private final Random random = new Random(0);

    private final Histogram histogram;

    public GreetingServlet() {
        histogram = Histogram.builder()
            .name("request_duration_seconds")
            .help("request duration in seconds")
            .unit(Unit.SECONDS)
            .labelNames("http_status")
            .register();
        histogram.initLabelValues("200");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long start = System.nanoTime();
        try {
            Thread.sleep((long) (Math.abs((random.nextGaussian() + 1.0) * 100.0)));
            resp.setStatus(200);
            resp.setContentType("text/plain");
            resp.getWriter().println("Hello, World!");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            histogram.labelValues("200").observe(nanosToSeconds(System.nanoTime() - start));
        }
    }
}

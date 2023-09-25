package io.prometheus.metrics.examples.tomcat_servlet;

import io.prometheus.metrics.core.metrics.Counter;
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
public class HelloWorldServlet extends HttpServlet {

    private final Random random = new Random(0);

    // Note: The requests_total counter is not a great example, because the
    // request_duration_seconds histogram below also has a count with the number of requests.
    private final Counter counter = Counter.builder()
            .name("requests_total")
            .help("total number of requests")
            .labelNames("http_status")
            .register();

    private final Histogram histogram = Histogram.builder()
            .name("request_duration_seconds")
            .help("request duration in seconds")
            .unit(Unit.SECONDS)
            .labelNames("http_status")
            .register();

    public HelloWorldServlet() {
        counter.initLabelValues("200");
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
            counter.labelValues("200").inc();
            histogram.labelValues("200").observe(nanosToSeconds(System.nanoTime() - start));
        }
    }
}

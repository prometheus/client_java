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

public class HelloWorldServlet extends HttpServlet {

    private final Random random = new Random(0);

    private final Counter counter = Counter.newBuilder()
            .withName("requests_total")
            .withHelp("total number of requests")
            .withLabelNames("http_status")
            .register();

    private final Histogram histogram = Histogram.newBuilder()
            .withName("request_duration_seconds")
            .withHelp("request duration in seconds")
            .withUnit(Unit.SECONDS)
            .withLabelNames("http_status")
            .register();

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
            counter.withLabelValues("200").inc();
            histogram.withLabelValues("200").observe(nanosToSeconds(System.nanoTime() - start));
        }
    }
}

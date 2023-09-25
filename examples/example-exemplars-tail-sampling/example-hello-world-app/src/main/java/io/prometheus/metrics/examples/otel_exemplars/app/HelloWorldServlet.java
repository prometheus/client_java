package io.prometheus.metrics.examples.otel_exemplars.app;

import io.prometheus.metrics.core.metrics.Histogram;
import io.prometheus.metrics.model.snapshots.Unit;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Random;

import static io.prometheus.metrics.model.snapshots.Unit.nanosToSeconds;
import static java.net.http.HttpResponse.BodyHandlers.ofString;

/**
 * Hello World REST servlet, with an example counter and an example histogram.
 */
public class HelloWorldServlet extends HttpServlet {

    private final Random random = new Random(0);

    private final Histogram histogram;

    public HelloWorldServlet() {
        histogram = Histogram.builder()
                .name("request_duration_seconds")
                .help("request duration in seconds")
                .unit(Unit.SECONDS)
                .labelNames("http_status")
                .register();
        histogram.initLabelValues("200");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        long start = System.nanoTime();
        try {
            Thread.sleep((long) (Math.abs((random.nextGaussian() + 1.0) * 100.0)));
            String greeting = executeGreetingServiceRequest();
            resp.setStatus(200);
            resp.setContentType("text/plain");
            resp.getWriter().print(greeting);
        } catch (Exception e) {
            throw new ServletException(e);
        } finally {
            histogram.labelValues("200").observe(nanosToSeconds(System.nanoTime() - start));
        }
    }

    private String executeGreetingServiceRequest() throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(new URI("http://localhost:8081/"))
                .build();
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> response = httpClient.send(request, ofString());
        return response.body();
    }
}

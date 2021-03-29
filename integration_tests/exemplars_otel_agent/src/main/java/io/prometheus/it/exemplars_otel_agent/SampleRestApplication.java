package io.prometheus.it.exemplars_otel_agent;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.prometheus.client.Summary;
import io.prometheus.client.exporter.MetricsServlet;
import io.prometheus.client.hotspot.DefaultExports;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@SpringBootApplication
@RestController
public class SampleRestApplication {

  private double durationOuterMs = 0.5;
  private double durationInnerMs = 0.3;

  private final OkHttpClient client = new OkHttpClient();

  private final Counter requestCounter = Counter.build()
      .name("requests_total")
      .help("Total number of requests.")
      .labelNames("path")
      .register();
  private final Gauge lastRequestTimestamp = Gauge.build()
      .name("last_request_timestamp")
      .help("unix time of the last request")
      .labelNames("path")
      .register();
  private final Histogram requestDurationHistogram = Histogram.build()
      .name("request_duration_histogram")
      .help("Request duration in seconds")
      .labelNames("path")
      .buckets(0.001, 0.002, 0.003, 0.004, 0.005, 0.006, 0.007, 0.008, 0.009)
      .register();
  private final Summary requestDurationSummary = Summary.build()
      .name("request_duration_summary")
      .help("Request duration in seconds")
      .labelNames("path")
      .quantile(0.75, 0.01)
      .quantile(0.85, 0.01)
      .register();

  public static void main(String[] args) {
    DefaultExports.initialize();
    SpringApplication.run(SampleRestApplication.class, args);
  }

  @GetMapping("/hello")
  public String hello() throws IOException {
    String path = "/hello";
    requestCounter.labels(path).inc();
    lastRequestTimestamp.labels(path).setToCurrentTime();
    requestDurationHistogram.labels(path).observe(durationOuterMs / 1000.0);
    requestDurationSummary.labels(path).observe(durationOuterMs / 1000.0);
    durationOuterMs += 1;
    if (durationOuterMs > 10) {
      durationOuterMs = 0.5;
    }
    Request request = new Request.Builder()
        .url("http://localhost:8080/god-of-fire")
        .build();
    try (Response response = client.newCall(request).execute()) {
      return "Hello, " + response.body().string() + "!\n";
    }
  }

  @GetMapping("/god-of-fire")
  public String godOfFire() {
    String path = "/god-of-fire";
    requestCounter.labels(path).inc();
    lastRequestTimestamp.labels(path).setToCurrentTime();
    requestDurationHistogram.labels(path).observe(durationInnerMs / 1000.0);
    requestDurationSummary.labels(path).observe(durationInnerMs / 1000.0);
    durationInnerMs += 1;
    if (durationInnerMs > 10) {
      durationInnerMs = 0.3;
    }
    return "Prometheus";
  }

  @Bean
  public ServletRegistrationBean<MetricsServlet> metricsServlet() {
    ServletRegistrationBean<MetricsServlet> bean = new ServletRegistrationBean<>(new MetricsServlet(), "/metrics");
    bean.setLoadOnStartup(1);
    return bean;
  }
}

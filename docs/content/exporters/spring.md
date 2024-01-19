---
title: Spring
weight: 5
---

Alternative: Use Spring's Built-in Metrics Library
--------------------------------------------------

[Spring Boot](https://spring.io/projects/spring-boot) has a built-in metric library named [Micrometer](https://micrometer.io/), which supports Prometheus exposition format and can be set up in three simple steps:

1. Add the `org.springframework.boot:spring-boot-starter-actuator` dependency.
2. Add the `io.micrometer:micrometer-registry-prometheus` as a _runtime_ dependency.
3. Enable the Prometheus endpoint by adding the line `management.endpoints.web.exposure.include=prometheus` to `application.properties`.

Note that Spring's default Prometheus endpoint is `/actuator/prometheus`, not `/metrics`.

In most cases the built-in Spring metrics library will work for you and you don't need the Prometheus Java library in Spring applications.

Use the Prometheus Metrics Library in Spring
--------------------------------------------

However, you may have your reasons why you want to use the Prometheus metrics library in Spring anyway. Maybe you want full support for all Prometheus metric types, or you want to use the new Prometheus native histograms.

The easiest way to use the Prometheus metrics library in Spring is to configure the [PrometheusMetricsServlet](/client_java/api/io/prometheus/metrics/exporter/servlet/jakarta/PrometheusMetricsServlet.html) to expose metrics.

Dependencies:

* `prometheus-metrics-core`: The core metrics library.
* `prometheus-metrics-exporter-servlet-jakarta`: For providing the `/metrics` endpoint.
* `prometheus-metrics-instrumentation-jvm`: Optional - JVM metrics

The following is the complete source code of a Spring Boot REST service using the Prometheus metrics library:

```java
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.exporter.servlet.jakarta.PrometheusMetricsServlet;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class DemoApplication {

    private static final Counter requestCount = Counter.builder()
            .name("requests_total")
            .register();

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
        JvmMetrics.builder().register();
    }

    @GetMapping("/")
    public String sayHello() throws InterruptedException {
        requestCount.inc();
        return "Hello, World!\n";
    }

    @Bean
    public ServletRegistrationBean<PrometheusMetricsServlet> createPrometheusMetricsEndpoint() {
        return new ServletRegistrationBean<>(new PrometheusMetricsServlet(), "/metrics/*");
    }
}
```

The important part are the last three lines: They configure the [PrometheusMetricsServlet](/client_java/api/io/prometheus/metrics/exporter/servlet/jakarta/PrometheusMetricsServlet.html) to expose metrics on `/metrics`:

```java
@Bean
public ServletRegistrationBean<PrometheusMetricsServlet> createPrometheusMetricsEndpoint() {
    return new ServletRegistrationBean<>(new PrometheusMetricsServlet(), "/metrics/*");
}
```

The example provides a _Hello, world!_ endpoint on [http://localhost:8080](http://localhost:8080), and Prometheus metrics on [http://localhost:8080/metrics](http://localhost:8080/metrics).

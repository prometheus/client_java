package io.prometheus.metrics.examples.otel_exemplars.greeting;

import io.prometheus.metrics.exporter.servlet.jakarta.PrometheusMetricsServlet;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

import java.io.File;

/**
 * Simple example using embedded Tomcat and the {@link PrometheusMetricsServlet}.
 */
public class Main {

    public static void main(String[] args) throws LifecycleException {

        JvmMetrics.builder().register();

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8081);

        Context ctx = tomcat.addContext("", new File(".").getAbsolutePath());

        Tomcat.addServlet(ctx, "hello", new GreetingServlet());
        ctx.addServletMappingDecoded("/*", "hello");

        Tomcat.addServlet(ctx, "metrics", new PrometheusMetricsServlet());
        ctx.addServletMappingDecoded("/metrics", "metrics");

        tomcat.getConnector();
        tomcat.start();
        tomcat.getServer().await();
    }
}

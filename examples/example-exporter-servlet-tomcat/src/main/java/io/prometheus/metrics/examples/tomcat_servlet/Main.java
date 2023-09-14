package io.prometheus.metrics.examples.tomcat_servlet;

import io.prometheus.metrics.exporter.servlet.jakarta.PrometheusMetricsServlet;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Simple example using embedded Tomcat and the {@link PrometheusMetricsServlet}.
 */
public class Main {

    public static void main(String[] args) throws LifecycleException, IOException {

        JvmMetrics.builder().register();

        Tomcat tomcat = new Tomcat();
        Path tmpDir = Files.createTempDirectory("prometheus-tomcat-servlet-example-");
        tomcat.setBaseDir(tmpDir.toFile().getAbsolutePath());

        Context ctx = tomcat.addContext("", new File(".").getAbsolutePath());

        Tomcat.addServlet(ctx, "hello", new HelloWorldServlet());
        ctx.addServletMappingDecoded("/*", "hello");

        Tomcat.addServlet(ctx, "metrics", new PrometheusMetricsServlet());
        ctx.addServletMappingDecoded("/metrics", "metrics");

        tomcat.getConnector();
        tomcat.start();
        tomcat.getServer().await();
    }
}

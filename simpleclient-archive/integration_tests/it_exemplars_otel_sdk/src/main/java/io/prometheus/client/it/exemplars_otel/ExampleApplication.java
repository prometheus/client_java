package io.prometheus.client.it.exemplars_otel;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.prometheus.client.Counter;
import io.prometheus.client.exporter.HTTPServer;

import java.io.IOException;

/**
 * Example application using the OpenTelemetry SDK.
 */
public class ExampleApplication {

  public static void main(String[] args) throws IOException, InterruptedException {
    Counter counter = Counter.build()
        .name("test")
        .help("help")
        .register();

    // The following code compiles with OpenTelemetry versions 0.13.0 and higher.
    // Starting with 0.16.0 you will see exemplars. With 0.15.0 or lower the code will run but you won't see exemplars.
    System.out.println(Tracer.class.getProtectionDomain().getCodeSource().getLocation());
    Tracer tracer = SdkTracerProvider.builder().build().get(null);
    Span span = tracer.spanBuilder("my span").startSpan();
    span.makeCurrent();
    counter.inc(1);
    span.end();

    // Examples with older OpenTelemetry versions used for manual testing:

    /*
    // OpenTelemetry versions 0.10.0 - 0.12.0
    System.out.println(Tracer.class.getProtectionDomain().getCodeSource().getLocation());
    Tracer tracer = OpenTelemetrySdk.get().getTracer("test");
    Span span = tracer.spanBuilder("my span").startSpan();
    span.makeCurrent();
    counter.inc(1);
    span.end();
     */

    /*
    // OpenTelemetry versions 0.4.0 - 0.9.1
    System.out.println(TracerSdkProvider.class.getProtectionDomain().getCodeSource().getLocation());
    Tracer tracer = TracerSdkProvider.builder().build().get("test");
    Span span = tracer.spanBuilder("my span").startSpan();
    counter.inc(1);
    span.end();
     */

    /*
    // OpenTelemetry version 0.3.0
    System.out.println(TracerSdkProvider.class.getProtectionDomain().getCodeSource().getLocation());
    TracerSdk tracer = TracerSdkProvider.builder().build().get("test");
    Span span = tracer.spanBuilder("my span").startSpan();
    counter.inc(1);
    span.end();
     */

    /*
    // OpenTelemetry version 0.2.0
    System.out.println(TracerSdkFactory.class.getProtectionDomain().getCodeSource().getLocation());
    TracerSdk tracer = TracerSdkFactory.create().get("test");
    Span span = tracer.spanBuilder("my span").startSpan();
    counter.inc(1);
    span.end();
     */

    new HTTPServer(9000);
    Thread.currentThread().join(); // sleep forever
  }
}

package io.prometheus.client.it.exemplars_micrometer_tracing;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.test.simple.SimpleSpan;
import io.micrometer.tracing.test.simple.SimpleTracer;
import io.prometheus.client.Counter;
import io.prometheus.client.exemplars.DefaultExemplarSampler;
import io.prometheus.client.exemplars.ExemplarConfig;
import io.prometheus.client.exemplars.ExemplarSampler;
import io.prometheus.client.exemplars.tracer.micrometer.tracing.MicrometerTracingSpanContextSupplier;
import io.prometheus.client.exporter.HTTPServer;

import java.io.IOException;

/**
 * Example application using Micrometer Tracing.
 */
public class ExampleApplication {

  public static void main(String[] args) throws IOException, InterruptedException {
    SimpleTracer tracer = new SimpleTracer();
    ExemplarSampler exemplarSampler = new DefaultExemplarSampler(new MicrometerTracingSpanContextSupplier(tracer));
    ExemplarConfig.setCounterExemplarSampler(exemplarSampler);
    ExemplarConfig.setHistogramExemplarSampler(exemplarSampler);
    ExemplarConfig.enableExemplars();

    Counter counter = Counter.build()
        .name("test")
        .help("help")
        .register();

    Span span = nextSpan(tracer);
    try (Tracer.SpanInScope ws = tracer.withSpan(span.start())) {
      counter.inc(1);
    }
    finally {
      span.end();
    }

    new HTTPServer(9000);
    Thread.currentThread().join(); // sleep forever
  }

  private static Span nextSpan(SimpleTracer tracer) {
    SimpleSpan span = tracer.nextSpan().name("testSpan");
    span.context().setSampled(true);
    span.context().setTraceId("45e09ee1c39e1f8f");
    span.context().setSpanId("22f69a0e2c0ab635");

    return span;
  }
}

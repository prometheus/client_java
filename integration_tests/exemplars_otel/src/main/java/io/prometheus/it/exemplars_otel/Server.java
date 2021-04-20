package io.prometheus.it.exemplars_otel;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.prometheus.client.Counter;
import io.prometheus.client.exporter.HTTPServer;

import java.io.IOException;

public class Server {

  public static void main(String[] args) throws IOException, InterruptedException {
    new HTTPServer(9000);
    Counter counter = Counter.build()
        .name("test")
        .help("help")
        .register();

    Tracer tracer = SdkTracerProvider.builder().build().get(null);
    Span span = tracer.spanBuilder("my span").startSpan();
    span.makeCurrent();
    counter.inc(1);
    span.end();
    Thread.currentThread().join(); // sleep forever
  }
}

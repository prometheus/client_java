package io.prometheus.client.exemplars.tracer;

import io.prometheus.client.exemplars.tracer.common.SpanContextSupplier;
import io.prometheus.client.exemplars.tracer.otel.OpenTelemetrySpanContextSupplier;
import io.prometheus.client.exemplars.tracer.otel_agent.OpenTelemetryAgentSpanContextSupplier;

/**
 * Entrypoint for finding the corresponding {@link SpanContextSupplier} if a distributed tracing library is available.
 */
public class Tracer {

  /**
   * Find the {@link SpanContextSupplier} if a corresponding tracing library is available.
   * <p/>
   * If you want to use your own {@link SpanContextSupplier} implementation, use
   * {@code ExemplarConfig.setCounterExemplarSampler()} and {@code ExemplarConfig.setHistogramExemplarSampler()}
   * to override the default.
   *
   * @return a {@link SpanContextSupplier} if available, or {@code null}.
   */
  public static SpanContextSupplier findSpanContextSupplier() {
    try {
      if (OpenTelemetrySpanContextSupplier.isAvailable()) {
        return new OpenTelemetrySpanContextSupplier();
      }
      if (OpenTelemetryAgentSpanContextSupplier.isAvailable()) {
        return new OpenTelemetryAgentSpanContextSupplier();
      }
    } catch (UnsupportedClassVersionError ignored) {
      // OpenTelemetry requires Java 8, but client_java might run on Java 6.
    }
    return null;
  }
}

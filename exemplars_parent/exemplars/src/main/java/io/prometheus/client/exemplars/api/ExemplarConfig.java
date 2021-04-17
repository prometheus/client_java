package io.prometheus.client.exemplars.api;

import io.prometheus.client.exemplars.impl.DefaultExemplarSampler;
import io.prometheus.client.exemplars.impl.NoopExemplarSampler;
import io.prometheus.client.exemplars.tracer.common.SpanContext;
import io.prometheus.client.exemplars.tracer.otel.OpenTelemetrySpanContext;
import io.prometheus.client.exemplars.tracer.otel_agent.OpenTelemetryAgentSpanContext;

/**
 * Static configuration class for Exemplar behavior.
 */
public class ExemplarConfig {

  private static final ExemplarConfig instance = new ExemplarConfig();

  private final ExemplarSampler noopExemplarSampler;
  private final ExemplarSampler defaultExemplarSampler;

  private volatile HistogramExemplarSampler histogramExemplarSampler;
  private volatile CounterExemplarSampler counterExemplarSampler;

  private ExemplarConfig() {
    noopExemplarSampler = new NoopExemplarSampler();
    SpanContext spanContext = findSpanContextSupplier();
    if (spanContext != null) {
      defaultExemplarSampler = new DefaultExemplarSampler(spanContext);
    } else {
      defaultExemplarSampler = noopExemplarSampler;
    }
    counterExemplarSampler = defaultExemplarSampler;
    histogramExemplarSampler = defaultExemplarSampler;
  }

  // Counter

  public static void setCounterExemplarSampler(CounterExemplarSampler counterExemplarSampler) {
    if (counterExemplarSampler == null) {
      throw new NullPointerException();
    }
    instance.counterExemplarSampler = counterExemplarSampler;
  }

  // Histogram

  public static void setHistogramExemplarSampler(HistogramExemplarSampler histogramExemplarSampler) {
    if (histogramExemplarSampler == null) {
      throw new NullPointerException();
    }
    instance.histogramExemplarSampler = histogramExemplarSampler;
  }

  // Disable all of the above

  public static void disableExemplars() {
    instance.counterExemplarSampler = instance.noopExemplarSampler;
    instance.histogramExemplarSampler = instance.noopExemplarSampler;
  }

  // Methods for internal use

  public static CounterExemplarSampler getCounterExemplarSampler() {
    return instance.counterExemplarSampler;
  }

  public static HistogramExemplarSampler getHistogramExemplarSampler() {
    return instance.histogramExemplarSampler;
  }

  public static ExemplarSampler getNoopExemplarSampler() {
    return instance.noopExemplarSampler;
  }

  public static ExemplarSampler getDefaultExemplarSampler() {
    return instance.defaultExemplarSampler;
  }

  private static SpanContext findSpanContextSupplier() {
    try {
      if (OpenTelemetrySpanContext.isAvailable()) {
        return new OpenTelemetrySpanContext();
      }
      if (OpenTelemetryAgentSpanContext.isAvailable()) {
        return new OpenTelemetryAgentSpanContext();
      }
    } catch (UnsupportedClassVersionError ignored) {
      // OpenTelemetry requires Java 8, but client_java might run on Java 6.
    }
    return null;
  }
}

package io.prometheus.client.exemplars.api;

import io.prometheus.client.exemplars.impl.DefaultExemplarSampler;
import io.prometheus.client.exemplars.impl.NoopExemplarSampler;
import io.prometheus.client.exemplars.tracer.common.NoopSpanContext;
import io.prometheus.client.exemplars.tracer.common.SpanContext;
import io.prometheus.client.exemplars.tracer.otel.OpenTelemetrySpanContext;
import io.prometheus.client.exemplars.tracer.otel_agent.OpenTelemetryAgentSpanContext;

/**
 * Static configuration class for Exemplar behavior.
 */
public class ExemplarConfig {

  private static final ExemplarConfig instance = new ExemplarConfig();

  private final NoopExemplarSampler noopExemplarSampler = new NoopExemplarSampler();
  private volatile HistogramExemplarSampler defaultHistogramExemplarSampler;
  private volatile SummaryExemplarSampler defaultSummaryExemplarSampler;
  private volatile GaugeExemplarSampler defaultGaugeExemplarSampler;
  private volatile CounterExemplarSampler defaultCounterExemplarSampler;

  private ExemplarConfig() {
    DefaultExemplarSampler defaultExemplars = new DefaultExemplarSampler(findTraceIdSupplier());
    defaultCounterExemplarSampler = defaultExemplars;
    defaultGaugeExemplarSampler = defaultExemplars;
    defaultHistogramExemplarSampler = defaultExemplars;
    defaultSummaryExemplarSampler = defaultExemplars;
  }

  // Counter

  public static void setDefaultCounterExemplarSampler(CounterExemplarSampler defaultCounterExemplarSampler) {
    if (defaultCounterExemplarSampler == null) {
      throw new NullPointerException();
    }
    instance.defaultCounterExemplarSampler = defaultCounterExemplarSampler;
  }

  // Gauge

  public static void setDefaultGaugeExemplarSampler(GaugeExemplarSampler defaultGaugeExemplarSampler) {
    if (defaultGaugeExemplarSampler == null) {
      throw new NullPointerException();
    }
    instance.defaultGaugeExemplarSampler = defaultGaugeExemplarSampler;
  }

  // Histogram

  public static void setDefaultHistogramExemplarSampler(HistogramExemplarSampler defaultHistogramExemplarSampler) {
    if (defaultHistogramExemplarSampler == null) {
      throw new NullPointerException();
    }
    instance.defaultHistogramExemplarSampler = defaultHistogramExemplarSampler;
  }

  // Summary

  public static void setDefaultSummaryExemplarSampler(SummaryExemplarSampler defaultSummaryExemplarSampler) {
    if (defaultSummaryExemplarSampler == null) {
      throw new NullPointerException();
    }
    instance.defaultSummaryExemplarSampler = defaultSummaryExemplarSampler;
  }

  // Disable all of the above

  public static void disableByDefault() {
    instance.defaultCounterExemplarSampler = instance.noopExemplarSampler;
    instance.defaultGaugeExemplarSampler = instance.noopExemplarSampler;
    instance.defaultHistogramExemplarSampler = instance.noopExemplarSampler;
    instance.defaultSummaryExemplarSampler = instance.noopExemplarSampler;
  }

  // Methods for internal use

  public static CounterExemplarSampler getDefaultCounterExemplarSampler() {
    return instance.defaultCounterExemplarSampler;
  }

  public static GaugeExemplarSampler getDefaultGaugeExemplarSampler() {
    return instance.defaultGaugeExemplarSampler;
  }

  public static HistogramExemplarSampler getDefaultHistogramExemplarSampler() {
    return instance.defaultHistogramExemplarSampler;
  }

  public static SummaryExemplarSampler getDefaultSummaryExemplarSampler() {
    return instance.defaultSummaryExemplarSampler;
  }

  public static NoopExemplarSampler getNoopExemplarSampler() {
    return instance.noopExemplarSampler;
  }

  private static SpanContext findTraceIdSupplier() {
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
    return new NoopSpanContext();
  }
}

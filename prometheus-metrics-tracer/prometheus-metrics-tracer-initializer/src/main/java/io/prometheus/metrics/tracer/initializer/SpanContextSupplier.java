package io.prometheus.metrics.tracer.initializer;

import io.prometheus.metrics.tracer.common.SpanContext;
import io.prometheus.metrics.tracer.otel.OpenTelemetrySpanContext;
import io.prometheus.metrics.tracer.otel_agent.OpenTelemetryAgentSpanContext;

import java.util.concurrent.atomic.AtomicReference;

public class SpanContextSupplier {

    private static final AtomicReference<SpanContext> spanContextRef = new AtomicReference<SpanContext>();

    public static void setSpanContext(SpanContext spanContext) {
        spanContextRef.set(spanContext);
    }

    public static boolean hasSpanContext() {
        return getSpanContext() != null;
    }

    public static SpanContext getSpanContext() {
        return spanContextRef.get();
    }

    static {
        try {
            if (OpenTelemetrySpanContext.isAvailable()) {
                spanContextRef.set(new OpenTelemetrySpanContext());
            }
        } catch (NoClassDefFoundError ignored) {
            // tracer_otel dependency not found
        } catch (UnsupportedClassVersionError ignored) {
            // OpenTelemetry requires Java 8, but client_java might run on Java 6.
        }
        try {
            if (OpenTelemetryAgentSpanContext.isAvailable()) {
                spanContextRef.set(new OpenTelemetryAgentSpanContext());
            }
        } catch (NoClassDefFoundError ignored) {
            // tracer_otel_agent dependency not found
        } catch (UnsupportedClassVersionError ignored) {
            // OpenTelemetry requires Java 8, but client_java might run on Java 6.
        }
    }
}

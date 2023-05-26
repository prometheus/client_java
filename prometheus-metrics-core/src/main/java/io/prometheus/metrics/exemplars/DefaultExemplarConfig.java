package io.prometheus.metrics.exemplars;

import io.prometheus.client.exemplars.tracer.common.SpanContextSupplier;
import io.prometheus.client.exemplars.tracer.otel.OpenTelemetrySpanContextSupplier;
import io.prometheus.client.exemplars.tracer.otel_agent.OpenTelemetryAgentSpanContextSupplier;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;

public class DefaultExemplarConfig {

    private static volatile boolean exemplarsEnabledByDefault = true;
    private static volatile Object spanContextSupplier = findSpanContextSupplier();
    private static volatile long minAgeMillis = SECONDS.toMillis(7);
    private static volatile long maxAgeMillis = SECONDS.toMillis(70);
    private static volatile long sampleIntervalMillis = 20;
    private static final int numberOfExemplars = 4;

    public static void disableExemplarsByDefault() {
        exemplarsEnabledByDefault = false;
    }

    public static void enableExemplarsByDefault() {
        exemplarsEnabledByDefault = true;
    }

    public static boolean isEnabledByDefault() {
        // TODO: Allow disabling via system property or environment variable
        return exemplarsEnabledByDefault;
    }

    public static boolean hasSpanContextSupplier() {
        return spanContextSupplier != null;
    }

    // Avoid SpanContextSupplier in the method signature so that we can handle the NoClassDefFoundError
    // even if the user excluded simpleclient_tracer_common from the classpath.
    public static void setSpanContextSupplier(Object spanContextSupplier) {
        if (!(spanContextSupplier instanceof SpanContextSupplier)) {
            // This will throw a NullPointerException if spanContextSupplier is null.
            throw new IllegalArgumentException(spanContextSupplier.getClass().getSimpleName() + " does not implement SpanContextSupplier");
        }
        DefaultExemplarConfig.spanContextSupplier = spanContextSupplier;
    }

    /**
     * Will <i>not</i> return {@code null} after {@link #hasSpanContextSupplier()} returned {@code true}.
     */
    public static Object getSpanContextSupplier() {
        return spanContextSupplier;
    }

    public static void setMinAge(long minAge, TimeUnit unit) {
       DefaultExemplarConfig.minAgeMillis = unit.toMillis(minAge);
    }

    public static long getMinAgeMillis() {
        return minAgeMillis;
    }

    public static void setMaxAge(long maxAge, TimeUnit unit) {
        DefaultExemplarConfig.maxAgeMillis = unit.toMillis(maxAge);
    }

    public static long getMaxAgeMillis() {
        return maxAgeMillis;
    }

    public static void setSampleInterval(long sampleInterval, TimeUnit unit) {
        DefaultExemplarConfig.sampleIntervalMillis = unit.toMillis(sampleInterval);
    }

    public static long getSampleIntervalMillis() {
        return sampleIntervalMillis;
    }

    public static int getNumberOfExemplars() {
        return numberOfExemplars;
    }

    private static Object findSpanContextSupplier() {
        try {
            if (OpenTelemetrySpanContextSupplier.isAvailable()) {
                return new OpenTelemetrySpanContextSupplier();
            }
        } catch (NoClassDefFoundError ignored) {
            // tracer_otel dependency not found
        } catch (UnsupportedClassVersionError ignored) {
            // OpenTelemetry requires Java 8, but client_java might run on Java 6.
        }
        try {
            if (OpenTelemetryAgentSpanContextSupplier.isAvailable()) {
                return new OpenTelemetryAgentSpanContextSupplier();
            }
        } catch (NoClassDefFoundError ignored) {
            // tracer_otel_agent dependency not found
        } catch (UnsupportedClassVersionError ignored) {
            // OpenTelemetry requires Java 8, but client_java might run on Java 6.
        }
        return null;
    }
}

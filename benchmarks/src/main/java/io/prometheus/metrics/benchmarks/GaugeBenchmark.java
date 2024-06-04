package io.prometheus.metrics.benchmarks;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.*;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.prometheus.metrics.core.datapoints.GaugeDataPoint;
import io.prometheus.metrics.core.metrics.Gauge;
import org.openjdk.jmh.annotations.*;

/**
 * Results on a machine with dedicated 8 vCPU cores:
 * <pre>
 * Benchmark                                                        Mode  Cnt      Score      Error  Units
 * i.p.metrics.benchmarks.GaugeBenchmark.openTelemetryDoubleGauge  thrpt   25   1673.376 ±  129.696  ops/s
 * i.p.metrics.benchmarks.GaugeBenchmark.openTelemetryLongGauge    thrpt   25   1638.250 ±   48.570  ops/s
 * i.p.metrics.benchmarks.GaugeBenchmark.prometheus                thrpt   25  56532.523 ± 1048.815  ops/s
 * i.p.metrics.benchmarks.GaugeBenchmark.simpleclient              thrpt   25  16201.762 ±  140.938  ops/s
 * </pre>
 */

public class GaugeBenchmark {

    @State(Scope.Benchmark)
    public static class PrometheusGauge {

        final Gauge noLabels;
        final GaugeDataPoint dataPoint;

        public PrometheusGauge() {
            noLabels = Gauge.builder()
                    .name("test")
                    .help("help")
                    .build();

            Gauge labels = Gauge.builder()
                    .name("test")
                    .help("help")
                    .labelNames("path", "status")
                    .build();
            this.dataPoint = labels.labelValues("/", "200");
        }
    }

    @State(Scope.Benchmark)
    public static class SimpleclientGauge {

        final io.prometheus.client.Gauge noLabels;
        final io.prometheus.client.Gauge.Child dataPoint;

        public SimpleclientGauge() {
            noLabels = io.prometheus.client.Gauge.build()
                    .name("name")
                    .help("help")
                    .create();

            io.prometheus.client.Gauge gauge = io.prometheus.client.Gauge.build()
                    .name("name")
                    .help("help")
                    .labelNames("path", "status")
                    .create();

            this.dataPoint = gauge.labels("/", "200");
        }
    }

    @State(Scope.Benchmark)
    public static class OpenTelemetryGauge {

        final LongGauge longGauge;
        final DoubleGauge doubleGauge;
        final Attributes attributes;

        public OpenTelemetryGauge() {

            SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder()
                    .registerMetricReader(InMemoryMetricReader.create())
                    .setResource(Resource.getDefault())
                    .build();
            OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
                    .setMeterProvider(sdkMeterProvider)
                    .build();
            Meter meter = openTelemetry
                    .meterBuilder("instrumentation-library-name")
                    .setInstrumentationVersion("1.0.0")
                    .build();
            this.longGauge = meter
                    .gaugeBuilder("test1")
                    .setDescription("test")
                    .ofLongs()
                    .build();
            this.doubleGauge = meter
                    .gaugeBuilder("test2")
                    .setDescription("test")
                    .build();
            this.attributes = Attributes.of(
                    AttributeKey.stringKey("path"), "/",
                    AttributeKey.stringKey("status"), "200");
        }
    }

    @Benchmark
    @Threads(4)
    public GaugeDataPoint prometheus(RandomNumbers randomNumbers, PrometheusGauge gauge) {
        for (int i=0; i<randomNumbers.randomNumbers.length; i++) {
            gauge.dataPoint.set(randomNumbers.randomNumbers[i]);
        }
        return gauge.dataPoint;
    }

    @Benchmark
    @Threads(4)
    public DoubleGauge openTelemetryDoubleGauge(RandomNumbers randomNumbers, OpenTelemetryGauge gauge) {
        for (int i=0; i<randomNumbers.randomNumbers.length; i++) {
            gauge.doubleGauge.set(randomNumbers.randomNumbers[i], gauge.attributes);
        }
        return gauge.doubleGauge;
    }

    @Benchmark
    @Threads(4)
    public LongGauge openTelemetryLongGauge(RandomNumbers randomNumbers, OpenTelemetryGauge gauge) {
        for (int i=0; i<randomNumbers.randomNumbers.length; i++) {
            gauge.longGauge.set(1, gauge.attributes);
        }
        return gauge.longGauge;
    }

    @Benchmark
    @Threads(4)
    public io.prometheus.client.Gauge.Child simpleclient(RandomNumbers randomNumbers, SimpleclientGauge gauge) {
        for (int i=0; i<randomNumbers.randomNumbers.length; i++) {
            gauge.dataPoint.set(randomNumbers.randomNumbers[i]);
        }
        return gauge.dataPoint;
    }
}

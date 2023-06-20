package com.example;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.core.metrics.Histogram;
import io.prometheus.metrics.core.observer.DiscreteEventObserver;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;

@State(Scope.Benchmark)
public class BenchmarkRunner {

    public static void main(String[] args) throws Exception {
       org.openjdk.jmh.Main.main(args);
        /*
        BenchmarkRunner r = new BenchmarkRunner();
        r.setUp();
        for (int i=0; i<100*100*100*10; i++) {
            //r.benchmarkOtelCounter();
            //r.benchmarkPrometheusCounter();
            r.benchmarkOtelHistogram();
        }

         */
    }

    private DiscreteEventObserver goodCases;
    private DiscreteEventObserver errorCases;

    private Attributes goodCaseAttr;
    private Attributes errorCaseAttr;
    //private LongCounter otelCounter;
    private LongCounter otelCounter;

    private DoubleAdder doubleAdder;
    private LongAdder longAdder;

    private Histogram histogram;
    DoubleHistogram otelHist;

    private AtomicBoolean atomicBoolean = new AtomicBoolean();
    @Setup
    public void setUp() {
        Counter counter = Counter.newBuilder()
                .withLabelNames("path", "status")
                .withName("events_total")
                //.withoutExemplars()
                .register();
        goodCases = counter.withLabelValues("/get", "200");
        errorCases = counter.withLabelValues("/get", "500");

        histogram = Histogram.newBuilder().withName("test").nativeOnly().build();

        // -------- otel, see https://opentelemetry.io/docs/instrumentation/java/manual/

        Resource resource = Resource.getDefault()
                .merge(Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, "logical-service-name")));

        /*
        SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor.builder(OtlpGrpcSpanExporter.builder().build()).build())
                .setResource(resource)
                .build();
         */

        InMemoryMetricReader reader = InMemoryMetricReader.create();

        SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder()
                .registerMetricReader(reader) //OtlpGrpcMetricExporter.builder().build()).build())
                .setResource(resource)
                .registerView(InstrumentSelector.builder()
                                .setName("lksd")
                                .build(),
                        View.builder()
                                .setAggregation(Aggregation.base2ExponentialBucketHistogram())
                                .build()
                        )
                .build();

        /*
        SdkLoggerProvider sdkLoggerProvider = SdkLoggerProvider.builder()
                .addLogRecordProcessor(BatchLogRecordProcessor.builder(OtlpGrpcLogRecordExporter.builder().build()).build())
                .setResource(resource)
                .build();
         */

        OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
                //.setTracerProvider(sdkTracerProvider)
                .setMeterProvider(sdkMeterProvider)
                //.setLoggerProvider(sdkLoggerProvider)
                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                .buildAndRegisterGlobal();

        Meter meter = openTelemetry.meterBuilder("instrumentation-library-name")
                .setInstrumentationVersion("1.0.0")
                .build();

        otelCounter = meter
                .counterBuilder("processed_jobs")
                //.ofDoubles()
                .setDescription("Processed jobs")
                .setUnit("1")
                .build();

        if (!"base2_exponential_bucket_histogram".equals(System.getenv("OTEL_EXPORTER_OTLP_METRICS_DEFAULT_HISTOGRAM_AGGREGATION"))) {
            throw new RuntimeException("export OTEL_EXPORTER_OTLP_METRICS_DEFAULT_HISTOGRAM_AGGREGATION=base2_exponential_bucket_histogram");
        }
        otelHist = meter.histogramBuilder("lksd")
                //.ofLongs()
                .build();

        goodCaseAttr = Attributes.builder().put("path", "/get").put("status", "200").build();
        errorCaseAttr = Attributes.builder().put("path", "/get").put("status", "500").build();

        doubleAdder = new DoubleAdder();
        longAdder = new LongAdder();
    }

    //@Benchmark
    @Fork(value = 1, warmups = 1)
    public boolean benchmarkAtomicBoolean() {
        return atomicBoolean.get();
    }

    //@Benchmark
    @Fork(value = 1, warmups = 1)
    public long benchmarkNanoTime() {
        return System.nanoTime();
    }

    //@Benchmark
    @Fork(value = 1, warmups = 1)
    public long benchmarkCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

    //@Benchmark
    @Fork(value = 1, warmups = 1)
    public void benchmarkDoubleAdder() {
        doubleAdder.add(1.0);
    }

    //@Benchmark
    @Fork(value = 1, warmups = 1)
    public void benchmarkLongAdder() {
        longAdder.add(1);
    }

    @Benchmark
    @Fork(value = 1, warmups = 1)
    public void benchmarkPrometheusHistogram() {
        Random random = new Random(0);
        for (int i = 100; i > 0; i--) {
            double d = Math.abs(random.nextGaussian());
            histogram.observe(d);
        }
    }

    @Benchmark
    @Fork(value = 1, warmups = 1)
    public void benchmarkOtelHistogram() {
        Random random = new Random(0);
        for (int i = 100; i > 0; i--) {
            double d = Math.abs(random.nextGaussian());
            otelHist.record(d);
        }
    }

    //@Benchmark
    @Fork(value = 1, warmups = 1)
    public void benchmarkPrometheusCounter() {
        for (int i = 100; i > 0; i--) {
            if (i % 8 == 0) {
                errorCases.inc();
            } else {
                goodCases.inc();
            }
        }
    }

    //@Benchmark
    @Fork(value = 1, warmups = 1)
    public void benchmarkOtelCounter() {
        for (int i = 100; i > 0; i--) {
            if (i % 8 == 0) {
                otelCounter.add(1, errorCaseAttr);
            } else {
                otelCounter.add(1, goodCaseAttr);
            }
        }
    }
}

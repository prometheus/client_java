package com.example;

import java.util.Random;

//@State(Scope.Benchmark)
public class BenchmarkRunner {

    static double[] random = new double[10*1024*1024];
    private static void initRandom() {
        Random rand = new Random(0);
        for (int i=0; i<random.length; i++) {
            random[i] = Math.abs(rand.nextGaussian());
        }
        System.out.println("random init done");
    }
    public static void main(String[] args) throws Exception {
        //org.openjdk.jmh.Main.main(args);
        /*
        BenchmarkRunner r = new BenchmarkRunner();
        r.setUp();
        for (int i=0; i<10; i++) {
            //r.benchmarkOtelCounter();
            //r.benchmarkPrometheusCounter();
            r.benchmarkOtelHistogram();
            r.benchmarkPrometheusHistogram();
        }

         */
    }

    /*
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
    //@Setup
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

        //SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
        //        .addSpanProcessor(BatchSpanProcessor.builder(OtlpGrpcSpanExporter.builder().build()).build())
                .setResource(resource)
         //       .build();

        InMemoryMetricReader reader = InMemoryMetricReader.create();

        SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder()
                .registerMetricReader(reader) //OtlpGrpcMetricExporter.builder().build()).build())
                .setResource(resource)
                .registerView(InstrumentSelector.builder()
                                .setName("lksd")
                                .build(),
                        View.builder()
                                .setAggregation(Aggregation.base2ExponentialBucketHistogram(160, 5))
                                .build()
                        )
                .build();


       // SdkLoggerProvider sdkLoggerProvider = SdkLoggerProvider.builder()
       //         .addLogRecordProcessor(BatchLogRecordProcessor.builder(OtlpGrpcLogRecordExporter.builder().build()).build())
        //        .setResource(resource)
         //       .build();

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

        initRandom();
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

    //@Benchmak
    @Threads(4)
    @Fork(value = 1, warmups = 1)
    public void benchmarkPrometheusHistogram() {
        for (double observation : random) {
            histogram.observe(observation);
        }
    }

    //@Benchmark
    @Threads(4)
    @Fork(value = 1, warmups = 1)
    public void benchmarkOtelHistogram() {
        for (double observation : random) {
            otelHist.record(observation);
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
    */
}

package io.prometheus.metrics.exporter.opentelemetry;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.assertj.MetricAssert;
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions;
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.core.metrics.Gauge;
import io.prometheus.metrics.core.metrics.Histogram;
import io.prometheus.metrics.core.metrics.Info;
import io.prometheus.metrics.core.metrics.StateSet;
import io.prometheus.metrics.core.metrics.Summary;
import io.prometheus.metrics.model.registry.Collector;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.Unit;
import io.prometheus.metrics.model.snapshots.UnknownSnapshot;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class ExportTest {

  private static final Attributes ATTRIBUTES =
      Attributes.of(AttributeKey.stringKey("label"), "val", AttributeKey.stringKey("key"), "value");
  @RegisterExtension static OpenTelemetryExtension testing = OpenTelemetryExtension.create();

  private final PrometheusRegistry registry = new PrometheusRegistry();

  @BeforeEach
  void setUp() throws IllegalAccessException, NoSuchFieldException {
    Field field = testing.getClass().getDeclaredField("metricReader");
    field.setAccessible(true);
    MetricReader reader = (MetricReader) field.get(testing);

    PrometheusMetricProducer prometheusMetricProducer =
        new PrometheusMetricProducer(
            registry,
            InstrumentationScopeInfo.create("test"),
            Resource.create(Attributes.builder().put("staticRes", "value").build()));

    reader.register(prometheusMetricProducer);
  }

  @Test
  void targetInfo() {
    Info.builder().name("target").constLabels(Labels.of("res", "value")).register(registry);
    Labels scope = Labels.of("otel_scope_name", "scope", "otel_scope_version", "1");
    Info.builder()
        .name("otel_scope")
        .constLabels(scope.add("scopeKey", "value"))
        .register(registry);
    Counter counter = Counter.builder().name("name").constLabels(scope).register(registry);
    counter.inc();
    metricAssert()
        .hasResource(
            Resource.create(
                Attributes.builder().put("res", "value").put("staticRes", "value").build()))
        .hasInstrumentationScope(
            InstrumentationScopeInfo.builder("scope")
                .setAttributes(Attributes.of(AttributeKey.stringKey("scopeKey"), "value"))
                .setVersion("1")
                .build());
  }

  @Test
  void counter() {
    Counter counter =
        Counter.builder()
            .name("name")
            .help("help")
            .constLabels(Labels.of("key", "value"))
            .labelNames("label")
            .unit(Unit.BYTES)
            .withExemplars()
            .register(registry);
    counter.labelValues("val").inc();
    metricAssert()
        .hasName("name")
        .hasDescription("help")
        .hasUnit("By")
        .hasDoubleSumSatisfying(
            sum ->
                sum.isMonotonic()
                    .isCumulative()
                    .hasPointsSatisfying(points -> points.hasValue(1.0).hasAttributes(ATTRIBUTES)));
  }

  @Test
  void histogram() {
    Histogram histogram =
        Histogram.builder()
            .name("name")
            .help("help")
            .constLabels(Labels.of("key", "value"))
            .labelNames("label")
            .unit(Unit.BYTES)
            .classicOnly()
            .classicUpperBounds(1, 2, 3)
            .register(registry);
    histogram.labelValues("val").observe(1);
    metricAssert()
        .hasName("name")
        .hasDescription("help")
        .hasUnit("By")
        .hasHistogramSatisfying(
            hist ->
                hist.hasPointsSatisfying(
                    points ->
                        points
                            .hasAttributes(ATTRIBUTES)
                            .hasCount(1)
                            .hasSum(1.0)
                            .satisfies(
                                p -> {
                                  assertThat(p.getMin()).isNaN();
                                  assertThat(p.getMax()).isNaN();
                                  assertThat(p.getStartEpochNanos()).isPositive();
                                  assertThat(p.getEpochNanos()).isPositive();
                                })
                            .hasExemplars()
                            .hasBucketBoundaries(1, 2, 3, Double.POSITIVE_INFINITY)
                            .hasBucketCounts(1, 0, 0, 0)));
  }

  @Test
  void exponentialHistogram() {
    Histogram histogram =
        Histogram.builder()
            .name("name")
            .help("help")
            .constLabels(Labels.of("key", "value"))
            .labelNames("label")
            .unit(Unit.BYTES)
            .nativeOnly()
            .register(registry);
    histogram.labelValues("val").observe(1);
    metricAssert()
        .hasName("name")
        .hasDescription("help")
        .hasUnit("By")
        .hasExponentialHistogramSatisfying(
            hist ->
                hist.isCumulative()
                    .hasPointsSatisfying(
                        points ->
                            points
                                .hasAttributes(ATTRIBUTES)
                                .hasCount(1)
                                .hasScale(5)
                                .hasZeroCount(0)
                                .hasSum(1.0)
                                .satisfies(
                                    p -> {
                                      assertThat(p.getMin()).isNaN();
                                      assertThat(p.getMax()).isNaN();
                                      assertThat(p.getStartEpochNanos()).isPositive();
                                      assertThat(p.getEpochNanos()).isPositive();
                                    })
                                .hasExemplars()
                                .hasPositiveBucketsSatisfying(
                                    buckets ->
                                        buckets
                                            .hasOffset(-1)
                                            .hasTotalCount(1)
                                            .hasCounts(Collections.singletonList(1L)))));
  }

  @Test
  void summary() {
    Summary summary =
        Summary.builder()
            .name("name")
            .help("help")
            .constLabels(Labels.of("key", "value"))
            .labelNames("label")
            .unit(Unit.BYTES)
            .quantile(0.5, 0.1)
            .register(registry);
    summary.labelValues("val").observe(1);
    metricAssert()
        .hasName("name")
        .hasDescription("help")
        .hasUnit("By")
        .hasSummarySatisfying(
            sum ->
                sum.hasPointsSatisfying(
                    points ->
                        points
                            .hasAttributes(ATTRIBUTES)
                            .hasCount(1)
                            .hasSum(1.0)
                            .satisfies(
                                p -> {
                                  assertThat(p.getStartEpochNanos()).isPositive();
                                  assertThat(p.getEpochNanos()).isPositive();
                                })
                            .hasValuesSatisfying(values -> values.hasQuantile(0.5).hasValue(1.0))));
  }

  @Test
  void gauge() {
    Gauge gauge =
        Gauge.builder()
            .name("name")
            .help("help")
            .constLabels(Labels.of("key", "value"))
            .labelNames("label")
            .unit(Unit.BYTES)
            .register(registry);
    gauge.labelValues("val").set(1);
    metricAssert()
        .hasName("name")
        .hasDescription("help")
        .hasUnit("By")
        .hasDoubleGaugeSatisfying(
            gaugeData ->
                gaugeData.hasPointsSatisfying(
                    points -> points.hasValue(1.0).hasExemplars().hasAttributes(ATTRIBUTES)));
  }

  @Test
  void stateSet() {
    StateSet stateSet =
        StateSet.builder()
            .name("name")
            .help("help")
            .constLabels(Labels.of("key", "value"))
            .labelNames("label")
            .states("state")
            .register(registry);
    stateSet.labelValues("val").setTrue("state");
    metricAssert()
        .hasName("name")
        .hasDescription("help")
        .hasDoubleSumSatisfying(
            sum ->
                sum.isNotMonotonic()
                    .isCumulative()
                    .hasPointsSatisfying(
                        points ->
                            points
                                .hasValue(1.0)
                                .hasAttributes(
                                    ATTRIBUTES.toBuilder().put("name", "state").build())));
  }

  @Test
  void info() {
    Info info =
        Info.builder()
            .name("name")
            .help("help")
            .constLabels(Labels.of("key", "value"))
            .labelNames("label")
            .register(registry);
    info.addLabelValues("val");
    metricAssert()
        .hasName("name")
        .hasDescription("help")
        .hasDoubleSumSatisfying(
            sum ->
                sum.isCumulative()
                    .isNotMonotonic()
                    .hasPointsSatisfying(points -> points.hasAttributes(ATTRIBUTES).hasValue(1.0)));
  }

  @Test
  void unknown() {
    Collector collector =
        () ->
            UnknownSnapshot.builder()
                .name("name_bytes")
                .help("help")
                .unit(Unit.BYTES)
                .dataPoint(
                    UnknownSnapshot.UnknownDataPointSnapshot.builder()
                        .value(1.0)
                        .labels(Labels.of("label", "val"))
                        .build())
                .build();
    registry.register(collector);
    metricAssert()
        .hasName("name")
        .hasDescription("help")
        .hasUnit("By")
        .hasDoubleGaugeSatisfying(
            gaugeData ->
                gaugeData.hasPointsSatisfying(
                    points ->
                        points
                            .hasValue(1.0)
                            .hasExemplars()
                            .hasAttributes(Attributes.of(AttributeKey.stringKey("label"), "val"))));
  }

  @Test
  void metricsWithoutDataPointsAreNotExported() {
    // Register metrics with labels but don't create any data points
    // This simulates the jvm_memory_pool_allocated_bytes scenario where a metric
    // is registered with label names, but no data points are created until GC happens
    Counter.builder().name("counter_no_data").labelNames("pool").register(registry);
    Gauge.builder().name("gauge_no_data").labelNames("pool").register(registry);
    Summary.builder().name("summary_no_data").labelNames("pool").register(registry);
    Histogram.builder().name("histogram_no_data").labelNames("pool").register(registry);
    StateSet.builder()
        .name("stateset_no_data")
        .states("state")
        .labelNames("pool")
        .register(registry);
    Info.builder().name("info_no_data").labelNames("pool").register(registry);

    List<MetricData> metrics = testing.getMetrics();
    assertThat(metrics).isEmpty();
  }

  private MetricAssert metricAssert() {
    List<MetricData> metrics = testing.getMetrics();
    assertThat(metrics).hasSize(1);
    return OpenTelemetryAssertions.assertThat(metrics.get(0));
  }
}

package io.prometheus.metrics.core.metrics;

import static io.prometheus.metrics.core.metrics.TestUtil.assertExemplarEquals;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

import io.prometheus.metrics.core.exemplars.ExemplarSamplerConfigTestUtil;
import io.prometheus.metrics.expositionformats.PrometheusProtobufWriter;
import io.prometheus.metrics.expositionformats.generated.com_google_protobuf_3_25_3.Metrics;
import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.Exemplar;
import io.prometheus.metrics.model.snapshots.Label;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.Unit;
import io.prometheus.metrics.shaded.com_google_protobuf_3_25_3.TextFormat;
import io.prometheus.metrics.tracer.common.SpanContext;
import io.prometheus.metrics.tracer.initializer.SpanContextSupplier;
import java.util.Arrays;
import java.util.Iterator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CounterTest {

  private Counter noLabels;
  private Counter labels;
  private static final long exemplarSampleIntervalMillis = 10;
  private static final long exemplarMinAgeMillis = 100;
  private SpanContext origSpanContext;

  @BeforeEach
  public void setUp() throws NoSuchFieldException, IllegalAccessException {
    noLabels = Counter.builder().name("nolabels").build();
    labels =
        Counter.builder().name("labels").help("help").unit(Unit.SECONDS).labelNames("l").build();
    origSpanContext = SpanContextSupplier.getSpanContext();
    ExemplarSamplerConfigTestUtil.setSampleIntervalMillis(noLabels, exemplarSampleIntervalMillis);
    ExemplarSamplerConfigTestUtil.setMinRetentionPeriodMillis(noLabels, exemplarMinAgeMillis);
    ExemplarSamplerConfigTestUtil.setSampleIntervalMillis(labels, exemplarSampleIntervalMillis);
    ExemplarSamplerConfigTestUtil.setMinRetentionPeriodMillis(labels, exemplarMinAgeMillis);
  }

  @AfterEach
  public void tearDown() {
    SpanContextSupplier.setSpanContext(origSpanContext);
  }

  private CounterSnapshot.CounterDataPointSnapshot getData(Counter counter, String... labels) {
    return counter.collect().getDataPoints().stream()
        .filter(d -> d.getLabels().equals(Labels.of(labels)))
        .findAny()
        .orElseThrow(
            () ->
                new RuntimeException(
                    "counter with labels " + Arrays.toString(labels) + " not found"));
  }

  private double getValue(Counter counter, String... labels) {
    return getData(counter, labels).getValue();
  }

  private int getNumberOfLabels(Counter counter) {
    return counter.collect().getDataPoints().size();
  }

  @Test
  public void testIncrement() {
    noLabels.inc();
    assertThat(getValue(noLabels)).isCloseTo(1.0, offset(.001));
    noLabels.inc(2);
    assertThat(getValue(noLabels)).isCloseTo(3.0, offset(.001));
    noLabels.labelValues().inc(4);
    assertThat(getValue(noLabels)).isCloseTo(7.0, offset(.001));
    noLabels.labelValues().inc();
    assertThat(getValue(noLabels)).isCloseTo(8.0, offset(.001));
  }

  @Test
  public void testNegativeIncrementFails() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> noLabels.inc(-1))
        .withMessage("Negative increment -1 is illegal for Counter metrics.");
  }

  @Test
  public void testEmptyCountersHaveNoLabels() {
    assertThat(getNumberOfLabels(noLabels)).isOne();
    assertThat(getNumberOfLabels(labels)).isZero();
  }

  @Test
  public void testLabels() {
    assertThat(getNumberOfLabels(labels)).isZero();
    labels.labelValues("a").inc();
    assertThat(getNumberOfLabels(labels)).isOne();
    assertThat(getValue(labels, "l", "a")).isCloseTo(1.0, offset(.001));
    labels.labelValues("b").inc(3);
    assertThat(getNumberOfLabels(labels)).isEqualTo(2);
    assertThat(getValue(labels, "l", "a")).isCloseTo(1.0, offset(.001));
    assertThat(getValue(labels, "l", "b")).isCloseTo(3.0, offset(.001));
  }

  @Test
  public void testTotalStrippedFromName() {
    for (String name :
        new String[] {
          "my_counter_total", "my.counter.total",
          "my_counter_seconds_total", "my.counter.seconds.total",
          "my_counter", "my.counter",
          "my_counter_seconds", "my.counter.seconds"
        }) {
      Counter counter = Counter.builder().name(name).unit(Unit.SECONDS).build();
      Metrics.MetricFamily protobufData = new PrometheusProtobufWriter().convert(counter.collect());
      assertThat(TextFormat.printer().shortDebugString(protobufData))
          .isEqualTo(
              "name: \"my_counter_seconds_total\" type: COUNTER metric { counter { value: 0.0 } }");
    }
  }

  @Test
  public void testSnapshotComplete() {
    long before = System.currentTimeMillis();
    Counter counter =
        Counter.builder()
            .name("test_seconds_total")
            .unit(Unit.SECONDS)
            .help("help message")
            .constLabels(Labels.of("const1name", "const1value", "const2name", "const2value"))
            .labelNames("path", "status")
            .build();
    counter.labelValues("/", "200").inc(2);
    counter.labelValues("/", "500").inc();
    CounterSnapshot snapshot = counter.collect();
    assertThat(snapshot.getMetadata().getName()).isEqualTo("test_seconds");
    assertThat(snapshot.getMetadata().getUnit()).hasToString("seconds");
    assertThat(snapshot.getMetadata().getHelp()).isEqualTo("help message");
    assertThat(snapshot.getDataPoints()).hasSize(2);
    Iterator<CounterSnapshot.CounterDataPointSnapshot> iter = snapshot.getDataPoints().iterator();
    // data is ordered by labels, so 200 comes before 500
    CounterSnapshot.CounterDataPointSnapshot data = iter.next();
    assertThat((Iterable<? extends Label>) data.getLabels())
        .isEqualTo(
            Labels.of(
                "const1name",
                "const1value",
                "const2name",
                "const2value",
                "path",
                "/",
                "status",
                "200"));
    assertThat(data.getValue()).isCloseTo(2, offset(0.0001));
    assertThat(data.getCreatedTimestampMillis())
        .isGreaterThanOrEqualTo(before)
        .isLessThanOrEqualTo(System.currentTimeMillis());
    // 500
    data = iter.next();
    assertThat((Iterable<? extends Label>) data.getLabels())
        .isEqualTo(
            Labels.of(
                "const1name",
                "const1value",
                "const2name",
                "const2value",
                "path",
                "/",
                "status",
                "500"));
    assertThat(data.getValue()).isCloseTo(1, offset(0.0001));
    assertThat(data.getCreatedTimestampMillis())
        .isGreaterThanOrEqualTo(before)
        .isLessThanOrEqualTo(System.currentTimeMillis());
  }

  @Test
  public void testIncWithExemplar() throws Exception {
    noLabels.incWithExemplar(Labels.of("key", "value"));
    assertExemplar(noLabels, 1.0, "key", "value");

    Thread.sleep(exemplarMinAgeMillis + 2 * exemplarSampleIntervalMillis);

    noLabels.incWithExemplar(Labels.EMPTY);
    assertExemplar(noLabels, 1.0);

    Thread.sleep(exemplarMinAgeMillis + 2 * exemplarSampleIntervalMillis);

    noLabels.incWithExemplar(3, Labels.of("key1", "value1", "key2", "value2"));
    assertExemplar(noLabels, 3, "key1", "value1", "key2", "value2");
  }

  private void assertExemplar(Counter counter, double value, String... labels) {
    Exemplar exemplar = getData(counter).getExemplar();
    assertThat(exemplar.getValue()).isCloseTo(value, offset(0.0001));
    assertThat((Iterable<? extends Label>) exemplar.getLabels()).isEqualTo(Labels.of(labels));
  }

  @Test
  public void testExemplarSampler() throws Exception {
    final Exemplar exemplar1 = Exemplar.builder().value(2.0).traceId("abc").spanId("123").build();
    final Exemplar exemplar2 = Exemplar.builder().value(1.0).traceId("def").spanId("456").build();
    final Exemplar exemplar3 = Exemplar.builder().value(1.0).traceId("123").spanId("abc").build();
    final Exemplar customExemplar =
        Exemplar.builder()
            .value(1.0)
            .traceId("bab")
            .spanId("cdc")
            .labels(Labels.of("test", "test"))
            .build();
    SpanContext spanContext =
        new SpanContext() {
          private int callNumber = 0;

          @Override
          public String getCurrentTraceId() {
            switch (callNumber) {
              case 1:
                return "abc";
              case 3:
                return "def";
              case 4:
                return "123";
              case 5:
                return "bab";
              default:
                throw new RuntimeException("unexpected call");
            }
          }

          @Override
          public String getCurrentSpanId() {
            switch (callNumber) {
              case 1:
                return "123";
              case 3:
                return "456";
              case 4:
                return "abc";
              case 5:
                return "cdc";
              default:
                throw new RuntimeException("unexpected call");
            }
          }

          @Override
          public boolean isCurrentSpanSampled() {
            callNumber++;
            return callNumber != 2;
          }

          @Override
          public void markCurrentSpanAsExemplar() {}
        };
    Counter counter = Counter.builder().name("count_total").build();

    SpanContextSupplier.setSpanContext(spanContext);
    ExemplarSamplerConfigTestUtil.setMinRetentionPeriodMillis(counter, exemplarMinAgeMillis);
    ExemplarSamplerConfigTestUtil.setSampleIntervalMillis(counter, exemplarSampleIntervalMillis);

    counter.inc(2.0);
    assertExemplarEquals(exemplar1, getData(counter).getExemplar());

    Thread.sleep(2 * exemplarSampleIntervalMillis);

    counter.inc(
        3.0); // min age not reached -> keep the previous exemplar, exemplar sampler not called
    assertExemplarEquals(exemplar1, getData(counter).getExemplar());

    Thread.sleep(exemplarMinAgeMillis + 2 * exemplarSampleIntervalMillis);

    counter.inc(2.0); // 2nd call: isSampled() returns false -> not sampled
    assertExemplarEquals(exemplar1, getData(counter).getExemplar());

    Thread.sleep(2 * exemplarSampleIntervalMillis);

    counter.inc(1.0); // sampled
    assertExemplarEquals(exemplar2, getData(counter).getExemplar());

    Thread.sleep(exemplarMinAgeMillis + 2 * exemplarSampleIntervalMillis);

    counter.inc(1.0); // sampled
    assertExemplarEquals(exemplar3, getData(counter).getExemplar());

    Thread.sleep(2 * exemplarSampleIntervalMillis);

    counter.incWithExemplar(
        Labels.of(
            "test",
            "test")); // custom exemplar sampled even though the automatic exemplar hasn't reached
    // min age yet
    assertExemplarEquals(customExemplar, getData(counter).getExemplar());
  }

  @Test
  public void testExemplarSamplerDisabled() {
    Counter counter =
        Counter.builder()
            // .withExemplarSampler((inc, prev) -> {throw new RuntimeException("unexpected call to
            // exemplar sampler");})
            .name("count_total")
            .withoutExemplars()
            .build();
    counter.incWithExemplar(3.0, Labels.of("a", "b"));
    assertThat(getData(counter).getExemplar()).isNull();
    counter.inc(2.0);
    assertThat(getData(counter).getExemplar()).isNull();
  }

  @Test
  public void testConstLabelsFirst() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(
            () ->
                Counter.builder()
                    .name("test_total")
                    .constLabels(Labels.of("const_a", "const_b"))
                    .labelNames("const.a")
                    .build());
  }

  @Test
  public void testConstLabelsSecond() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(
            () ->
                Counter.builder()
                    .name("test_total")
                    .labelNames("const.a")
                    .constLabels(Labels.of("const_a", "const_b"))
                    .build());
  }
}

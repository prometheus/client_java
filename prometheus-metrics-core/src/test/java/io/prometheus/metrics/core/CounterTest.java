package io.prometheus.metrics.core;

import com.google.protobuf.TextFormat;
import io.prometheus.client.exemplars.tracer.common.SpanContextSupplier;
import io.prometheus.expositionformat.protobuf.Protobuf;
import io.prometheus.expositionformat.protobuf.generated.Metrics;
import io.prometheus.metrics.exemplars.ExemplarConfig;
import io.prometheus.metrics.model.CounterSnapshot;
import io.prometheus.metrics.model.Exemplar;
import io.prometheus.metrics.model.Labels;
import io.prometheus.metrics.model.MetricType;
import io.prometheus.metrics.model.Unit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Iterator;

import static io.prometheus.metrics.core.TestUtil.assertExemplarEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.rules.ExpectedException.none;

public class CounterTest {

  Counter noLabels;
  Counter labels;
  private static final long exemplarSampleIntervalMillis = 10;
  private static final long exemplarMinAgeMillis = 100;

  @Rule
  public final ExpectedException thrown = none();

  @BeforeClass
  public static void beforeClass() {
    ExemplarConfig.setDefaultSampleIntervalMillis(exemplarSampleIntervalMillis);
    ExemplarConfig.setDefaultMinAgeMillis(exemplarMinAgeMillis);
  }

  @Before
  public void setUp() {
    noLabels = Counter.newBuilder().withName("nolabels").build();
    labels = Counter.newBuilder().withName("labels")
            .withHelp("help")
            .withUnit(Unit.SECONDS)
            .withLabelNames("l")
            .build();
  }

  private CounterSnapshot.CounterData getData(Counter counter, String... labels) {
    return counter.collect().getData().stream()
            .filter(d -> d.getLabels().equals(Labels.of(labels)))
            .findAny()
            .orElseThrow(() -> new RuntimeException("counter with labels " + labels + " not found"));
  }

  private double getValue(Counter counter, String... labels) {
    return getData(counter, labels).getValue();
  }


  private int getNumberOfLabels(Counter counter) {
    return ((CounterSnapshot) counter.collect()).getData().size();
  }

  @Test
  public void testIncrement() {
    noLabels.inc();
    assertEquals(1.0, getValue(noLabels), .001);
    noLabels.inc(2);
    assertEquals(3.0, getValue(noLabels), .001);
    noLabels.withLabels().inc(4);
    assertEquals(7.0, getValue(noLabels), .001);
    noLabels.withLabels().inc();
    assertEquals(8.0, getValue(noLabels), .001);
  }
    
  @Test
  public void testNegativeIncrementFails() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Negative increment -1.0 is illegal for Counter metrics.");
    noLabels.inc(-1);
  }
  
  @Test
  public void testEmptyCountersHaveNoLabels() {
    assertEquals(1, getNumberOfLabels(noLabels));
    assertEquals(0, getNumberOfLabels(labels));
  }

  @Test
  public void testLabels() {
    assertEquals(0, getNumberOfLabels(labels));
    labels.withLabels("a").inc();
    assertEquals(1, getNumberOfLabels(labels));
    assertEquals(1.0, getValue(labels, "l", "a"), .001);
    labels.withLabels("b").inc(3);
    assertEquals(2, getNumberOfLabels(labels));
    assertEquals(1.0, getValue(labels, "l", "a"), .001);
    assertEquals(3.0, getValue(labels, "l", "b"), .001);
  }

  @Test
  public void testTotalStrippedFromName() {
    Counter counter = Counter.newBuilder()
            .withName("my_counter_total")
            .withUnit(Unit.SECONDS)
            .build();
    Metrics.MetricFamily protobufData = Protobuf.convert(counter.collect());
    assertEquals("name: \"my_counter_seconds_total\" type: COUNTER metric { counter { value: 0.0 } }", TextFormat.printer().shortDebugString(protobufData));

    counter = Counter.newBuilder()
            .withName("my_counter")
            .build();
    protobufData = Protobuf.convert(counter.collect());
    assertEquals("name: \"my_counter_total\" type: COUNTER metric { counter { value: 0.0 } }", TextFormat.printer().shortDebugString(protobufData));
  }

  @Test
  public void testSnapshotComplete() {
    long before = System.currentTimeMillis();
    Counter counter = Counter.newBuilder()
            .withName("test_seconds_total")
            .withUnit(Unit.SECONDS)
            .withHelp("help message")
            .withConstLabels(Labels.of("const1name", "const1value", "const2name", "const2value"))
            .withLabelNames("path", "status")
            .build();
    counter.withLabels("/", "200").inc(2);
    counter.withLabels("/", "500").inc();
    CounterSnapshot snapshot = (CounterSnapshot) counter.collect();
    Assert.assertEquals("test_seconds", snapshot.getMetadata().getName());
    Assert.assertEquals("seconds", snapshot.getMetadata().getUnit());
    Assert.assertEquals("help message", snapshot.getMetadata().getHelp());
    Assert.assertEquals(MetricType.COUNTER, snapshot.getMetadata().getType());
    Assert.assertEquals(2, snapshot.getData().size());
    Iterator<CounterSnapshot.CounterData> iter = snapshot.getData().iterator();
    // data is ordered by labels, so 200 comes before 500
    CounterSnapshot.CounterData data = iter.next();
    Assert.assertEquals(Labels.of("const1name", "const1value", "const2name", "const2value", "path", "/", "status", "200"), data.getLabels());
    Assert.assertEquals(2, data.getValue(), 0.0001);
    Assert.assertTrue(data.getCreatedTimeMillis() >= before);
    Assert.assertTrue(data.getCreatedTimeMillis() <= System.currentTimeMillis());
    // 500
    data = iter.next();
    Assert.assertEquals(Labels.of("const1name", "const1value", "const2name", "const2value", "path", "/", "status", "500"), data.getLabels());
    Assert.assertEquals(1, data.getValue(), 0.0001);
    Assert.assertTrue(data.getCreatedTimeMillis() >= before);
    Assert.assertTrue(data.getCreatedTimeMillis() <= System.currentTimeMillis());
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
    Assert.assertEquals(value, exemplar.getValue(), 0.0001);
    assertEquals(Labels.of(labels), exemplar.getLabels());
  }

  @Test
  public void testExemplarSampler() throws Exception {
    final Exemplar exemplar1 = Exemplar.newBuilder()
            .withValue(2.0)
            .withTraceId("abc")
            .withSpanId("123")
            .build();
    final Exemplar exemplar2 = Exemplar.newBuilder()
            .withValue(1.0)
            .withTraceId("def")
            .withSpanId("456")
            .build();
    final Exemplar exemplar3 = Exemplar.newBuilder()
            .withValue(1.0)
            .withTraceId("123")
            .withSpanId("abc")
            .build();
    final Exemplar customExemplar = Exemplar.newBuilder()
            .withValue(1.0)
            .withTraceId("bab")
            .withSpanId("cdc")
            .withLabels(Labels.of("test", "test"))
            .build();
    SpanContextSupplier scs = new SpanContextSupplier() {
      private int callNumber = 0;
      @Override
      public String getTraceId() {
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
      public String getSpanId() {
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
      public boolean isSampled() {
        callNumber++;
        if (callNumber == 2) {
          return false;
        }
        return true;
      }
    };
    Counter counter = Counter.newBuilder()
            .withExemplarConfig(ExemplarConfig.newBuilder().withSpanContextSupplier(scs).build())
            .withName("count_total")
            .build();

    counter.inc(2.0);
    assertExemplarEquals(exemplar1, getData(counter).getExemplar());

    Thread.sleep(2 * exemplarSampleIntervalMillis);

    counter.inc(3.0); // min age not reached -> keep the previous exemplar, exemplar sampler not called
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

    counter.incWithExemplar(Labels.of("test", "test")); // custom exemplar sampled even though the automatic exemplar hasn't reached min age yet
    assertExemplarEquals(customExemplar, getData(counter).getExemplar());
  }

  @Test
  public void testExemplarSamplerDisabled() {
    Counter counter = Counter.newBuilder()
            //.withExemplarSampler((inc, prev) -> {throw new RuntimeException("unexpected call to exemplar sampler");})
            .withName("count_total")
            .withoutExemplars()
            .build();
    counter.incWithExemplar(3.0, Labels.of("a", "b"));
    Assert.assertNull(getData(counter).getExemplar());
    counter.inc(2.0);
    Assert.assertNull(getData(counter).getExemplar());
  }
}

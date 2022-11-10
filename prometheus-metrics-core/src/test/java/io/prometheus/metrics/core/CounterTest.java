package io.prometheus.metrics.core;

import com.google.protobuf.TextFormat;
import io.prometheus.expositionformat.protobuf.Protobuf;
import io.prometheus.expositionformat.protobuf.generated.Metrics;
import io.prometheus.metrics.exemplars.CounterExemplarSampler;
import io.prometheus.metrics.model.CounterSnapshot;
import io.prometheus.metrics.model.Exemplar;
import io.prometheus.metrics.model.Labels;
import io.prometheus.metrics.model.MetricType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.rules.ExpectedException.none;

public class CounterTest {

  Counter noLabels;
  Counter labels;

  @Rule
  public final ExpectedException thrown = none();

  @Before
  public void setUp() {
    noLabels = Counter.newBuilder().withName("nolabels").build();
    labels = Counter.newBuilder().withName("labels")
            .withHelp("help")
            .withUnit("seconds")
            .withLabelNames("l")
            .build();
  }

  private CounterSnapshot.CounterData getData(Counter counter, String... labels) {
    return ((CounterSnapshot) counter.collect()).getData().stream()
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
            .withUnit("seconds")
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
            .withUnit("seconds")
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
  public void testIncWithExemplar() {
    noLabels.incWithExemplar(Labels.of("key", "value"));
    assertExemplar(noLabels, 1.0, "key", "value");

    noLabels.incWithExemplar(Labels.EMPTY);
    assertExemplar(noLabels, 1.0);

    noLabels.incWithExemplar(3, Labels.of("key1", "value1", "key2", "value2"));
    assertExemplar(noLabels, 3, "key1", "value1", "key2", "value2");
  }

  private void assertExemplar(Counter counter, double value, String... labels) {
    Exemplar exemplar = getData(counter).getExemplar();
    Assert.assertEquals(value, exemplar.getValue(), 0.0001);
    Assert.assertEquals(Labels.of(labels), exemplar.getLabels());
  }

  @Test
  public void testExemplarSampler() {
    final Exemplar exemplar1 = new Exemplar(2.0, Labels.of("trace_id", "abc", "span_id", "123"), System.currentTimeMillis());
    final Exemplar exemplar2 = new Exemplar(1.0, Labels.of("trace_id", "def", "span_id", "456"), System.currentTimeMillis());
    final Exemplar exemplar3 = new Exemplar(1.0, Labels.of("trace_id", "123", "span_id", "abc"), System.currentTimeMillis());
    final AtomicReference<Exemplar> previous = new AtomicReference<>();
    final AtomicReference<Integer> callNumber = new AtomicReference<>(0);
    CounterExemplarSampler exemplarSampler = (increment, prev) -> {
      assertEquals(previous.get(), prev);
      switch (callNumber.get()) {
        case 0:
          return exemplar1;
        case 1:
          return null;
        case 2:
          return exemplar1;
        case 3:
          return exemplar2;
        case 4:
          return exemplar3;
        default:
          throw new RuntimeException("Unexpected 6th call");
      }
    };
    Counter counter = Counter.newBuilder()
            .withExemplarSampler(exemplarSampler)
            .withName("count_total")
            .build();

    counter.inc(2.0);
    assertExemplarEquals(exemplar1, getData(counter).getExemplar());
    previous.set(exemplar1);
    callNumber.set(callNumber.get() + 1);

    counter.inc(3.0); // exemplar sampler returns null -> keep the same exemplar
    assertExemplarEquals(exemplar1, getData(counter).getExemplar());
    callNumber.set(callNumber.get() + 1);

    counter.inc(2.0); // exemplar sampler returns exemplar1 -> keep it
    assertExemplarEquals(exemplar1, getData(counter).getExemplar());
    callNumber.set(callNumber.get() + 1);

    counter.inc(1.0);
    assertExemplarEquals(exemplar2, getData(counter).getExemplar());
    previous.set(exemplar2);
    callNumber.set(callNumber.get() + 1);

    counter.inc(1.0);
    assertExemplarEquals(exemplar3, getData(counter).getExemplar());
    previous.set(exemplar3);
    callNumber.set(callNumber.get() + 1);

    counter.incWithExemplar(Labels.of("test", "test"));
    // exemplarSampler not called, otherwise it would throw an exception
    assertExemplarEquals(new Exemplar(1.0, Labels.of("test", "test"), System.currentTimeMillis()), getData(counter).getExemplar());
  }

  @Test
  public void testExemplarSamplerDisabled() {
    Counter counter = Counter.newBuilder()
            .withExemplarSampler((inc, prev) -> {throw new RuntimeException("unexpected call to exemplar sampler");})
            .withName("count_total")
            .withoutExemplars()
            .build();
    counter.incWithExemplar(3.0, Labels.of("a", "b"));
    Assert.assertNull(getData(counter).getExemplar());
    counter.inc(2.0);
  }

  private void assertExemplarEquals(Exemplar expected, Exemplar actual) {
    // ignore timestamp
    Assert.assertEquals(expected.getValue(), actual.getValue(), 0.001);
    Assert.assertEquals(expected.getLabels(), actual.getLabels());
  }
}

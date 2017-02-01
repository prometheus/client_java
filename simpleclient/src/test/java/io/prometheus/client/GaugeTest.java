package io.prometheus.client;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class GaugeTest {

  CollectorRegistry registry;
  Gauge noLabels, labels;

  @Before
  public void setUp() {
    registry = new CollectorRegistry();
    noLabels = Gauge.build().name("nolabels").help("help").register(registry);
    labels = Gauge.build().name("labels").help("help").labelNames("l").register(registry);
  }

  @After
  public void tearDown() {
    Gauge.Child.timeProvider = new Gauge.TimeProvider();
  }

  private double getValue() {
    return registry.getSampleValue("nolabels").doubleValue();
  }
  
  @Test
  public void testIncrement() {
    noLabels.inc();
    assertEquals(1.0, getValue(), .001);
    assertEquals(1.0, noLabels.get(), .001);
    noLabels.inc(2);
    assertEquals(3.0, getValue(), .001);
    assertEquals(3.0, noLabels.get(), .001);
    noLabels.labels().inc(4);
    assertEquals(7.0, getValue(), .001);
    assertEquals(7.0, noLabels.get(), .001);
    noLabels.labels().inc();
    assertEquals(8.0, getValue(), .001);
    assertEquals(8.0, noLabels.get(), .001);
  }
    
  @Test
  public void testDecrement() {
    noLabels.dec();
    assertEquals(-1.0, getValue(), .001);
    noLabels.dec(2);
    assertEquals(-3.0, getValue(), .001);
    noLabels.labels().dec(4);
    assertEquals(-7.0, getValue(), .001);
    noLabels.labels().dec();
    assertEquals(-8.0, getValue(), .001);
  }
  
  @Test
  public void testSet() {
    noLabels.set(42);
    assertEquals(42, getValue(), .001);
    noLabels.labels().set(7);
    assertEquals(7.0, getValue(), .001);
  }

  @Test
  public void testSetToCurrentTime() {
    Gauge.Child.timeProvider = new Gauge.TimeProvider() {
      long currentTimeMillis() {
        return 42000;
      }
    };
    noLabels.setToCurrentTime();
    assertEquals(42, getValue(), .001);
  }

  @Test
  public void testTimer() {
    Gauge.Child.timeProvider = new Gauge.TimeProvider() {
      long value = (long)(30 * 1e9);
      long nanoTime() {
        value += (long)(10 * 1e9);
        return value;
      }
    };

    double elapsed = noLabels.setToTime(new Runnable() {
      @Override
      public void run() {
        //no op
      }
    });
    assertEquals(10, elapsed, .001);

    Gauge.Timer timer = noLabels.startTimer();
    elapsed = timer.setDuration();
    assertEquals(10, getValue(), .001);
    assertEquals(10, elapsed, .001);
  }

  @Test
  public void noLabelsDefaultZeroValue() {
    assertEquals(0.0, getValue(), .001);
  }
  
  private Double getLabelsValue(String labelValue) {
    return registry.getSampleValue("labels", new String[]{"l"}, new String[]{labelValue});
  }

  @Test
  public void testLabels() {
    assertEquals(null, getLabelsValue("a"));
    assertEquals(null, getLabelsValue("b"));
    labels.labels("a").inc();
    assertEquals(1.0, getLabelsValue("a").doubleValue(), .001);
    assertEquals(null, getLabelsValue("b"));
    labels.labels("b").inc(3);
    assertEquals(1.0, getLabelsValue("a").doubleValue(), .001);
    assertEquals(3.0, getLabelsValue("b").doubleValue(), .001);
  }

  @Test
  public void testCollect() {
    labels.labels("a").inc();
    List<Collector.MetricFamilySamples> mfs = labels.collect();
    
    ArrayList<Collector.MetricFamilySamples.Sample> samples = new ArrayList<Collector.MetricFamilySamples.Sample>();
    ArrayList<String> labelNames = new ArrayList<String>();
    labelNames.add("l");
    ArrayList<String> labelValues = new ArrayList<String>();
    labelValues.add("a");
    samples.add(new Collector.MetricFamilySamples.Sample("labels", labelNames, labelValues, 1.0));
    Collector.MetricFamilySamples mfsFixture = new Collector.MetricFamilySamples("labels", Collector.Type.GAUGE, "help", samples);

    assertEquals(1, mfs.size());
    assertEquals(mfsFixture, mfs.get(0));
  }

}

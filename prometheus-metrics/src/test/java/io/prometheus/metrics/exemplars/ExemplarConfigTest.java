package io.prometheus.metrics.exemplars;

import io.prometheus.metrics.Counter;
import io.prometheus.metrics.Histogram;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ExemplarConfigTest {

  private boolean origEnabled;
  private CounterExemplarSampler origCounterExemplarSampler;
  private HistogramExemplarSampler origHistogramExemplarSampler;

  private TestExemplarSampler defaultSampler;
  private TestExemplarSampler customSampler;

  @Before
  public void setUp() {
    origEnabled = ExemplarConfig.isExemplarsEnabled();
    origCounterExemplarSampler = ExemplarConfig.getCounterExemplarSampler();
    origHistogramExemplarSampler = ExemplarConfig.getHistogramExemplarSampler();

    defaultSampler = new TestExemplarSampler();
    customSampler = new TestExemplarSampler();
    ExemplarConfig.enableExemplars();
    ExemplarConfig.setCounterExemplarSampler(defaultSampler);
    ExemplarConfig.setHistogramExemplarSampler(defaultSampler);
  }

  @After
  public void tearDown() {
    ExemplarConfig.setCounterExemplarSampler(origCounterExemplarSampler);
    ExemplarConfig.setHistogramExemplarSampler(origHistogramExemplarSampler);
    if (origEnabled) {
      ExemplarConfig.enableExemplars();
    } else {
      ExemplarConfig.disableExemplars();
    }
  }

  private static class TestExemplarSampler implements ExemplarSampler {

    private boolean called = false;

    @Override
    public Exemplar sample(double increment, Exemplar previous) {
      called = true;
      return null;
    }

    @Override
    public Exemplar sample(double value, double bucketFrom, double bucketTo, Exemplar previous) {
      called = true;
      return null;
    }
  }

  @Test
  public void testCounterWithExemplarSampler() {
    Counter counter = Counter.build()
        .withExemplarSampler(customSampler)
        .name("test")
        .help("help")
        .create();
    counter.inc(3);
    Assert.assertTrue(customSampler.called);
    Assert.assertFalse(defaultSampler.called);
  }

  @Test
  public void testCounterWithoutExemplars() {
    Counter counter = Counter.build()
        .withoutExemplars()
        .name("test")
        .help("help")
        .create();
    counter.inc(3);
    Assert.assertFalse(customSampler.called);
    Assert.assertFalse(defaultSampler.called);
  }

  @Test
  public void testCounterWithExemplars() {
    ExemplarConfig.disableExemplars();
    Counter counter = Counter.build()
        .withExemplars()
        .name("test")
        .help("help")
        .create();
    counter.inc(3);
    Assert.assertFalse(customSampler.called);
    Assert.assertTrue(defaultSampler.called);
  }

  @Test
  public void testCounterDefaultDisabled() {
    ExemplarConfig.disableExemplars();
    Counter counter = Counter.build()
        .name("test")
        .help("help")
        .create();
    counter.inc(3);
    Assert.assertFalse(customSampler.called);
    Assert.assertFalse(defaultSampler.called);
  }

  @Test
  public void testCounterIncWithExemplar() {
    Counter counter = Counter.build()
        .name("test")
        .help("help")
        .create();
    counter.incWithExemplar(3);
    Assert.assertFalse(customSampler.called);
    Assert.assertFalse(defaultSampler.called);
  }

  @Test
  public void testHistogramWithExemplarSampler() {
    Histogram histogram = Histogram.build()
        .withExemplarSampler(customSampler)
        .name("test")
        .help("help")
        .create();
    histogram.observe(3);
    Assert.assertTrue(customSampler.called);
    Assert.assertFalse(defaultSampler.called);
  }

  @Test
  public void testHistogramWithoutExemplars() {
    Histogram histogram = Histogram.build()
        .withoutExemplars()
        .name("test")
        .help("help")
        .create();
    histogram.observe(3);
    Assert.assertFalse(customSampler.called);
    Assert.assertFalse(defaultSampler.called);
  }

  @Test
  public void testHistogramWithExemplars() {
    ExemplarConfig.disableExemplars();
    Histogram histogram = Histogram.build()
        .withExemplars()
        .name("test")
        .help("help")
        .create();
    histogram.observe(3);
    Assert.assertFalse(customSampler.called);
    Assert.assertTrue(defaultSampler.called);
  }

  @Test
  public void testHistogramDefaultDisabled() {
    ExemplarConfig.disableExemplars();
    Histogram histogram = Histogram.build()
        .name("test")
        .help("help")
        .create();
    histogram.observe(3);
    Assert.assertFalse(customSampler.called);
    Assert.assertFalse(defaultSampler.called);
  }
  @Test
  public void testHistogramObserveWithExemplar() {
    Histogram histogram = Histogram.build()
        .name("test")
        .help("help")
        .create();
    histogram.observeWithExemplar(3);
    Assert.assertFalse(customSampler.called);
    Assert.assertFalse(defaultSampler.called);
  }
}

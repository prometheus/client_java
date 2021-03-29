package io.prometheus.client.exemplars.impl;

import io.prometheus.client.exemplars.api.CounterExemplarSampler;
import io.prometheus.client.exemplars.api.Exemplar;
import io.prometheus.client.exemplars.api.HistogramExemplarSampler;
import io.prometheus.client.exemplars.api.SummaryExemplarSampler;
import io.prometheus.client.exemplars.api.Value;
import io.prometheus.client.exemplars.tracer.common.SpanContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.POSITIVE_INFINITY;

public class DefaultExemplarSamplerTest {

  final AtomicReference<String> traceId = new AtomicReference<String>();
  final AtomicReference<String> spanId = new AtomicReference<String>();
  final AtomicReference<Double> value = new AtomicReference<Double>();
  final AtomicLong timestamp = new AtomicLong();
  DefaultExemplarSampler defaultSampler;

  final SpanContext testContext = new SpanContext() {
    @Override
    public String getTraceId() {
      return traceId.get();
    }

    @Override
    public String getSpanId() {
      return spanId.get();
    }
  };

  final Value valueHolder = new Value() {
    @Override
    public double get() {
      return value.get();
    }
  };

  final DefaultExemplarSampler.Clock testClock = new DefaultExemplarSampler.Clock() {
    @Override
    public long currentTimeMillis() {
      return timestamp.get();
    }
  };

  @Before
  public void setUp() {
    traceId.set("trace-1");
    spanId.set("span-1");
    value.set(0.0);
    timestamp.set(System.currentTimeMillis());
    defaultSampler = new DefaultExemplarSampler(testContext, testClock);
  }

  @Test
  public void testCounterAndGauge() {
    CounterExemplarSampler sampler = defaultSampler;
    value.set(2.0);
    Exemplar first = new Exemplar(traceId.get(), spanId.get(), value.get(), timestamp.get());
    Assert.assertEquals(first, sampler.sample(valueHolder, null));
    value.set(3.0);
    traceId.set("trace-2");
    spanId.set("span-2");
    timestamp.getAndAdd(100); // 100ms later
    Exemplar second = new Exemplar(traceId.get(), spanId.get(), value.get(), timestamp.get());
    Assert.assertNull(sampler.sample(valueHolder, first)); // no new exemplar yet
    Assert.assertEquals(second, sampler.sample(valueHolder, null));
    timestamp.getAndAdd(10 * 1000); // 10s later
    value.set(4.0);
    traceId.set("trace-3");
    spanId.set("span-3");
    Exemplar third = new Exemplar(traceId.get(), spanId.get(), value.get(), timestamp.get());
    Assert.assertEquals(third, sampler.sample(valueHolder, second));
    traceId.set(null);
    Assert.assertNull(sampler.sample(valueHolder, null));
  }

  @Test
  public void testHistogram() {
    HistogramExemplarSampler sampler = defaultSampler;
    // Almost identical to Counter and Gauge, except that the value is passed directly and not via valueHolder.
    value.set(2.0);
    Exemplar first = new Exemplar(traceId.get(), spanId.get(), value.get(), timestamp.get());
    Assert.assertEquals(first, sampler.sample(valueHolder.get(), NEGATIVE_INFINITY, POSITIVE_INFINITY, null));
    value.set(3.0);
    traceId.set("trace-2");
    spanId.set("span-2");
    timestamp.getAndAdd(100); // 100ms later
    Exemplar second = new Exemplar(traceId.get(), spanId.get(), value.get(), timestamp.get());
    Assert.assertNull(sampler.sample(valueHolder.get(), NEGATIVE_INFINITY, POSITIVE_INFINITY, first)); // no new exemplar yet
    Assert.assertEquals(second, sampler.sample(valueHolder.get(), NEGATIVE_INFINITY, POSITIVE_INFINITY, null));
    timestamp.getAndAdd(10 * 1000); // 10s later
    value.set(4.0);
    traceId.set("trace-3");
    spanId.set("span-3");
    Exemplar third = new Exemplar(traceId.get(), spanId.get(), value.get(), timestamp.get());
    Assert.assertEquals(third, sampler.sample(valueHolder.get(), NEGATIVE_INFINITY, POSITIVE_INFINITY, second));
    traceId.set(null);
    Assert.assertNull(sampler.sample(valueHolder.get(), NEGATIVE_INFINITY, POSITIVE_INFINITY, null));
  }

  @Test
  public void testSummary() {
    SummaryExemplarSampler sampler = defaultSampler;
    value.set(3.0);
    Exemplar exemplar = sampler.sample(valueHolder.get());
    Assert.assertEquals(new Exemplar(traceId.get(), spanId.get(), value.get(), timestamp.get()), exemplar);
    traceId.set(null);
    Assert.assertNull(sampler.sample(4.0));
  }
}

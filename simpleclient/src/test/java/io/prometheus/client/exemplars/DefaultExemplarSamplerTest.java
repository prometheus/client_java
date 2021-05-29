package io.prometheus.client.exemplars;

import io.prometheus.client.exemplars.tracer.common.SpanContextSupplier;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.POSITIVE_INFINITY;

public class DefaultExemplarSamplerTest {

  private static final String SPAN_ID = "span_id";
  private static final String TRACE_ID = "trace_id";

  final AtomicReference<String> traceId = new AtomicReference<String>();
  final AtomicReference<String> spanId = new AtomicReference<String>();
  final AtomicLong timestamp = new AtomicLong();
  DefaultExemplarSampler defaultSampler;

  final SpanContextSupplier testContext = new SpanContextSupplier() {
    @Override
    public String getTraceId() {
      return traceId.get();
    }

    @Override
    public String getSpanId() {
      return spanId.get();
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
    timestamp.set(System.currentTimeMillis());
    defaultSampler = new DefaultExemplarSampler(testContext, testClock);
  }

  @Test
  public void testCounter() {
    CounterExemplarSampler sampler = defaultSampler;
    Exemplar first = new Exemplar(2.0, timestamp.get(), TRACE_ID, traceId.get(), SPAN_ID, spanId.get());
    Assert.assertEquals(first, sampler.sample(2.0, null));
    traceId.set("trace-2");
    spanId.set("span-2");
    timestamp.getAndAdd(100); // 100ms later
    Exemplar second = new Exemplar(3.0, timestamp.get(), TRACE_ID, traceId.get(), SPAN_ID, spanId.get());
    Assert.assertNull(sampler.sample(3.0, first)); // no new exemplar yet
    Assert.assertEquals(second, sampler.sample(3.0, null));
    timestamp.getAndAdd(10 * 1000); // 10s later
    traceId.set("trace-3");
    spanId.set("span-3");
    Exemplar third = new Exemplar(4.0, timestamp.get(), TRACE_ID, traceId.get(), SPAN_ID, spanId.get());
    Assert.assertEquals(third, sampler.sample(4.0, second));
    traceId.set(null);
    Assert.assertNull(sampler.sample(4.0, null));
  }

  @Test
  public void testHistogram() {
    HistogramExemplarSampler sampler = defaultSampler;
    // Almost identical to Counter and Gauge, except that the value is passed directly and not via valueHolder.
    Exemplar first = new Exemplar(2.0, timestamp.get(), TRACE_ID, traceId.get(), SPAN_ID, spanId.get());
    Assert.assertEquals(first, sampler.sample(2.0, NEGATIVE_INFINITY, POSITIVE_INFINITY, null));
    traceId.set("trace-2");
    spanId.set("span-2");
    timestamp.getAndAdd(100); // 100ms later
    Exemplar second = new Exemplar(3.0, timestamp.get(), TRACE_ID, traceId.get(), SPAN_ID, spanId.get());
    Assert.assertNull(sampler.sample(3.0, NEGATIVE_INFINITY, POSITIVE_INFINITY, first)); // no new exemplar yet
    Assert.assertEquals(second, sampler.sample(3.0, NEGATIVE_INFINITY, POSITIVE_INFINITY, null));
    timestamp.getAndAdd(10 * 1000); // 10s later
    traceId.set("trace-3");
    spanId.set("span-3");
    Exemplar third = new Exemplar(4.0, timestamp.get(), TRACE_ID, traceId.get(), SPAN_ID, spanId.get());
    Assert.assertEquals(third, sampler.sample(4.0, NEGATIVE_INFINITY, POSITIVE_INFINITY, second));
    traceId.set(null);
    Assert.assertNull(sampler.sample(4.0, NEGATIVE_INFINITY, POSITIVE_INFINITY, null));
  }
}

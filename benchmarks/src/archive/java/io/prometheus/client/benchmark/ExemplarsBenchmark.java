package io.prometheus.client.benchmark;

import io.prometheus.client.Counter;
import io.prometheus.client.exemplars.DefaultExemplarSampler;
import io.prometheus.client.exemplars.tracer.common.SpanContextSupplier;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class ExemplarsBenchmark {

  private Counter counter;
  private Counter counterWithExemplars;
  private Counter counterWithoutExemplars;

  @Setup
  public void setup() {

    counter = Counter.build()
        .name("counter_total")
        .help("Total number of requests.")
        .labelNames("path")
        .create();

    counterWithExemplars = Counter.build()
        .name("counter_with_exemplars_total")
        .help("Total number of requests.")
        .labelNames("path")
        .withExemplarSampler(new DefaultExemplarSampler(new MockSpanContextSupplier()))
        .create();

    counterWithoutExemplars = Counter.build()
        .name("counter_without_exemplars_total")
        .help("Total number of requests.")
        .labelNames("path")
        .withoutExemplars()
        .create();
  }

  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void testCounter() {
    counter.labels("test").inc();
  }

  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void testCounterWithExemplars() {
    counterWithExemplars.labels("test").inc();
  }

  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void testCounterWithoutExemplars() {
    counterWithoutExemplars.labels("test").inc();
  }

  private static class MockSpanContextSupplier implements SpanContextSupplier {

    @Override
    public String getTraceId() {
      return "trace-id";
    }

    @Override
    public String getSpanId() {
      return "span-id";
    }

    @Override
    public boolean isSampled() {
        return true;
    }
  }
}

package io.prometheus.benchmark;

import com.codahale.metrics.MetricRegistry;

import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Benchmark)
public class CounterBenchmark {

  MetricRegistry registry;
  com.codahale.metrics.Counter codahaleCounter;
  com.codahale.metrics.Meter codahaleMeter;

  io.prometheus.client.metrics.Counter prometheusCounter;
  io.prometheus.client.metrics.Counter.Child prometheusCounterChild;
  io.prometheus.client.Counter prometheusSimpleCounter;
  io.prometheus.client.Counter.Child prometheusSimpleCounterChild;
  io.prometheus.client.Counter prometheusSimpleCounterNoLabels;

  @Setup
  public void setup() {
    prometheusCounter = io.prometheus.client.metrics.Counter.newBuilder()
      .name("name")
      .documentation("some description..")
      .build();
    prometheusCounterChild = prometheusCounter.newPartial().apply();

    prometheusSimpleCounter = io.prometheus.client.Counter.build()
      .name("name")
      .help("some description..")
      .labelNames("some", "group").create();
    prometheusSimpleCounterChild = prometheusSimpleCounter.labels("test", "group");

    prometheusSimpleCounterNoLabels = io.prometheus.client.Counter.build()
      .name("name")
      .help("some description..")
      .create();

    registry = new MetricRegistry();
    codahaleCounter = registry.counter("counter");
    codahaleMeter = registry.meter("meter");
  }

  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void prometheusCounterIncBenchmark() {
    prometheusCounter.newPartial().apply().increment();
  }

  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void prometheusCounterChildIncBenchmark() {
    prometheusCounterChild.increment();
  }

  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void prometheusSimpleCounterIncBenchmark() {
    prometheusSimpleCounter.labels("test", "group").inc(); 
  }
  
  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void prometheusSimpleCounterChildIncBenchmark() {
    prometheusSimpleCounterChild.inc(); 
  }

  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void prometheusSimpleCounterNoLabelsIncBenchmark() {
    prometheusSimpleCounterNoLabels.inc(); 
  }

  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void codahaleCounterIncBenchmark() {
    codahaleCounter.inc();
  }

  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void codahaleMeterMarkBenchmark() {
    codahaleMeter.mark();
  }

  public static void main(String[] args) throws RunnerException {

    Options opt = new OptionsBuilder()
      .include(CounterBenchmark.class.getSimpleName())
      .warmupIterations(5)
      .measurementIterations(4)
      .threads(4)
      .forks(1)
      .build();

    new Runner(opt).run();
  }
}

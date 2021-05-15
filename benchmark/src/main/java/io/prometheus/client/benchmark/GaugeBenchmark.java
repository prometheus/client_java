package io.prometheus.client.benchmark;

import com.codahale.metrics.MetricRegistry;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class GaugeBenchmark {

  MetricRegistry registry;
  com.codahale.metrics.Counter codahaleCounter;

  io.prometheus.client.Gauge prometheusSimpleGauge;
  io.prometheus.client.Gauge.Child prometheusSimpleGaugeChild;
  io.prometheus.client.Gauge prometheusSimpleGaugeNoLabels;

  @Setup
  public void setup() {
    prometheusSimpleGauge = io.prometheus.client.Gauge.build()
      .name("name")
      .help("some description..")
      .labelNames("some", "group").create();
    prometheusSimpleGaugeChild = prometheusSimpleGauge.labels("test", "group");

    prometheusSimpleGaugeNoLabels = io.prometheus.client.Gauge.build()
      .name("name")
      .help("some description..")
      .create();

    registry = new MetricRegistry();
    codahaleCounter = registry.counter("name");
  }

  // Increment.
  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void prometheusSimpleGaugeIncBenchmark() {
    prometheusSimpleGauge.labels("test", "group").inc(); 
  }
  
  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void prometheusSimpleGaugeChildIncBenchmark() {
    prometheusSimpleGaugeChild.inc(); 
  }

  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void prometheusSimpleGaugeNoLabelsIncBenchmark() {
    prometheusSimpleGaugeNoLabels.inc(); 
  }

  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void codahaleCounterIncBenchmark() {
    codahaleCounter.inc();
  }


  // Decrement.
  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void prometheusSimpleGaugeDecBenchmark() {
    prometheusSimpleGauge.labels("test", "group").dec(); 
  }
  
  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void prometheusSimpleGaugeChildDecBenchmark() {
    prometheusSimpleGaugeChild.dec(); 
  }

  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void prometheusSimpleGaugeNoLabelsDecBenchmark() {
    prometheusSimpleGaugeNoLabels.dec(); 
  }

  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void codahaleCounterDecBenchmark() {
    codahaleCounter.dec();
  }

  // Set.
  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void prometheusSimpleGaugeSetBenchmark() {
    prometheusSimpleGauge.labels("test", "group").set(42); 
  }
  
  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void prometheusSimpleGaugeChildSetBenchmark() {
    prometheusSimpleGaugeChild.set(42);
  }

  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void prometheusSimpleGaugeNoLabelsSetBenchmark() {
    prometheusSimpleGaugeNoLabels.set(42); 
  }

  public static void main(String[] args) throws RunnerException {

    Options opt = new OptionsBuilder()
      .include(GaugeBenchmark.class.getSimpleName())
      .warmupIterations(5)
      .measurementIterations(4)
      .threads(4)
      .forks(1)
      .build();

    new Runner(opt).run();
  }
}

package io.prometheus.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class HdrSummaryBenchmark {

  io.prometheus.client.HdrSummary prometheusSimpleHdrSummary;
  io.prometheus.client.HdrSummary.Child prometheusSimpleHdrSummaryChild;
  io.prometheus.client.HdrSummary prometheusSimpleHdrSummaryNoLabels;
  io.prometheus.client.HdrSummary prometheusSimpleHdrSummaryQuantiles;
  io.prometheus.client.HdrSummary.Child prometheusSimpleHdrSummaryQuantilesChild;
  io.prometheus.client.HdrSummary prometheusSimpleHdrSummaryQuantilesNoLabels;

  @Setup
  public void setup() {
    prometheusSimpleHdrSummary = io.prometheus.client.HdrSummary.build()
      .name("name")
      .help("some description..")
      .labelNames("some", "group").create();
    prometheusSimpleHdrSummaryChild = prometheusSimpleHdrSummary.labels("test", "group");

    prometheusSimpleHdrSummaryNoLabels = io.prometheus.client.HdrSummary.build()
      .name("name")
      .help("some description..")
      .create();

    prometheusSimpleHdrSummaryQuantiles = io.prometheus.client.HdrSummary.build()
      .name("name")
      .help("some description..")
      .quantile(0.5).quantile(0.9).quantile(0.95).quantile(0.99)
      .labelNames("some", "group").create();
    prometheusSimpleHdrSummaryQuantilesChild = prometheusSimpleHdrSummaryQuantiles.labels("test", "group");

    prometheusSimpleHdrSummaryQuantilesNoLabels = io.prometheus.client.HdrSummary.build()
      .name("name")
      .help("some description..")
      .quantile(0.5).quantile(0.9).quantile(0.95).quantile(0.99)
      .create();
  }

  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void prometheusSimpleHdrSummaryBenchmark() {
    prometheusSimpleHdrSummary.labels("test", "group").observe(1) ;
  }

  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void prometheusSimpleHdrSummaryChildBenchmark() {
    prometheusSimpleHdrSummaryChild.observe(1);
  }

  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void prometheusSimpleHdrSummaryNoLabelsBenchmark() {
    prometheusSimpleHdrSummaryNoLabels.observe(1);
  }

  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void prometheusSimpleHdrSummaryQuantilesBenchmark() {
    prometheusSimpleHdrSummaryQuantiles.labels("test", "group").observe(1) ;
  }

  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void prometheusSimpleHdrSummaryQuantilesChildBenchmark() {
    prometheusSimpleHdrSummaryQuantilesChild.observe(1);
  }

  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void prometheusSimpleHdrSummaryQuantilesNoLabelsBenchmark() {
    prometheusSimpleHdrSummaryQuantilesNoLabels.observe(1);
  }

  public static void main(String[] args) throws RunnerException {

    Options opt = new OptionsBuilder()
      .include(HdrSummaryBenchmark.class.getSimpleName())
      .warmupIterations(5)
      .measurementIterations(4)
      .threads(4)
      .forks(1)
      .build();

    new Runner(opt).run();
  }

}

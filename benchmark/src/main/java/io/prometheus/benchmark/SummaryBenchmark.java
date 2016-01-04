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
public class SummaryBenchmark {

  MetricRegistry registry;
  com.codahale.metrics.Histogram codahaleHistogram;

  io.prometheus.client.metrics.Summary prometheusSummary;
  io.prometheus.client.metrics.Summary.Child prometheusSummaryChild;
  io.prometheus.client.Summary prometheusSimpleSummary;
  io.prometheus.client.Summary.Child prometheusSimpleSummaryChild;
  io.prometheus.client.Summary prometheusSimpleSummaryNoLabels;
  io.prometheus.client.Summary prometheusQuantileSummary;
  io.prometheus.client.Summary.Child prometheusQuantileSummaryChild;
  io.prometheus.client.Histogram prometheusSimpleHistogram;
  io.prometheus.client.Histogram.Child prometheusSimpleHistogramChild;
  io.prometheus.client.Histogram prometheusSimpleHistogramNoLabels;

  @Setup
  public void setup() {
    prometheusSummary = io.prometheus.client.metrics.Summary.newBuilder()
      .name("name")
      .documentation("some description..")
      .build();
    prometheusSummaryChild = prometheusSummary.newPartial().apply();

    prometheusSimpleSummary = io.prometheus.client.Summary.build()
      .name("name")
      .help("some description..")
      .labelNames("some", "group").create();
    prometheusSimpleSummaryChild = prometheusSimpleSummary.labels("test", "group");

    prometheusSimpleSummaryNoLabels = io.prometheus.client.Summary.build()
      .name("name")
      .help("some description..")
      .create();

   /*  prometheusQuantileSummary = io.prometheus.client.Summary.build()
           .name("name")
            .help("quantiles computation help")
            .quantiles(1024, 0.25)
            .labelNames("some", "group")
            .create();
    prometheusQuantileSummaryChild = prometheusQuantileSummary.labels("test", "group2");
    */

    prometheusSimpleHistogram = io.prometheus.client.Histogram.build()
      .name("name")
      .help("some description..")
      .labelNames("some", "group").create();
    prometheusSimpleHistogramChild = prometheusSimpleHistogram.labels("test", "group");

    prometheusSimpleHistogramNoLabels = io.prometheus.client.Histogram.build()
      .name("name")
      .help("some description..")
      .create();

    registry = new MetricRegistry();
    codahaleHistogram = registry.histogram("name");
  }

  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void prometheusSummaryBenchmark() {
    prometheusSummary.newPartial().apply().observe(1.0);
  }

  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void prometheusSummaryChildBenchmark() {
    prometheusSummaryChild.observe(1.0);
  }

  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void prometheusSimpleSummaryBenchmark() {
    prometheusSimpleSummary.labels("test", "group").observe(1) ;
  }

  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void prometheusSimpleSummaryChildBenchmark() {
    prometheusSimpleSummaryChild.observe(1); 
  }

  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void prometheusQuantileSummaryChildBenchmark() {
    prometheusQuantileSummaryChild.observe(1);
  }


  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void prometheusSimpleSummaryNoLabelsBenchmark() {
    prometheusSimpleSummaryNoLabels.observe(1);
  }

  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void prometheusSimpleHistogramBenchmark() {
    prometheusSimpleHistogram.labels("test", "group").observe(1) ;
  }

  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void prometheusSimpleHistogramChildBenchmark() {
    prometheusSimpleHistogramChild.observe(1);
  }

  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void prometheusSimpleHistogramNoLabelsBenchmark() {
    prometheusSimpleHistogramNoLabels.observe(1);
  }

  @Benchmark
  @BenchmarkMode({Mode.AverageTime})
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void codahaleHistogramBenchmark() {
    codahaleHistogram.update(1);
  }

  public static void main(String[] args) throws RunnerException {

    Options opt = new OptionsBuilder()
      .include(SummaryBenchmark.class.getSimpleName())
      .warmupIterations(5)
      .measurementIterations(4)
      .threads(4)
      .forks(1)
      .build();

    new Runner(opt).run();
  }
}

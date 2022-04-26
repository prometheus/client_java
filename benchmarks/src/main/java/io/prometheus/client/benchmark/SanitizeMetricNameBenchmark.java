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
import org.openjdk.jmh.runner.options.TimeValue;

import io.prometheus.client.Collector;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class SanitizeMetricNameBenchmark {

  @Benchmark
  @BenchmarkMode({ Mode.AverageTime })
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void sanitizeSanitizedName() {
    Collector.sanitizeMetricName("good_name");
  }

  @Benchmark
  @BenchmarkMode({ Mode.AverageTime })
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void sanitizeNonSanitizedName() {
    Collector.sanitizeMetricName("9not_good_name!");
  }

  public static void main(String[] args) throws RunnerException {

    Options opt = new OptionsBuilder()
        .include(SanitizeMetricNameBenchmark.class.getSimpleName())
        .warmupIterations(5)
        .measurementIterations(4)
        .measurementTime(TimeValue.seconds(1))
        .warmupTime(TimeValue.seconds(1))
        .threads(4)
        .forks(1)
        .build();

    new Runner(opt).run();
  }
}

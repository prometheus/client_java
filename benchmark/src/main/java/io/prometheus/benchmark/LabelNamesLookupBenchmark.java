package io.prometheus.benchmark;

import io.prometheus.client.Histogram;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class LabelNamesLookupBenchmark {

    @Param({"1","2"})
    public int labelNamesCount;

    String[] labelNames;
    io.prometheus.client.Histogram prometheusSimpleHistogram;

    @Setup
    public void setup() {
        final String baseLabelName = "label";
        labelNames = new String[labelNamesCount];
        for (int i = 0; i< labelNamesCount; i++){
            labelNames[i] = baseLabelName + '_' + i;
        }
        prometheusSimpleHistogram = io.prometheus.client.Histogram.build()
                .name("name")
                .help("some description..")
                .labelNames(labelNames).create();
        if (labelNamesCount == 1) {
            prometheusSimpleHistogram.labels(labelNames[0]);
        } else {
            prometheusSimpleHistogram.labels(labelNames);
        }
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public Histogram.Child labelNamesLookupBenchmark() {
        if (labelNamesCount == 1) {
            return prometheusSimpleHistogram.labels(labelNames[0]);
        } else {
            return prometheusSimpleHistogram.labels(labelNames);
        }
    }

    public static void main(String[] args) throws RunnerException {

        Options opt = new OptionsBuilder()
                .include(LabelNamesLookupBenchmark.class.getSimpleName())
                .jvmArgs("-XX:+UseBiasedLocking", "-XX:BiasedLockingStartupDelay=0")
                .addProfiler(GCProfiler.class)
                .warmupIterations(5)
                .measurementIterations(4)
                .threads(1)
                .forks(2)
                .build();

        new Runner(opt).run();
    }
}

package io.prometheus.client;

import io.prometheus.client.CKMSQuantiles.Quantile;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class CKMSQuantileBenchmark {

    @State(Scope.Benchmark)
    public static class EmptyBenchmarkState {
        @Param({"10000", "100000", "1000000"})
        public int value;

        CKMSQuantiles ckmsQuantiles;

        List<Quantile> quantiles;
        Random rand = new Random(0);

        long[] shuffle;

        @Setup(Level.Trial)
        public void setup() {
            quantiles = new ArrayList<Quantile>();
            quantiles.add(new Quantile(0.50, 0.050));
            quantiles.add(new Quantile(0.90, 0.010));
            quantiles.add(new Quantile(0.95, 0.005));
            quantiles.add(new Quantile(0.99, 0.001));


            shuffle = new long[value];
            for (int i = 0; i < shuffle.length; i++) {
                shuffle[i] = i;
            }
            Collections.shuffle(Arrays.asList(shuffle), rand);

        }
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void ckmsQuantileInsertBenchmark(Blackhole blackhole, EmptyBenchmarkState state) {
        CKMSQuantiles q = new CKMSQuantiles(state.quantiles.toArray(new Quantile[]{}));
        for (long l : state.shuffle) {
            q.insert(l);
        }
    }

    @State(Scope.Benchmark)
    public static class PrefilledBenchmarkState {
        public int value = 1000000;

        CKMSQuantiles ckmsQuantiles;

        List<Quantile> quantiles;
        Random rand = new Random(0);

        long[] shuffle;

        @Setup(Level.Trial)
        public void setup() {
            quantiles = new ArrayList<Quantile>();
            quantiles.add(new Quantile(0.50, 0.050));
            quantiles.add(new Quantile(0.90, 0.010));
            quantiles.add(new Quantile(0.95, 0.005));
            quantiles.add(new Quantile(0.99, 0.001));


            shuffle = new long[value];
            for (int i = 0; i < shuffle.length; i++) {
                shuffle[i] = i;
            }
            Collections.shuffle(Arrays.asList(shuffle), rand);
            ckmsQuantiles = new CKMSQuantiles(quantiles.toArray(new Quantile[]{}));
            for (long l : shuffle) {
                ckmsQuantiles.insert(l);
            }

        }
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void ckmsQuantileGetBenchmark(Blackhole blackhole, PrefilledBenchmarkState state) {
        blackhole.consume(state.ckmsQuantiles.get(0.95));
    }

    public static void main(String[] args) throws RunnerException {

        Options opt = new OptionsBuilder()
                .include(CKMSQuantileBenchmark.class.getSimpleName())
                .warmupIterations(5)
                .measurementIterations(4)
                .threads(1)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}

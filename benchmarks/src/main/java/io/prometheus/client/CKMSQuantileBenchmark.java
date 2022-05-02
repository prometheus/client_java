package io.prometheus.client;

import io.prometheus.client.CKMSQuantiles.Quantile;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class CKMSQuantileBenchmark {

    @State(Scope.Benchmark)
    public static class EmptyBenchmarkState {
        @Param({"10000", "100000", "1000000"})
        public int value;

        List<Quantile> quantiles;
        Random rand = new Random(0);

        List<Double> shuffle;

        Quantile mean = new Quantile(0.50, 0.050);
        Quantile q90 = new Quantile(0.90, 0.010);
        Quantile q95 = new Quantile(0.95, 0.005);
        Quantile q99 = new Quantile(0.99, 0.001);

        @Setup(Level.Trial)
        public void setup() {
            quantiles = new ArrayList<Quantile>();
            quantiles.add(mean);
            quantiles.add(q90);
            quantiles.add(q95);
            quantiles.add(q99);

            shuffle = new ArrayList<Double>(value);
            for (int i = 0; i < value; i++) {
                shuffle.add((double) i);
            }
            Collections.shuffle(shuffle, rand);
        }
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void ckmsQuantileInsertBenchmark(EmptyBenchmarkState state) {
        CKMSQuantiles q = new CKMSQuantiles(state.quantiles.toArray(new Quantile[]{}));
        for (Double l : state.shuffle) {
            q.insert(l);
        }
    }

    /** prefilled benchmark, means that we already have a filled and compressed samples available */
    @State(Scope.Benchmark)
    public static class PrefilledBenchmarkState {
        @Param({"10000", "100000", "1000000"})
        public int value;


        CKMSQuantiles ckmsQuantiles;

        List<Quantile> quantiles;
        Random rand = new Random(0);

        Quantile mean = new Quantile(0.50, 0.050);
        Quantile q90 = new Quantile(0.90, 0.010);
        Quantile q95 = new Quantile(0.95, 0.005);
        Quantile q99 = new Quantile(0.99, 0.001);
        List<Double> shuffle;

        int rank = (int) (value * q95.quantile);


        @Setup(Level.Trial)
        public void setup() {
            quantiles = new ArrayList<Quantile>();
            quantiles.add(mean);
            quantiles.add(q90);
            quantiles.add(q95);
            quantiles.add(q99);

            shuffle = new ArrayList<Double>(value);
            for (int i = 0; i < value; i++) {
                shuffle.add((double) i);
            }
            Collections.shuffle(shuffle, rand);


            ckmsQuantiles = new CKMSQuantiles(quantiles.toArray(new Quantile[]{}));
            for (Double l : shuffle) {
                ckmsQuantiles.insert(l);
            }
            // make sure we inserted all 'hanging' samples (count % 128)
            ckmsQuantiles.get(0);
            // compress everything so we have a similar samples size regardless of n.
            ckmsQuantiles.compress();
            System.out.println("Sample size is: " + ckmsQuantiles.samples.size());
        }

    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void ckmsQuantileGetBenchmark(Blackhole blackhole, PrefilledBenchmarkState state) {
        blackhole.consume(state.ckmsQuantiles.get(state.q90.quantile));
    }

    /**
     * benchmark for the f method.
     */
    @Benchmark
    @BenchmarkMode({Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void ckmsQuantileF(Blackhole blackhole, PrefilledBenchmarkState state) {
        blackhole.consume(state.ckmsQuantiles.f(state.rank));
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

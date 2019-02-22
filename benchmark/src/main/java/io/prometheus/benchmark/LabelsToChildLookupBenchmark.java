package io.prometheus.benchmark;

import io.prometheus.client.Counter;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 1500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(2)
public class LabelsToChildLookupBenchmark {

    private static final String LABEL1 = "label1", LABEL2 = "label2", LABEL3 = "label3";
    private static final String LABEL4 = "label4", LABEL5 = "label5";
 
    private Counter noLabelsCollector, oneLabelCollector, twoLabelsCollector, threeLabelsCollector;
    private Counter fourLabelsCollector, fiveLabelsCollector;

    @Setup
    public void setup() {
        Counter.Builder builder = new Counter.Builder().name("testCollector").help("testHelp");
        noLabelsCollector = builder.create();
        oneLabelCollector = builder.labelNames("name1").create();
        twoLabelsCollector = builder.labelNames("name1", "name2").create();
        threeLabelsCollector = builder.labelNames("name1", "name2", "name3").create();
        fourLabelsCollector = builder.labelNames("name1", "name2", "name3", "name4").create();
        fiveLabelsCollector = builder.labelNames("name1", "name2", "name3", "name4", "name5").create();
    }

    @Benchmark
    public void baseline(LabelsToChildLookupBenchmark state) {
        noLabelsCollector.inc();
    }

    @Benchmark
    public void oneLabel(LabelsToChildLookupBenchmark state) {
        oneLabelCollector.labels(LABEL1).inc();
    }

    @Benchmark
    public void twoLabels(LabelsToChildLookupBenchmark state) {
        twoLabelsCollector.labels(LABEL1, LABEL2).inc();
    }

    @Benchmark
    public void threeLabels(LabelsToChildLookupBenchmark state) {
        threeLabelsCollector.labels(LABEL1, LABEL2, LABEL3).inc();
    }

    @Benchmark
    public void fourLabels(LabelsToChildLookupBenchmark state) {
        fourLabelsCollector.labels(LABEL1, LABEL2, LABEL3, LABEL4).inc();
    }

    @Benchmark
    public void fiveLabels(LabelsToChildLookupBenchmark state) {
        fiveLabelsCollector.labels(LABEL1, LABEL2, LABEL3, LABEL4, LABEL5).inc();
    }

    public static void main(String[] args) throws RunnerException {
        new Runner(new OptionsBuilder()
                .include(LabelsToChildLookupBenchmark.class.getSimpleName())
                .build()).run();
    }
}
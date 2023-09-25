package io.prometheus.metrics.benchmarks;

import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.util.Random;

@State(Scope.Thread)
public class RandomNumbers {

    final double[] randomNumbers = new double[10*1024];

    public RandomNumbers() {
        Random rand = new Random(0);
        for (int i = 0; i < randomNumbers.length; i++) {
            randomNumbers[i] = Math.abs(rand.nextGaussian());
        }
    }
}

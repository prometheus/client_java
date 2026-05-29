package io.prometheus.metrics.benchmarks;

import java.util.Random;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@State(Scope.Thread)
public class RandomNumbers {

  final double[] randomNumbers = new double[10 * 1024];

  public RandomNumbers() {
    Random rand = new Random(0);
    for (int i = 0; i < randomNumbers.length; i++) {
      randomNumbers[i] = Math.abs(rand.nextGaussian());
    }
  }
}

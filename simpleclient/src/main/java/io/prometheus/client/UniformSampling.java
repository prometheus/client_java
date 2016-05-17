package io.prometheus.client;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An algorithm for selecting a (fixed-size) random sample of elements
 * (with replacement) from a stream whose size is unknown beforehand.
 *
 * @author Cristian Greco
 */
class UniformSampling {

  // the fixed size of the random sample
  private final int size;

  // a prng to generate uniformly distributed values
  private final Random rand;

  // the number of elements observed so far
  private final AtomicLong count;

  // the candidate elements of the random sample
  private final AtomicDoubleArray values;

  public UniformSampling() {
    this(1024);
  }

  public UniformSampling(int size) {
    this(size, new Random());
  }

  public UniformSampling(int size, Random rand) {
    this.size = size;
    this.rand = rand;
    this.count = new AtomicLong(0);
    this.values = new AtomicDoubleArray(size);
  }

  /**
   * Process a new element.
   * The first {@code size} elements observed are
   * immediately elected as candidates for the random
   * sample. Then, each subsequent observed element gets
   * a chance to replace an existing candidate.
   *
   * @param value the observed element
   */
  public void add(double value) {
    final int c = (int) count.incrementAndGet();
    if (c <= size) {
      values.set(c - 1, value);
    } else {
      final int m = (int) (c * rand.nextDouble()); // 0 <= m <= c-1
      if (m < size) {
        values.set(m, value);
      }
    }
  }

  /**
   * Return the array of elements which form a random
   * sample of all the elements observed so far.
   * The length of the returned array is at most {@code size}.
   *
   * @return the random sample of values
   */
  public double[] getValues() {
    final long c = count.get();
    final int intSize = c >= size ? size : (int) c;

    final double[] sample = new double[intSize];
    for (int i = 0; i < intSize; i++) {
      sample[i] = values.get(i);
    }
    return sample;
  }

}

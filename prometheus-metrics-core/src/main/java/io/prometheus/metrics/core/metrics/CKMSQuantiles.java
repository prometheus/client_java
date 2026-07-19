package io.prometheus.metrics.core.metrics;

// The original implementation was copied from
// https://raw.githubusercontent.com/Netflix/ocelli/master/ocelli-core/src/main/java/netflix/ocelli/stats/CKMSQuantiles.java
// Revision d0357b8bf5c17a173ce94d6b26823775b3f999f6 from Jan 21, 2015.
// However, it has been heavily refactored in the meantime.

/*
Copyright 2012 Andrew Wang (andrew@umbrant.com)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

// CHECKSTYLE:OFF: checkstyle

/**
 * Algorithm solving the "Targeted Quantile Problem" as described in "Effective Computation of
 * Biased Quantiles over Data Streams" by Cormode, Korn, Muthukrishnan, and Srivastava.
 */
final class CKMSQuantiles {

  @SuppressWarnings("ReferenceEquality")
  private static boolean sameObject(Object left, Object right) {
    return left == right;
  }

  final Quantile[] quantiles;

  /** Total number of observations (not including those that are still in the buffer). */
  int n = 0;

  /** List of sampled observations, ordered by Sample.value. */
  @SuppressWarnings("JdkObsolete")
  final LinkedList<Sample> samples = new LinkedList<>();

  /**
   * Compress is called every compressInterval inserts. Note that the buffer is flushed whenever
   * get() is called, so we cannot just wait until the buffer is full before we call compress.
   */
  private final int compressInterval = 128;

  private int insertsSinceLastCompress = 0;

  /**
   * Note that the buffer size could as well be less than the compressInterval. However, the buffer
   * size should not be greater than the compressInterval, because the compressInterval is not
   * respected in flush(), so if you want to compress more often than calling flush() that won't
   * work.
   */
  private final double[] buffer = new double[compressInterval];

  private int bufferPos = 0;

  public CKMSQuantiles(Quantile... quantiles) {
    if (quantiles.length == 0) {
      throw new IllegalArgumentException("quantiles cannot be empty");
    }
    this.quantiles = quantiles;
  }

  /** Add an observed value */
  public void insert(double value) {
    buffer[bufferPos++] = value;

    if (bufferPos == buffer.length) {
      flush();
    }

    if (++insertsSinceLastCompress == compressInterval) {
      compress();
      insertsSinceLastCompress = 0;
    }
  }

  private void flush() {
    Arrays.sort(buffer, 0, bufferPos);
    insertBatch(buffer, bufferPos);
    bufferPos = 0;
  }

  /** Inserts the elements from index 0 to index toIndex from the sortedBuffer. */
  void insertBatch(double[] sortedBuffer, int toIndex) {
    if (toIndex == 0) {
      return;
    }
    ListIterator<Sample> iterator = samples.listIterator();
    int i = 0; // position in buffer
    int r = 0; // sum of g's left of the current sample
    while (iterator.hasNext() && i < toIndex) {
      Sample item = iterator.next();
      while (i < toIndex) {
        if (sortedBuffer[i] > item.value) {
          break;
        }
        insertBefore(iterator, sortedBuffer[i], r);
        r++; // new item with g=1 was inserted before, so increment r
        i++;
        n++;
      }
      r += item.g;
    }
    while (i < toIndex) {
      samples.add(new Sample(sortedBuffer[i], 0));
      i++;
      n++;
    }
  }

  private void insertBefore(ListIterator<Sample> iterator, double value, int r) {
    if (!iterator.hasPrevious()) {
      samples.addFirst(new Sample(value, 0));
    } else {
      iterator.previous();
      iterator.add(new Sample(value, f(r) - 1));
      iterator.next();
    }
  }

  /** Get the estimated value at the specified quantile. */
  public double get(double q) {
    flush();

    if (samples.isEmpty()) {
      return Double.NaN;
    }

    if (q == 0.0) {
      return samples.getFirst().value;
    }

    if (q == 1.0) {
      return samples.getLast().value;
    }

    int desiredRank = (int) Math.ceil(q * n);

    // Return the sample whose possible-rank interval [rMin, rMax] is centered closest to the
    // desired rank. rMin is the sum of the g's up to and including the sample, rMax is rMin+delta.
    //
    // The previous implementation stopped at the first sample whose maximum rank
    // (r + g + delta) exceeded desiredRank + f(desiredRank)/2 and returned the preceding sample.
    // That rule only works while g + delta stays small for all samples up to the target rank.
    // For a targeted quantile with 2*epsilon >= 1-quantile the error function allows delta of
    // order n at low ranks, so a freshly inserted low-rank sample (delta = f(r) - 1) makes the
    // scan stop far too early and return a value near the minimum observation. See issue #2292.
    Sample result = samples.getFirst();
    double smallestDistance = Double.POSITIVE_INFINITY;
    int r = 0; // sum of g's left of the current sample
    for (Sample sample : samples) {
      int rMin = r + sample.g;
      double center = rMin + sample.delta / 2.0;
      double distance = Math.abs(center - desiredRank);
      if (distance < smallestDistance) {
        smallestDistance = distance;
        result = sample;
      }
      r += sample.g;
    }
    return result.value;
  }

  /** Error function, as in definition 5 of the paper. */
  int f(int r) {
    return f(r, r);
  }

  /**
   * Minimum of the error function {@link #f(int)} over the rank interval {@code [lo, hi]}.
   *
   * <p>Each targeted quantile contributes a V-shaped error curve whose global minimum {@code
   * 2*epsilon*n} is at rank {@code quantile*n}, so the minimum over the interval is reached at an
   * interior target (when the interval straddles it) or otherwise at the endpoint nearest the
   * target. {@link #compress()} uses this so that a merged sample can never span a target
   * quantile's rank with a width coarser than that quantile's accuracy window (see issue #2292).
   * With {@code lo == hi} this reduces to the plain error function at a single rank.
   */
  int f(int lo, int hi) {
    int minResult = Integer.MAX_VALUE;
    for (Quantile q : quantiles) {
      if (q.quantile == 0 || q.quantile == 1) {
        continue;
      }
      double target = q.quantile * n;
      int result;
      if (target > lo && target < hi) {
        // The interval straddles this quantile's target rank: use its tightest tolerance.
        result = (int) (2.0 * q.epsilon * n + 0.00000000001);
      } else {
        int probe = target <= lo ? lo : hi;
        // We had a numerical error here with the following example:
        // quantile = 0.95, epsilon = 0.01, (n-r) = 30.
        // The expected result of (2*0.01*30)/(1-0.95) is 12. The actual result is
        // 11.99999999999999. To avoid running into these types of error we add 0.00000000001
        // before rounding down.
        if (probe >= q.quantile * n) {
          result = (int) (q.v * probe + 0.00000000001);
        } else {
          result = (int) (q.u * (n - probe) + 0.00000000001);
        }
      }
      if (result < minResult) {
        minResult = result;
      }
    }
    return Math.max(minResult, 1);
  }

  /** Merge pairs of consecutive samples if this doesn't violate the error function. */
  void compress() {
    if (samples.size() < 3) {
      return;
    }
    Iterator<Sample> descendingIterator = samples.descendingIterator();
    int r = n; // n is equal to the sum of the g's of all samples

    Sample right;
    Sample left = descendingIterator.next();
    r -= left.g;

    while (descendingIterator.hasNext()) {
      right = left;
      left = descendingIterator.next();
      r = r - left.g;
      if (sameObject(left, samples.getFirst())) {
        // The min sample must never be merged.
        break;
      }
      // The merged sample would span the ranks [r, r + left.g + right.g + right.delta]. Bounding
      // the merge by the error function over that whole interval (rather than only at its left
      // edge r) prevents a single sample from spanning across the accuracy window of a target
      // quantile, which used to collapse the sketch when 2*epsilon >= 1-quantile (see issue #2292).
      if (left.g + right.g + right.delta < f(r, r + left.g + right.g + right.delta)) {
        right.g += left.g;
        descendingIterator.remove();
        left = right;
      }
    }
  }

  static class Sample {

    /** Observed value. */
    final double value;

    /**
     * Difference between the lowest possible rank of this sample and its predecessor. This always
     * starts with 1, but will be updated when compress() merges Samples.
     */
    int g = 1;

    /**
     * Difference between the greatest possible rank of this sample and the lowest possible rank of
     * this sample.
     */
    final int delta;

    Sample(double value, int delta) {
      this.value = value;
      this.delta = delta;
    }

    @Override
    public String toString() {
      return String.format("Sample{val=%.3f, g=%d, delta=%d}", value, g, delta);
    }
  }

  static class Quantile {

    /** Quantile. Must be between 0 and 1. */
    final double quantile;

    /** Allowed error. Must be between 0 and 1. */
    final double epsilon;

    /** Helper used in the error function f(), see definition 5 in the paper. */
    final double u;

    /** Helper used in the error function f(), see definition 5 in the paper. */
    final double v;

    Quantile(double quantile, double epsilon) {
      if (quantile < 0.0 || quantile > 1.0)
        throw new IllegalArgumentException("Quantile must be between 0 and 1");
      if (epsilon < 0.0 || epsilon > 1.0)
        throw new IllegalArgumentException("Epsilon must be between 0 and 1");

      this.quantile = quantile;
      this.epsilon = epsilon;
      u = 2.0 * epsilon / (1.0 - quantile); // if quantile == 1 this will be Double.NaN
      v = 2.0 * epsilon / quantile; // if quantile == 0 this will be Double.NaN
    }

    @Override
    public String toString() {
      return String.format("Quantile{q=%.3f, epsilon=%.3f}", quantile, epsilon);
    }
  }
}

// CHECKSTYLE:ON: checkstyle

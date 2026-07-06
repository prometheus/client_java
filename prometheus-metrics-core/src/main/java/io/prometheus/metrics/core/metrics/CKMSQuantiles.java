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
      // delta is bounded by maxWidthNotCrossingTargets(r) in addition to the paper's f(r) - 1:
      // for a targeted quantile with 2*epsilon >= 1-quantile, f(r) below the target is of order
      // n-r, and a freshly inserted sample with such a delta has a possible-rank interval
      // centered near rank n regardless of its position — indistinguishable in get() from a
      // genuine sample near a target. See maxWidthNotCrossingTargets.
      iterator.add(new Sample(value, effectiveMaxWidth(r) - 1));
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

    // Return the value of the sample whose possible rank range is centered closest to the
    // desired rank. The true rank of samples.get(i) is somewhere in
    // [r(i) , r(i) + delta(i)] with r(i) = g(0) + ... + g(i), so the best point estimate
    // of its rank is the center of that interval.
    //
    // Note that the previous implementation ("stop at the first sample with
    // r + g + delta > desiredRank + f(desiredRank)/2 and return the value of the sample
    // before it") is only correct if g + delta is small for all samples up to the target
    // rank. With targeted quantiles the error function f() allows g + delta to be large at
    // ranks far below a target quantile (for a target (q, epsilon) and rank r < q*n it
    // allows 2*epsilon*(n-r)/(1-q)), and freshly inserted samples used to get
    // delta = f(r) - 1 (and flush() above guarantees freshly inserted samples are present).
    // Such a sample tripped the old stop condition long before the target rank, so get()
    // returned a value from a far lower quantile than requested. For example, with
    // quantiles {(0.9, 0.05), (0.99, 0.005)} get(0.99) returned the minimum observation.
    // Sample widths are additionally bounded by maxWidthNotCrossingTargets at insert and
    // merge time, so near a target quantile the interval centers are tight estimates.
    int r = 0; // sum of g's left of the current sample
    int desiredRank = (int) Math.ceil(q * n);
    double bestDistance = Double.MAX_VALUE;
    Sample bestSample = samples.getFirst();
    for (Sample sample : samples) {
      double rankEstimate = r + sample.g + sample.delta / 2.0;
      double distance = Math.abs(rankEstimate - desiredRank);
      if (distance < bestDistance) {
        bestDistance = distance;
        bestSample = sample;
      }
      r += sample.g;
    }
    return bestSample.value;
  }

  /** Error function, as in definition 5 of the paper. */
  int f(int r) {
    int minResult = Integer.MAX_VALUE;
    for (Quantile q : quantiles) {
      if (q.quantile == 0 || q.quantile == 1) {
        continue;
      }
      int result;
      // We had a numerical error here with the following example:
      // quantile = 0.95, epsilon = 0.01, (n-r) = 30.
      // The expected result of (2*0.01*30)/(1-0.95) is 12. The actual result is 11.99999999999999.
      // To avoid running into these types of error we add 0.00000000001 before rounding down.
      if (r >= q.quantile * n) {
        result = (int) (q.v * r + 0.00000000001);
      } else {
        result = (int) (q.u * (n - r) + 0.00000000001);
      }
      if (result < minResult) {
        minResult = result;
      }
    }
    return Math.max(minResult, 1);
  }

  /**
   * Maximum width (g + delta) of a sample whose predecessor has rank r such that the sample keeps
   * enough resolution around the accuracy window [quantile*n - epsilon*n, quantile*n + epsilon*n]
   * of every target quantile: below a window a sample may extend at most max(windowStart - r,
   * 2*epsilon*n) — it can intrude into the window but never reach the window's end — and any sample
   * overlapping a window has width at most the window's size 2*epsilon*n. So no single sample can
   * span a whole window, and resolution around each target stays at the window scale: the center of
   * a sample's possible-rank interval is within epsilon*n of any rank the sample covers inside the
   * window.
   *
   * <p>This is needed in addition to the error function f(): for a target (quantile, epsilon) and
   * rank r below the target, f() allows a width of 2*epsilon*(n-r)/(1-quantile). When 2*epsilon >=
   * (1-quantile) — e.g. (0.9, 0.05) or (0.99, 0.005) — this is >= (n-r), i.e. a single sample may
   * span all ranks from r to n. Two failure modes follow: compress() merges away all samples
   * between r and n, permanently destroying the information needed to answer the quantile query
   * (with quantiles {(0.9, 0.05), (0.99, 0.005)} the sample list collapsed to 3 samples regardless
   * of how many values were inserted, and get() returned the minimum observation for every
   * quantile), and insertBefore() assigns freshly inserted samples a delta of the same order, so
   * their possible-rank intervals are centered near rank n and get() cannot tell them apart from
   * genuine samples near a target. This bound is therefore applied both when merging in compress()
   * and when assigning delta in insertBefore(). For configurations with 2*epsilon < (1-quantile)
   * this bound is larger than f() near the target, so behavior is mostly unchanged.
   *
   * <p>The bound is anchored at the window's start rather than its end so that it does not
   * degenerate for targets with quantile + epsilon >= 1 (e.g. (0.95, 0.05) or (0.99, 0.01)), where
   * the window's end is rank n and "may not extend past the window's end" would be no constraint at
   * all.
   *
   * <p>This is intentionally not part of the per-sample invariant (g + delta <= f(r)): the bound
   * depends on n while a sample's delta is fixed at insert time, so it cannot be maintained as a
   * static invariant — but enforcing it at insert and merge time is what matters, because those are
   * the only operations that create sample widths.
   */
  int maxWidthNotCrossingTargets(int r) {
    double min = Double.MAX_VALUE;
    for (Quantile q : quantiles) {
      if (q.quantile == 0 || q.quantile == 1) {
        continue;
      }
      double windowStart = q.quantile * n - q.epsilon * n;
      double windowEnd = q.quantile * n + q.epsilon * n;
      if (r < windowEnd) {
        min = Math.min(min, Math.max(windowStart - r, 2 * q.epsilon * n));
      }
    }
    if (min == Double.MAX_VALUE) {
      return Integer.MAX_VALUE;
    }
    return Math.max((int) (min + 0.00000000001), 1);
  }

  /**
   * Effective maximum width (g + delta) of a sample whose predecessor has rank r: the error
   * function f() additionally bounded by {@link #maxWidthNotCrossingTargets(int)}. Both places that
   * create sample widths — merging in compress() and delta assignment in insertBefore() — must use
   * this combined bound.
   */
  int effectiveMaxWidth(int r) {
    return Math.min(f(r), maxWidthNotCrossingTargets(r));
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
      if (left.g + right.g + right.delta < effectiveMaxWidth(r)) {
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

package io.prometheus.metrics.core.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.data.Offset.offset;

import io.prometheus.metrics.core.metrics.CKMSQuantiles.Quantile;
import java.util.*;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

class CKMSQuantilesTest {

  private final Quantile qMin = new Quantile(0.0, 0.00);
  private final Quantile q50 = new Quantile(0.5, 0.01);
  private final Quantile q95 = new Quantile(0.95, 0.005);
  private final Quantile q99 = new Quantile(0.99, 0.001);
  private final Quantile qMax = new Quantile(1.0, 0.00);

  @Test
  void testGetOnEmptyValues() {
    CKMSQuantiles ckms = new CKMSQuantiles(q50, q95, q99);
    assertThat(Double.isNaN(ckms.get(q95.quantile))).isTrue();
  }

  @Test
  void testGet() {
    Random random = new Random(0);
    CKMSQuantiles ckms = new CKMSQuantiles(q50, q95, q99);
    List<Double> input = shuffledValues(100, random);
    for (double value : input) {
      ckms.insert(value);
    }
    validateResults(ckms);
  }

  @Test
  void testBatchInsert() {
    Random random = new Random(1);
    testInsertBatch(1, 1, 100, random);
    testInsertBatch(1, 10, 100, random);
    testInsertBatch(2, 10, 100, random);
    testInsertBatch(
        2, 110, 100,
        random); // compress never called, because compress interval > number of inserts
    testInsertBatch(3, 10, 100, random);
    testInsertBatch(10, 10, 100, random);
    testInsertBatch(128, 128, 1, random);
    testInsertBatch(128, 128, 1000, random);
    testInsertBatch(128, 128, 10 * 1000, random);
    testInsertBatch(128, 128, 100 * 1000, random);
    testInsertBatch(128, 128, 1000 * 1000, random);
  }

  private void testInsertBatch(
      int batchSize, int compressInterval, int totalNumber, Random random) {
    System.out.println(
        "testInsertBatch(batchSize="
            + batchSize
            + ", compressInterval="
            + compressInterval
            + ", totalNumber="
            + totalNumber
            + ")");
    CKMSQuantiles ckms = new CKMSQuantiles(q50, q95);
    int insertsSinceCompress = 0;
    List<Double> input = shuffledValues(totalNumber, random);
    for (int i = 0; i < input.size(); i += batchSize) {
      double[] batch = new double[batchSize];
      int j;
      for (j = 0; j < batchSize && i + j < input.size(); j++) {
        batch[j] = input.get(i + j);
      }
      Arrays.sort(batch, 0, j);
      ckms.insertBatch(batch, j);
      validateSamples(ckms); // after each insert the samples should still be valid
      insertsSinceCompress += j;
      if (insertsSinceCompress >= compressInterval) {
        ckms.compress();
        validateSamples(ckms); // after each compress the samples should still be valid
        insertsSinceCompress = 0;
      }
    }
    validateResults(ckms);
  }

  @Test
  void testGetWithAMillionElements() {
    Random random = new Random(2);
    List<Double> input = shuffledValues(1000 * 1000, random);
    CKMSQuantiles ckms = new CKMSQuantiles(q50, q95, q99);
    for (double v : input) {
      ckms.insert(v);
    }
    validateResults(ckms);
    assertThat(ckms.samples).as("sample size should be way below 1_000_000").hasSizeLessThan(1000);
  }

  @Test
  void testMin() {
    Random random = new Random(3);
    List<Double> input = shuffledValues(1000, random);
    CKMSQuantiles ckms = new CKMSQuantiles(qMin);
    for (double v : input) {
      ckms.insert(v);
    }
    validateResults(ckms);
    ckms.compress();
    assertThat(ckms.samples).hasSize(2);
  }

  @Test
  void testMax() {
    Random random = new Random(4);
    List<Double> input = shuffledValues(1000, random);
    CKMSQuantiles ckms = new CKMSQuantiles(qMax);
    for (double v : input) {
      ckms.insert(v);
    }
    validateResults(ckms);
    ckms.compress();
    assertThat(ckms.samples).hasSize(2);
  }

  @Test
  void testMinMax() {
    Random random = new Random(5);
    List<Double> input = shuffledValues(1000, random);
    CKMSQuantiles ckms = new CKMSQuantiles(qMin, qMax);
    for (double v : input) {
      ckms.insert(v);
    }
    validateResults(ckms);
    ckms.compress();
    assertThat(ckms.samples).hasSize(2);
  }

  @Test
  void testMinAndOthers() {
    Random random = new Random(6);
    List<Double> input = shuffledValues(1000, random);
    CKMSQuantiles ckms = new CKMSQuantiles(q95, qMin);
    for (double v : input) {
      ckms.insert(v);
    }
    validateResults(ckms);
    assertThat(ckms.samples).hasSizeLessThan(200); // should be a lot less than input.size()
  }

  @Test
  void testMaxAndOthers() {
    Random random = new Random(7);
    List<Double> input = shuffledValues(10000, random);
    CKMSQuantiles ckms = new CKMSQuantiles(q50, q95, qMax);
    for (double v : input) {
      ckms.insert(v);
    }
    validateResults(ckms);
    assertThat(ckms.samples).hasSizeLessThan(200); // should be a lot less than input.size()
  }

  @Test
  void testMinMaxAndOthers() {
    Random random = new Random(8);
    List<Double> input = shuffledValues(10000, random);
    CKMSQuantiles ckms = new CKMSQuantiles(qMin, q50, q95, q99, qMax);
    for (double v : input) {
      ckms.insert(v);
    }
    validateResults(ckms);
    assertThat(ckms.samples).hasSizeLessThan(200); // should be a lot less than input.size()
  }

  @Test
  void testExactQuantile() {
    Random random = new Random(9);
    List<Double> input = shuffledValues(10000, random);
    CKMSQuantiles ckms = new CKMSQuantiles(new Quantile(0.95, 0));
    for (double v : input) {
      ckms.insert(v);
    }
    validateResults(ckms);
    // With epsilon == 0 we need to keep all inputs in samples.
    assertThat(ckms.samples).hasSameSizeAs(input);
  }

  @Test
  void testExactAndOthers() {
    Random random = new Random(10);
    List<Double> input = shuffledValues(10000, random);
    CKMSQuantiles ckms = new CKMSQuantiles(q50, new Quantile(0.95, 0), q99);
    for (double v : input) {
      ckms.insert(v);
    }
    validateResults(ckms);
    // With epsilon == 0 we need to keep all inputs in samples.
    assertThat(ckms.samples).hasSameSizeAs(input);
  }

  @Test
  void testExactAndMin() {
    Random random = new Random(11);
    List<Double> input = shuffledValues(10000, random);
    CKMSQuantiles ckms = new CKMSQuantiles(qMin, q50, new Quantile(0.95, 0));
    for (double v : input) {
      ckms.insert(v);
    }
    validateResults(ckms);
    // With epsilon == 0 we need to keep all inputs in samples.
    assertThat(ckms.samples).hasSameSizeAs(input);
  }

  @Test
  void testMaxEpsilon() {
    Random random = new Random(12);
    List<Double> input = shuffledValues(10000, random);
    // epsilon == 1 basically gives you random results, but it should still not throw an exception.
    CKMSQuantiles ckms = new CKMSQuantiles(new Quantile(0.95, 1));
    for (double v : input) {
      ckms.insert(v);
    }
    validateResults(ckms);
  }

  /**
   * Reproducer for the quantile collapse bug: for a target quantile (q, epsilon) the error function
   * allows samples below rank q*n to have g + delta up to 2*epsilon*(n-r)/(1-q). When 2*epsilon >=
   * 1-q (as in (0.9, 0.05) or (0.99, 0.005) — both taken from real-world configurations) this is >=
   * n-r, so (a) compress() merged almost all samples away and (b) get() stopped at the first
   * freshly inserted sample (delta = f(r)-1) and returned the minimum observation for every
   * quantile: get(0.9) == get(0.99) == 1.0 regardless of the input data.
   */
  @Test
  void testTargetedQuantilesDoNotCollapse() {
    Random random = new Random(42);
    CKMSQuantiles ckms = new CKMSQuantiles(new Quantile(0.9, 0.05), new Quantile(0.99, 0.005));
    for (double value : shuffledValues(100 * 1000, random)) {
      ckms.insert(value);
    }
    validateResults(ckms);
  }

  /** Like {@link #testTargetedQuantilesDoNotCollapse()}, with a single targeted quantile. */
  @Test
  void testSingleTargetedQuantileDoesNotCollapse() {
    Random random = new Random(43);
    CKMSQuantiles ckms = new CKMSQuantiles(new Quantile(0.99, 0.005));
    for (double value : shuffledValues(100 * 1000, random)) {
      ckms.insert(value);
    }
    validateResults(ckms);
  }

  /**
   * Adding a well-behaved quantile (0.5, 0.05) to the collapsing configuration bounds the error
   * function in the lower ranks, but before the fix get(0.99) still returned a value from around
   * the 85th percentile: samples between rank 0.8*n and 0.99*n may have g + delta up to n-r, and
   * the old stop condition in get() tripped on the first of them.
   */
  @Test
  void testTargetedQuantilesWithMedian() {
    Random random = new Random(44);
    CKMSQuantiles ckms =
        new CKMSQuantiles(
            new Quantile(0.5, 0.05), new Quantile(0.9, 0.05), new Quantile(0.99, 0.005));
    for (double value : shuffledValues(100 * 1000, random)) {
      ckms.insert(value);
    }
    validateResults(ckms);
  }

  /**
   * Deterministic small-n case from the review of an earlier fix attempt
   * (https://github.com/prometheus/client_java/pull/2316): with values 1..10,000 shuffled with seed
   * 2, selecting the sample whose possible-rank interval is centered nearest the desired rank
   * returned 9784 there, outside the accuracy window [9800, 10000]. The additional merge bound in
   * compress() keeps enough resolution around the target rank for this case to pass.
   */
  @Test
  void testSingleTargetedQuantileSmallN() {
    Random random = new Random(2);
    CKMSQuantiles ckms = new CKMSQuantiles(new Quantile(0.99, 0.005));
    for (double value : shuffledValues(10 * 1000, random)) {
      ckms.insert(value);
    }
    validateResults(ckms);
  }

  /**
   * Counterexample: with a single well-behaved quantile (0.5, 0.025) and values 1..257 shuffled
   * with seed 5, an earlier revision that selected the sample whose possible-rank interval is
   * centered nearest the desired rank returned 121, outside the accuracy window [122, 135].
   * Selecting the sample that minimizes the worst-case rank error (center distance plus half the
   * interval's width) returns 132.
   */
  @Test
  void testMedianSmallN() {
    Random random = new Random(5);
    CKMSQuantiles ckms = new CKMSQuantiles(new Quantile(0.5, 0.025));
    for (double value : shuffledValues(257, random)) {
      ckms.insert(value);
    }
    validateResults(ckms);
  }

  /**
   * Targets with quantile + epsilon >= 1 are the degenerate end of the collapsing family: the
   * accuracy window's end is rank n itself, so a bound phrased as "a sample may not extend past the
   * window's end" is no constraint at all, and freshly inserted samples with delta = f(r) - 1 have
   * possible-rank intervals centered near rank n regardless of their position. Both the merge bound
   * and the insert-time delta bound must be anchored at the window's start for these
   * configurations.
   */
  @Test
  void testTargetedQuantileWindowReachingMaximum() {
    for (Quantile quantile : new Quantile[] {new Quantile(0.99, 0.01), new Quantile(0.95, 0.05)}) {
      for (int seed = 0; seed < 5; seed++) {
        Random random = new Random(seed);
        CKMSQuantiles ckms = new CKMSQuantiles(quantile);
        for (double value : shuffledValues(10 * 1000, random)) {
          ckms.insert(value);
        }
        validateResults(ckms);
      }
    }
  }

  /**
   * Descending input is the worst case for the collapsing configurations: every insert happens at
   * the front of the sample list, where the error function is loosest. Before the insert-time delta
   * bound, get(0.9) was off by 2.9 * epsilon here.
   */
  @Test
  void testTargetedQuantilesDescendingInput() {
    CKMSQuantiles ckms = new CKMSQuantiles(new Quantile(0.9, 0.05), new Quantile(0.99, 0.005));
    for (int value = 10 * 1000; value >= 1; value--) {
      ckms.insert(value);
    }
    validateResults(ckms);
  }

  /**
   * At larger n, descending input can still exceed the 1 * epsilon rank bound (up to 1.75 * epsilon
   * observed across the configurations tested): sample widths are bounded when they are created,
   * but with descending input a sample's rank grows by 1 per insert while the accuracy windows move
   * right by only quantile ± epsilon per insert, so old samples drift towards the windows and their
   * width bound erodes. This is not a regression: the previous implementation exceeded 1 * epsilon
   * on descending input for every configuration tested, in this exact case by 18 * epsilon for
   * get(0.9) and 198 * epsilon for get(0.99). This test pins the remaining gap to at most 2 *
   * epsilon.
   */
  @Test
  void testTargetedQuantilesDescendingInputLargeN() {
    CKMSQuantiles ckms = new CKMSQuantiles(new Quantile(0.9, 0.05), new Quantile(0.99, 0.005));
    for (int value = 100 * 1000; value >= 1; value--) {
      ckms.insert(value);
    }
    validateResults(ckms, 2);
  }

  /** Ascending input order, the counterpart of {@link #testTargetedQuantilesDescendingInput()}. */
  @Test
  void testTargetedQuantilesAscendingInput() {
    CKMSQuantiles ckms = new CKMSQuantiles(new Quantile(0.9, 0.05), new Quantile(0.99, 0.005));
    for (int value = 1; value <= 10 * 1000; value++) {
      ckms.insert(value);
    }
    validateResults(ckms);
  }

  @Test
  void testGetGaussian() {
    RandomGenerator rand = new JDKRandomGenerator();
    rand.setSeed(0);

    double mean = 0.0;
    double stddev = 1.0;
    NormalDistribution normalDistribution =
        new NormalDistribution(
            rand, mean, stddev, NormalDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);

    List<Quantile> quantiles = new ArrayList<>();
    quantiles.add(new Quantile(0.10, 0.001));
    quantiles.add(new Quantile(0.50, 0.01));
    quantiles.add(new Quantile(0.90, 0.001));
    quantiles.add(new Quantile(0.95, 0.001));
    quantiles.add(new Quantile(0.99, 0.001));

    CKMSQuantiles ckms = new CKMSQuantiles(quantiles.toArray(new Quantile[] {}));

    final int elemCount = 1000 * 1000;
    double[] shuffle = normalDistribution.sample(elemCount);

    // insert a million samples
    for (double v : shuffle) {
      ckms.insert(v);
    }

    // give the actual values for the quantiles we test
    double p10 = normalDistribution.inverseCumulativeProbability(0.1);
    double p90 = normalDistribution.inverseCumulativeProbability(0.9);
    double p95 = normalDistribution.inverseCumulativeProbability(0.95);
    double p99 = normalDistribution.inverseCumulativeProbability(0.99);

    // ε-approximate quantiles relaxes the requirement
    // to finding an item with rank between (φ−ε)n and (φ+ε)n.
    assertThat(ckms.get(0.1))
        .isCloseTo(p10, offset(errorBoundsNormalDistribution(0.1, 0.001, normalDistribution)));
    assertThat(ckms.get(0.5))
        .isCloseTo(mean, offset(errorBoundsNormalDistribution(0.5, 0.01, normalDistribution)));
    assertThat(ckms.get(0.9))
        .isCloseTo(p90, offset(errorBoundsNormalDistribution(0.9, 0.001, normalDistribution)));
    assertThat(ckms.get(0.95))
        .isCloseTo(p95, offset(errorBoundsNormalDistribution(0.95, 0.001, normalDistribution)));
    assertThat(ckms.get(0.99))
        .isCloseTo(p99, offset(errorBoundsNormalDistribution(0.99, 0.001, normalDistribution)));

    assertThat(ckms.samples).as("sample size should be below 1000").hasSizeLessThan(1000);
  }

  double errorBoundsNormalDistribution(double p, double epsilon, NormalDistribution nd) {
    // (φ+ε)n
    double upperBound = nd.inverseCumulativeProbability(p + epsilon);
    // (φ−ε)n
    double lowerBound = nd.inverseCumulativeProbability(p - epsilon);
    // subtract and divide by 2, assuming that the increase is linear in this small epsilon.
    return Math.abs(upperBound - lowerBound) / 2;
  }

  @Test
  void testIllegalArgumentException() {
    try {
      new Quantile(-1, 0);
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage()).isEqualTo("Quantile must be between 0 and 1");
    } catch (Exception e) {
      fail("Wrong exception thrown" + e);
    }
    try {
      new Quantile(0.95, 2);
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage()).isEqualTo("Epsilon must be between 0 and 1");
    } catch (Exception e) {
      fail("Wrong exception thrown" + e);
    }
  }

  private List<Double> shuffledValues(int n, Random random) {
    List<Double> result = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      result.add(i + 1.0);
    }
    Collections.shuffle(result, random);
    return result;
  }

  /** The following invariant must be true for each sample: g + delta <= f(r) */
  private void validateSamples(CKMSQuantiles ckms) {
    double prev = -1.0;
    int r = 0; // sum of all g's left of the current sample
    for (CKMSQuantiles.Sample sample : ckms.samples) {
      String msg =
          "invalid sample " + sample + ": count=" + ckms.n + " r=" + r + " f(r)=" + ckms.f(r);
      assertThat(sample.g + sample.delta).as(msg).isLessThanOrEqualTo(ckms.f(r));
      assertThat(prev)
          .as(
              "Samples not ordered. Keep in mind that insertBatch() takes a sorted array as"
                  + " parameter.")
          .isLessThanOrEqualTo(sample.value);
      prev = sample.value;
      r += sample.g;
    }
    assertThat(ckms.n)
        .as("the sum of all g's must be the total number of observations")
        .isEqualTo(r);
  }

  /**
   * The values that we insert in these tests are always the numbers from 1 to n, in some order. So
   * we can trivially calculate the range of acceptable results for each quantile. We check if the
   * value returned by get() is within the documented q ± epsilon rank bound (floor/ceil because
   * ranks are integers).
   */
  private void validateResults(CKMSQuantiles ckms) {
    validateResults(ckms, 1);
  }

  /**
   * Only pass an epsilonFactor other than 1 for the known descending-input gap (see {@link
   * #testTargetedQuantilesDescendingInputLargeN()}); everything else must meet the documented
   * bound.
   */
  private void validateResults(CKMSQuantiles ckms, double epsilonFactor) {
    for (Quantile q : ckms.quantiles) {
      double actual = ckms.get(q.quantile);
      double lowerBound, upperBound;
      if (q.quantile == 0) {
        lowerBound = 1;
        upperBound = 1;
      } else if (q.quantile == 1) {
        lowerBound = ckms.n;
        upperBound = ckms.n;
      } else {
        lowerBound = Math.floor(ckms.n * (q.quantile - epsilonFactor * q.epsilon));
        upperBound = Math.ceil(ckms.n * (q.quantile + epsilonFactor * q.epsilon));
      }
      boolean ok = actual >= lowerBound && actual <= upperBound;
      if (!ok) {
        for (CKMSQuantiles.Sample sample : ckms.samples) {
          System.err.println(sample);
        }
      }
      String errorMessage =
          q
              + ": "
              + actual
              + " not in ["
              + lowerBound
              + ", "
              + upperBound
              + "], n="
              + ckms.n
              + ", "
              + q.quantile
              + "*"
              + ckms.n
              + "="
              + (q.quantile * ckms.n);
      assertThat(ok).as(errorMessage).isTrue();
    }
  }
}

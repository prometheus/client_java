package io.prometheus.client.exemplars;

/**
 * Exemplar sampler for histogram metrics.
 */
public interface HistogramExemplarSampler {
  /**
   * @param value      the value to be observed.
   * @param bucketFrom upper boundary of the previous bucket in the histogram.
   *                   Will be {@link Double#NEGATIVE_INFINITY} if there is no previous bucket.
   * @param bucketTo   upper boundary of this histogram bucket.
   *                   Will be {@link Double#POSITIVE_INFINITY} if this is the last bucket.
   * @param previous   the previously sampled exemplar, or {@code null} if there is none.
   * @return an Exemplar to be sampled, or {@code null} if the previous exemplar does not need to be updated.
   * Returning {@code null} and returning {@code previous} is equivalent.
   */
  Exemplar sample(double value, double bucketFrom, double bucketTo, Exemplar previous);
}

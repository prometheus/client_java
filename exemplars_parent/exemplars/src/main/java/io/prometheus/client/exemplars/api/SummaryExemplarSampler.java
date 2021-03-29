package io.prometheus.client.exemplars.api;

/**
 * Exemplar sampler for summary metrics.
 */
public interface SummaryExemplarSampler {
  /**
   * @param value the value to be observed.
   * @return an Exemplar if an Exemplar should be associated with that value, or {@code null} otherwise.
   */
  Exemplar sample(double value);
}

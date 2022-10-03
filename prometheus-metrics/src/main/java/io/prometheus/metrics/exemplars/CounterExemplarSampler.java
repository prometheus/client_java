package io.prometheus.metrics.exemplars;

/**
 * Exemplar sampler for counter metrics.
 */
public interface CounterExemplarSampler {

  /**
   * @param increment the value added to the counter on this event
   * @param previous  the previously sampled exemplar, or {@code null} if there is none.
   * @return an Exemplar to be sampled, or {@code null} if the previous exemplar does not need to be updated.
   * Returning {@code null} and returning {@code previous} is equivalent.
   */
  Exemplar sample(double increment, Exemplar previous);
}

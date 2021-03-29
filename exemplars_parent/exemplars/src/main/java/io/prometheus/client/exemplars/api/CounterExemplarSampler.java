package io.prometheus.client.exemplars.api;

/**
 * Exemplar sampler for counter metrics.
 */
public interface CounterExemplarSampler {

  /**
   * @param value    the new counter value.
   *                 Note that {@link Value#get()} might be an expensive operation. The implementation
   *                 of {@link CounterExemplarSampler} should call {@link Value#get()} only if needed.
   * @param previous the previously sampled exemplar, or {@code null} if there is none.
   * @return an Exemplar to be sampled, or {@code null} if the previous exemplar does not need to be updated.
   * Returning {@code null} and returning {@code previous} is equivalent.
   */
  Exemplar sample(Value value, Exemplar previous);
}

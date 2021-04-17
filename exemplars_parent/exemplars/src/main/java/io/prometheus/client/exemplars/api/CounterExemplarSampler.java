package io.prometheus.client.exemplars.api;

/**
 * Exemplar sampler for counter metrics.
 */
public interface CounterExemplarSampler {

  /**
   * @param increment     the value added to the counter on this event
   * @param newTotalValue the new total counter value after adding the increment.
   *                      Note that {@link Value#get()} might be an expensive operation. The implementation
   *                      of {@link CounterExemplarSampler} should call {@link Value#get()} only if needed.
   * @param previous      the previously sampled exemplar, or {@code null} if there is none.
   * @return an Exemplar to be sampled, or {@code null} if the previous exemplar does not need to be updated.
   * Returning {@code null} and returning {@code previous} is equivalent.
   */
  Exemplar sample(double increment, Value newTotalValue, Exemplar previous);
}

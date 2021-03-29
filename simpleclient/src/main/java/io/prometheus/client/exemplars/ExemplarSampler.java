package io.prometheus.client.exemplars;

/**
 * For convenience, an interface for implementing both,
 * the {@link CounterExemplarSampler} and the {@link HistogramExemplarSampler}.
 */
public interface ExemplarSampler extends CounterExemplarSampler, HistogramExemplarSampler {
}

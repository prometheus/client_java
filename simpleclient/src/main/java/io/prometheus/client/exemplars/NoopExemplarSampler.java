package io.prometheus.client.exemplars;

/**
 * An {@link ExemplarSampler} that will never sample.
 */
public class NoopExemplarSampler implements ExemplarSampler {

  @Override
  public Exemplar sample(double increment, Exemplar previous) {
    return null;
  }

  @Override
  public Exemplar sample(double value, double bucketFrom, double bucketTo, Exemplar previous) {
    return null;
  }
}
package io.prometheus.client.exemplars.impl;

import io.prometheus.client.exemplars.api.Exemplar;
import io.prometheus.client.exemplars.api.ExemplarSampler;
import io.prometheus.client.exemplars.api.Value;

public class NoopExemplarSampler implements ExemplarSampler {

  @Override
  public Exemplar sample(double increment, Value newTotalValue, Exemplar previous) {
    return null;
  }

  @Override
  public Exemplar sample(double value, double bucketFrom, double bucketTo, Exemplar previous) {
    return null;
  }
}
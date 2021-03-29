package io.prometheus.client.exemplars.impl;

import io.prometheus.client.exemplars.api.CounterExemplarSampler;
import io.prometheus.client.exemplars.api.Exemplar;
import io.prometheus.client.exemplars.api.GaugeExemplarSampler;
import io.prometheus.client.exemplars.api.HistogramExemplarSampler;
import io.prometheus.client.exemplars.api.SummaryExemplarSampler;
import io.prometheus.client.exemplars.api.Value;

public class NoopExemplarSampler implements CounterExemplarSampler, GaugeExemplarSampler, HistogramExemplarSampler,
    SummaryExemplarSampler {

  @Override
  public Exemplar sample(Value value, Exemplar previous) {
    return null;
  }

  @Override
  public Exemplar sample(double value, double bucketFrom, double bucketTo, Exemplar previous) {
    return null;
  }

  @Override
  public Exemplar sample(double value) {
    return null;
  }
}
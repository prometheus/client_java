package io.prometheus.metrics.exemplars;

import io.prometheus.metrics.model.Exemplar;
import io.prometheus.metrics.model.Exemplars;
import io.prometheus.metrics.model.Labels;

import java.util.Collection;

public interface ExemplarSampler {
    Exemplars collect();
    void observe(double value);
    void observeWithExemplar(double value, Labels labels);
}

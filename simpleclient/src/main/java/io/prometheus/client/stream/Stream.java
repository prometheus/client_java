package io.prometheus.client.stream;

import java.util.Map;

/**
 * Created by warebot on 1/4/16.
 */
public interface Stream<T extends Number & Comparable<T>> {
    void insert(T v);
    T query (double q);
    Map<Quantile, T> getSnapshot(Quantile...quantiles);
    void reset();
}

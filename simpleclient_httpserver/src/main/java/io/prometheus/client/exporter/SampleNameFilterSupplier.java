package io.prometheus.client.exporter;

import io.prometheus.client.Predicate;
import io.prometheus.client.Supplier;

/**
 * For convenience, an implementation of a {@code Supplier<Predicate<String>>} that
 * always returns the same sampleNameFilter.
 */
public class SampleNameFilterSupplier implements Supplier<Predicate<String>> {

    private final Predicate<String> sampleNameFilter;

    public static SampleNameFilterSupplier of(Predicate<String> sampleNameFilter) {
        return new SampleNameFilterSupplier(sampleNameFilter);
    }

    private SampleNameFilterSupplier(Predicate<String> sampleNameFilter) {
        this.sampleNameFilter = sampleNameFilter;
    }

    @Override
    public Predicate<String> get() {
        return sampleNameFilter;
    }
}

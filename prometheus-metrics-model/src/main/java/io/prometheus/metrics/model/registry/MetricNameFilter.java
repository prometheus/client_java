package io.prometheus.metrics.model.registry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;

import static java.util.Collections.unmodifiableCollection;

/**
 * Filter samples (i.e. time series) by name.
 */
public class MetricNameFilter implements Predicate<String> {

    /**
     * For convenience, a filter that allows all names.
     */
    public static final Predicate<String> ALLOW_ALL = name -> true;

    private final Collection<String> nameIsEqualTo;
    private final Collection<String> nameIsNotEqualTo;
    private final Collection<String> nameStartsWith;
    private final Collection<String> nameDoesNotStartWith;

    private MetricNameFilter(Collection<String> nameIsEqualTo, Collection<String> nameIsNotEqualTo, Collection<String> nameStartsWith, Collection<String> nameDoesNotStartWith) {
        this.nameIsEqualTo = unmodifiableCollection(new ArrayList<>(nameIsEqualTo));
        this.nameIsNotEqualTo = unmodifiableCollection(new ArrayList<>(nameIsNotEqualTo));
        this.nameStartsWith = unmodifiableCollection(new ArrayList<>(nameStartsWith));
        this.nameDoesNotStartWith = unmodifiableCollection(new ArrayList<>(nameDoesNotStartWith));
    }

    @Override
    public boolean test(String sampleName) {
        return matchesNameEqualTo(sampleName)
                && !matchesNameNotEqualTo(sampleName)
                && matchesNameStartsWith(sampleName)
                && !matchesNameDoesNotStartWith(sampleName);
    }

    private boolean matchesNameEqualTo(String metricName) {
        if (nameIsEqualTo.isEmpty()) {
            return true;
        }
        for (String name : nameIsEqualTo) {
            // The following ignores suffixes like _total.
            // "request_count" and "request_count_total" both match a metric named "request_count".
            if (name.startsWith(metricName)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesNameNotEqualTo(String metricName) {
        if (nameIsNotEqualTo.isEmpty()) {
            return false;
        }
        for (String name : nameIsNotEqualTo) {
            // The following ignores suffixes like _total.
            // "request_count" and "request_count_total" both match a metric named "request_count".
            if (name.startsWith(metricName)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesNameStartsWith(String metricName) {
        if (nameStartsWith.isEmpty()) {
            return true;
        }
        for (String prefix : nameStartsWith) {
            if (metricName.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesNameDoesNotStartWith(String metricName) {
        if (nameDoesNotStartWith.isEmpty()) {
            return false;
        }
        for (String prefix : nameDoesNotStartWith) {
            if (metricName.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final Collection<String> nameEqualTo = new ArrayList<>();
        private final Collection<String> nameNotEqualTo = new ArrayList<>();
        private final Collection<String> nameStartsWith = new ArrayList<>();
        private final Collection<String> nameDoesNotStartWith = new ArrayList<>();

        private Builder() {
        }

        /**
         * @see #nameMustBeEqualTo(Collection)
         */
        public Builder nameMustBeEqualTo(String... names) {
            return nameMustBeEqualTo(Arrays.asList(names));
        }

        /**
         * Only samples with one of the {@code names} will be included.
         * <p>
         * Note that the provided {@code names} will be matched against the sample name (i.e. the time series name)
         * and not the metric name. For instance, to retrieve all samples from a histogram, you must include the
         * '_count', '_sum' and '_bucket' names.
         * <p>
         * This method should be used by HTTP exporters to implement the {@code ?name[]=} URL parameters.
         *
         * @param names empty means no restriction.
         */
        public Builder nameMustBeEqualTo(Collection<String> names) {
            if (names != null) {
                nameEqualTo.addAll(names);
            }
            return this;
        }

        /**
         * @see #nameMustNotBeEqualTo(Collection)
         */
        public Builder nameMustNotBeEqualTo(String... names) {
            return nameMustNotBeEqualTo(Arrays.asList(names));
        }

        /**
         * All samples that are not in {@code names} will be excluded.
         * <p>
         * Note that the provided {@code names} will be matched against the sample name (i.e. the time series name)
         * and not the metric name. For instance, to exclude all samples from a histogram, you must exclude the
         * '_count', '_sum' and '_bucket' names.
         *
         * @param names empty means no name will be excluded.
         */
        public Builder nameMustNotBeEqualTo(Collection<String> names) {
            if (names != null) {
                nameNotEqualTo.addAll(names);
            }
            return this;
        }

        /**
         * @see #nameMustStartWith(Collection)
         */
        public Builder nameMustStartWith(String... prefixes) {
            return nameMustStartWith(Arrays.asList(prefixes));
        }

        /**
         * Only samples whose name starts with one of the {@code prefixes} will be included.
         *
         * @param prefixes empty means no restriction.
         */
        public Builder nameMustStartWith(Collection<String> prefixes) {
            if (prefixes != null) {
                nameStartsWith.addAll(prefixes);
            }
            return this;
        }

        /**
         * @see #nameMustNotStartWith(Collection)
         */
        public Builder nameMustNotStartWith(String... prefixes) {
            return nameMustNotStartWith(Arrays.asList(prefixes));
        }

        /**
         * Samples with names starting with one of the {@code prefixes} will be excluded.
         *
         * @param prefixes empty means no time series will be excluded.
         */
        public Builder nameMustNotStartWith(Collection<String> prefixes) {
            if (prefixes != null) {
                nameDoesNotStartWith.addAll(prefixes);
            }
            return this;
        }

        public MetricNameFilter build() {
            return new MetricNameFilter(nameEqualTo, nameNotEqualTo, nameStartsWith, nameDoesNotStartWith);
        }
    }
}

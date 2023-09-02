package io.prometheus.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import static java.util.Collections.unmodifiableCollection;

/**
 * Filter samples (i.e. time series) by name.
 */
public class SampleNameFilter implements Predicate<String> {

    /**
     * For convenience, a filter that allows all names.
     */
    public static final Predicate<String> ALLOW_ALL = new AllowAll();

    private final Collection<String> nameIsEqualTo;
    private final Collection<String> nameIsNotEqualTo;
    private final Collection<String> nameStartsWith;
    private final Collection<String> nameDoesNotStartWith;
    private final Collection<String> nameEndsWith;
    private final Collection<String> nameDoesNotEndWith;

    @Override
    public boolean test(String sampleName) {
        return matchesNameEqualTo(sampleName)
                && matchesNameNotEqualTo(sampleName)
                && matchesNameStartsWith(sampleName)
                && matchesNameDoesNotStartWith(sampleName)
                && matchesNameEndsWith(sampleName)
                && matchesNameDoesNotEndWith(sampleName);
    }

    /**
     * Replacement for Java 8's {@code Predicate.and()} for compatibility with Java versions &lt; 8.
     */
    public Predicate<String> and(final Predicate<? super String> other) {
        if (other == null) {
            throw new NullPointerException();
        }
        return new Predicate<String>() {
            @Override
            public boolean test(String s) {
                return SampleNameFilter.this.test(s) && other.test(s);
            }
        };
    }

    private boolean matchesNameEqualTo(String metricName) {
        if (nameIsEqualTo.isEmpty()) {
            return true;
        }
        return nameIsEqualTo.contains(metricName);
    }

    private boolean matchesNameNotEqualTo(String metricName) {
        if (nameIsNotEqualTo.isEmpty()) {
            return true;
        }
        return !nameIsNotEqualTo.contains(metricName);
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
            return true;
        }
        for (String prefix : nameDoesNotStartWith) {
            if (metricName.startsWith(prefix)) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesNameEndsWith(String metricName) {
        if (nameEndsWith.isEmpty()) {
            return true;
        }
        for (String suffix : nameEndsWith) {
            if (metricName.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesNameDoesNotEndWith(String metricName) {
        if (nameDoesNotEndWith.isEmpty()) {
            return true;
        }
        for (String suffix : nameDoesNotEndWith) {
            if (metricName.endsWith(suffix)) {
                return false;
            }
        }
        return true;
    }

    public static class Builder {

        private final Collection<String> nameEqualTo = new ArrayList<String>();
        private final Collection<String> nameNotEqualTo = new ArrayList<String>();
        private final Collection<String> nameStartsWith = new ArrayList<String>();
        private final Collection<String> nameDoesNotStartWith = new ArrayList<String>();
        private final Collection<String> nameEndsWith = new ArrayList<String>();
        private final Collection<String> nameDoesNotEndWith = new ArrayList<String>();

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
            nameEqualTo.addAll(names);
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
            nameNotEqualTo.addAll(names);
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
         * @param prefixes empty means no restriction.
         */
        public Builder nameMustStartWith(Collection<String> prefixes) {
            nameStartsWith.addAll(prefixes);
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
         * @param prefixes empty means no time series will be excluded.
         */
        public Builder nameMustNotStartWith(Collection<String> prefixes) {
            nameDoesNotStartWith.addAll(prefixes);
            return this;
        }

        /**
         * @see #nameMustEndWith(Collection)
         */
        public Builder nameMustEndWith(String... prefixes) {
            return nameMustEndWith(Arrays.asList(prefixes));
        }

        /**
         * Only samples whose name ends with one of the {@code prefixes} will be included.
         * @param prefixes empty means no restriction.
         */
        public Builder nameMustEndWith(Collection<String> prefixes) {
            nameEndsWith.addAll(prefixes);
            return this;
        }

        /**
         * @see #nameMustNotEndWith(Collection)
         */
        public Builder nameMustNotEndWith(String... prefixes) {
            return nameMustNotEndWith(Arrays.asList(prefixes));
        }

        /**
         * Samples with names ending with one of the {@code prefixes} will be excluded.
         * For example, this can be used to exclude {@code _created} metrics.
         * @param prefixes empty means no time series will be excluded.
         */
        public Builder nameMustNotEndWith(Collection<String> prefixes) {
            nameDoesNotEndWith.addAll(prefixes);
            return this;
        }

        public SampleNameFilter build() {
            return new SampleNameFilter(nameEqualTo, nameNotEqualTo, nameStartsWith, nameDoesNotStartWith,
                nameEndsWith, nameDoesNotEndWith);
        }
    }

    private SampleNameFilter(Collection<String> nameIsEqualTo, Collection<String> nameIsNotEqualTo,
                             Collection<String> nameStartsWith, Collection<String> nameDoesNotStartWith,
                             Collection<String> nameEndsWith, Collection<String> nameDoesNotEndWith) {
        this.nameIsEqualTo = unmodifiableCollection(nameIsEqualTo);
        this.nameIsNotEqualTo = unmodifiableCollection(nameIsNotEqualTo);
        this.nameStartsWith = unmodifiableCollection(nameStartsWith);
        this.nameDoesNotStartWith = unmodifiableCollection(nameDoesNotStartWith);
        this.nameEndsWith = unmodifiableCollection(nameEndsWith);
        this.nameDoesNotEndWith = unmodifiableCollection(nameDoesNotEndWith);
    }

    private static class AllowAll implements Predicate<String> {

        private AllowAll() {
        }

        @Override
        public boolean test(String s) {
            return true;
        }
    }

    /**
     * Helper method to deserialize a {@code delimiter}-separated list of Strings into a {@code List<String>}.
     * <p>
     * {@code delimiter} is one of {@code , ; \t \n}.
     * <p>
     * This is implemented here so that exporters can provide a consistent configuration format for
     * lists of allowed names.
     */
    public static List<String> stringToList(String s) {
        List<String> result = new ArrayList<String>();
        if (s != null) {
            StringTokenizer tokenizer = new StringTokenizer(s, ",; \t\n");
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                token = token.trim();
                if (token.length() > 0) {
                    result.add(token);
                }
            }
        }
        return result;
    }

    /**
     * Helper method to compose a filter such that Sample names must
     * <ul>
     *     <li>match the existing filter</li>
     *     <li>and be in the list of allowedNames</li>
     * </ul>
     * This should be used to implement the {@code names[]} query parameter in HTTP exporters.
     *
     * @param filter may be null, indicating that the resulting filter should just filter by {@code allowedNames}.
     * @param allowedNames may be null or empty, indicating that {@code filter} is returned unmodified.
     * @return a filter combining the exising {@code filter} and the {@code allowedNames}, or {@code null}
     *         if both parameters were {@code null}.
     */
    public static Predicate<String> restrictToNamesEqualTo(Predicate<String> filter, Collection<String> allowedNames) {
        if (allowedNames != null && !allowedNames.isEmpty()) {
            SampleNameFilter allowedNamesFilter = new SampleNameFilter.Builder()
                    .nameMustBeEqualTo(allowedNames)
                    .build();
            if (filter == null) {
                return allowedNamesFilter;
            } else {
                return allowedNamesFilter.and(filter);
            }
        }
        return filter;
    }
}
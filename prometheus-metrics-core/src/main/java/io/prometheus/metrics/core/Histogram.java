package io.prometheus.metrics.core;

import io.prometheus.metrics.exemplars.ExemplarConfig;
import io.prometheus.metrics.model.FixedBucket;
import io.prometheus.metrics.model.FixedBuckets;
import io.prometheus.metrics.model.FixedBucketsHistogramSnapshot;
import io.prometheus.metrics.model.ExponentialBucket;
import io.prometheus.metrics.model.ExponentialBucketsHistogramSnapshot;
import io.prometheus.metrics.model.Labels;
import io.prometheus.metrics.model.MetricType;
import io.prometheus.metrics.observer.DistributionObserver;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;

public abstract class Histogram extends ObservingMetric<DistributionObserver, Histogram.HistogramData> implements DistributionObserver {

    protected final long createdTimeMillis = System.currentTimeMillis();

    // Helper used in exponential histograms. Must be here because inner classes can't have static variables.
    private static final double[][] EXPONENTIAL_BOUNDS;
    public static final double[] DEFAULT_BUCKETS = new double[] { .005, .01, .025, .05, .075, .1, .25, .5, .75, 1, 2.5, 5, 7.5, 10 };

    protected Histogram(Builder builder) {
        super(builder);
    }

    abstract static class HistogramData extends MetricData<DistributionObserver> implements DistributionObserver {}

    static class FixedBucketsHistogram extends Histogram {
        private final double[] upperBounds;

        private FixedBucketsHistogram(Histogram.Builder.FixedBucketsHistogramBuilder builder) {
            super(builder.getHistogramBuilder());
            SortedSet<Double> upperBounds = new TreeSet<>();
            for (double upperBound : builder.upperBounds) { // TODO: can upperBounds be null? Copy normalize code from ExemplarSampler?
                upperBounds.add(upperBound);
            }
            upperBounds.add(Double.POSITIVE_INFINITY);
            this.upperBounds = new double[upperBounds.size()];
            int i=0;
            for (double upperBound : upperBounds) {
                this.upperBounds[i++] = upperBound;
            }
        }

        @Override
        protected FixedBucketsHistogramData newMetricData() {
            return new FixedBucketsHistogramData();
        }

        @Override
        public FixedBucketsHistogramSnapshot collect() {
            return (FixedBucketsHistogramSnapshot) super.collect();
        }

        @Override
        protected FixedBucketsHistogramSnapshot collect(List<Labels> labels, List<HistogramData> metricData) {
            List<FixedBucketsHistogramSnapshot.FixedBucketsHistogramData> data = new ArrayList<>(labels.size());
            for (int i=0; i<labels.size(); i++) {
                data.add(((FixedBucketsHistogramData) metricData.get(i)).collect(labels.get(i)));
            }
            return new FixedBucketsHistogramSnapshot(getMetadata(), data);
        }

        class FixedBucketsHistogramData extends HistogramData {
            private final LongAdder[] buckets;
            protected final DoubleAdder sum = new DoubleAdder();
            protected final LongAdder count = new LongAdder();
            private final Buffer<FixedBucketsHistogramSnapshot.FixedBucketsHistogramData> buffer = new Buffer<>();

            private FixedBucketsHistogramData() {
                buckets = new LongAdder[upperBounds.length];
                for (int i = 0; i < upperBounds.length; i++) {
                    buckets[i] = new LongAdder();
                }
            }

            @Override
            public void observe(double amount) {
                if (!buffer.append(amount)) {
                    doObserve(amount);
                }
                if (isExemplarsEnabled() && hasSpanContextSupplier()) {
                    lazyInitExemplarSampler(exemplarConfig, null, upperBounds);
                    exemplarSampler.observe(amount);
                }
            }

            @Override
            public void observeWithExemplar(double amount, Labels labels) {
                if (!buffer.append(amount)) {
                    doObserve(amount);
                }
                if (isExemplarsEnabled()) {
                    lazyInitExemplarSampler(exemplarConfig, null, upperBounds);
                    exemplarSampler.observeWithExemplar(amount, labels);
                }
            }

            private void doObserve(double amount) {
                for (int i = 0; i < upperBounds.length; ++i) {
                    // The last bucket is +Inf, so we always increment.
                    if (amount <= upperBounds[i]) {
                        buckets[i].add(1);
                        break;
                    }
                }
                sum.add(amount);
                count.increment(); // must be the last step, because count is used to signal that the operation is complete.
            }

            public FixedBucketsHistogramSnapshot.FixedBucketsHistogramData collect(Labels labels) {
                Collection<io.prometheus.metrics.model.Exemplar> exemplars = exemplarSampler != null ? exemplarSampler.collect() : Collections.emptyList();
                return buffer.run(
                        expectedCount -> count.sum() == expectedCount,
                        () -> {
                            long[] cumulativeCounts = new long[buckets.length];
                            long cumulativeCount = 0;
                            for (int i=0; i<upperBounds.length; i++) {
                                cumulativeCount += buckets[i].sum();
                                cumulativeCounts[i] = cumulativeCount;
                            }
                            FixedBuckets buckets = FixedBuckets.of(upperBounds, cumulativeCounts);
                            return new FixedBucketsHistogramSnapshot.FixedBucketsHistogramData(count.longValue(), sum.sum(), buckets, labels, createdTimeMillis);
                        },
                        this::doObserve
                );
            }

            @Override
            public DistributionObserver toObserver() {
                return this;
            }

        }

        @Override
        public void observe(double amount) {
            getNoLabels().observe(amount);
        }

        @Override
        public void observeWithExemplar(double amount, Labels labels) {
            getNoLabels().observeWithExemplar(amount, labels);
        }
    }

    static class ExponentialBucketsHistogram extends Histogram {

        private final int schema; // integer in [-4, 8]
        private final double zeroThreshold;
        private final int maxBuckets;

        private ExponentialBucketsHistogram(Histogram.Builder.ExponentialBucketsHistogramBuilder builder) {
            super(builder.getHistogramBuilder());
            this.schema = builder.schema;
            //this.base = Math.pow(2, Math.pow(2, -schema));
            this.zeroThreshold = builder.zeroThreshold;
            this.maxBuckets = builder.maxBuckets;
        }

        @Override
        public void observe(double amount) {
            getNoLabels().observe(amount);
        }

        @Override
        public void observeWithExemplar(double amount, Labels labels) {
            getNoLabels().observeWithExemplar(amount, labels);
        }

        @Override
        public ExponentialBucketsHistogramSnapshot collect() {
            return (ExponentialBucketsHistogramSnapshot) super.collect();
        }

        @Override
        protected ExponentialBucketsHistogramSnapshot collect(List<Labels> labels, List<HistogramData> metricData) {
            List<ExponentialBucketsHistogramSnapshot.ExponentialBucketsHistogramData> data = new ArrayList<>(labels.size());
            for (int i=0; i<labels.size(); i++) {
                data.add(((ExponentialBucketsHistogramData) metricData.get(i)).collect(labels.get(i)));
            }
            return new ExponentialBucketsHistogramSnapshot(getMetadata(), data);
        }

        @Override
        protected ExponentialBucketsHistogramData newMetricData() {
            return new ExponentialBucketsHistogramData();
        }

        class ExponentialBucketsHistogramData extends HistogramData {
            private volatile int schema = ExponentialBucketsHistogram.this.schema; // integer in [-4, 8]
            private final double zeroThreshold = ExponentialBucketsHistogram.this.zeroThreshold;
            private final ConcurrentHashMap<Integer, LongAdder> bucketsForPositiveValues = new ConcurrentHashMap<>();
            private final ConcurrentHashMap<Integer, LongAdder> bucketsForNegativeValues = new ConcurrentHashMap<>();
            private final LongAdder count = new LongAdder();
            private final DoubleAdder sum = new DoubleAdder();
            private final LongAdder zeroCount = new LongAdder();
            private final long createdTimeMillis = System.currentTimeMillis();
            private final Buffer<ExponentialBucketsHistogramSnapshot.ExponentialBucketsHistogramData> buffer = new Buffer<>();

            private ExponentialBucketsHistogramData() {
            }

            @Override
            public void observe(double amount) {
                if (!buffer.append(amount)) {
                    doObserve(amount);
                }
                if (isExemplarsEnabled() && hasSpanContextSupplier()) {
                    lazyInitExemplarSampler(exemplarConfig, 4, null);
                    exemplarSampler.observe(amount);
                }
            }

            @Override
            public void observeWithExemplar(double amount, Labels labels) {
                if (!buffer.append(amount)) {
                    doObserve(amount);
                }
                if (isExemplarsEnabled()) {
                    lazyInitExemplarSampler(exemplarConfig, 4, null);
                    exemplarSampler.observeWithExemplar(amount, labels);
                }
            }

            private void doObserve(double value) {
                if (!Double.isNaN(value) && !Double.isInfinite(value)) {
                    if (value > zeroThreshold) {
                        addToBucket(bucketsForPositiveValues, value);
                    } else if (value < -zeroThreshold) {
                        addToBucket(bucketsForNegativeValues, -value);
                    } else {
                        zeroCount.add(1);
                    }
                }
                sum.add(value);
                // count must be incremented last, because in collect() the count
                // indicates the number of completed observations.
                count.increment();
            }

            private void addToBucket(ConcurrentHashMap<Integer, LongAdder> buckets, double value) {
                int bucketIndex = findBucketIndex(value);
                // debug: the IllegalStateException should never happen
                // todo: remove and write a unit test for findBucketIndex() instead
                double base = Math.pow(2, Math.pow(2, -schema));
                if (!(Math.pow(base, bucketIndex - 1) < value && value <= (Math.pow(base, bucketIndex)) + 0.00000000001)) { // (2^(1/4))^4 should be 2, but is 1.9999999999999998
                    throw new IllegalStateException("Bucket index " + bucketIndex + ": Invariance violated: " + Math.pow(base, bucketIndex - 1) + " < " + value + " <= " + Math.pow(base, bucketIndex));
                }
                LongAdder bucketCount = buckets.get(bucketIndex);
                if (bucketCount == null) {
                    LongAdder newBucketCount = new LongAdder();
                    LongAdder existingBucketCount = buckets.putIfAbsent(bucketIndex, newBucketCount);
                    bucketCount = existingBucketCount == null ? newBucketCount : existingBucketCount;
                }
                bucketCount.increment();
                int numberOfBuckets = buckets.size();
                if (numberOfBuckets == Integer.MAX_VALUE || numberOfBuckets > maxBuckets) {
                    scaleDown();
                }
            }

            // Assumptions:
            // Double.isNan(value) is false;
            // Double.isInfinite(value) is false;
            // value >= 0.0
            private int findBucketIndex(double value) {
                // The following is a naive implementation of C's frexp() function.
                // Performance can be improved by using the internal Bit representation of floating point numbers.
                // More info on the Bit representation of floating point numbers:
                // https://stackoverflow.com/questions/8341395/what-is-a-subnormal-floating-point-number
                // Result: value == frac * 2^exp where frac in [0.5, 1).
                double frac = value;
                int exp = 0;
                while (frac < 0.5) {
                    frac *= 2.0;
                    exp--;
                }
                while (frac >= 1.0) {
                    frac /= 2.0;
                    exp++;
                }
                // end of frexp()

                if (schema >= 1) {
                    return findIndex(EXPONENTIAL_BOUNDS[schema - 1], frac) + (exp - 1) * EXPONENTIAL_BOUNDS[schema - 1].length;
                } else {
                    int result = exp;
                    if (frac == 0.5) {
                        result--;
                    }
                    int div = 1 << -schema;
                    return (result + div - 1) / div;
                }
            }

            private int findIndex(double[] bounds, double frac) {
                // The following is the equivalent of golang's sort.SearchFloat64s(bounds, frac)
                // See https://pkg.go.dev/sort#SearchFloat64s
                int first = 0;
                int last = bounds.length - 1;
                while (first <= last) {
                    int mid = (first + last) / 2;
                    if (bounds[mid] == frac) {
                        return mid;
                    } else if (bounds[mid] < frac) {
                        first = mid + 1;
                    } else {
                        last = mid - 1;
                    }
                }
                return last + 1;
            }

            private void scaleDown() {
                // TODO
            }

            public ExponentialBucketsHistogramSnapshot.ExponentialBucketsHistogramData collect(Labels labels) {
                final Collection<io.prometheus.metrics.model.Exemplar> exemplars = exemplarSampler != null ? exemplarSampler.collect() : Collections.emptyList();
                return buffer.run(
                        expectedCount -> count.sum() == expectedCount,
                        () -> new ExponentialBucketsHistogramSnapshot.ExponentialBucketsHistogramData(
                                count.sum(),
                                sum.sum(),
                                schema,
                                zeroCount.sum(),
                                zeroThreshold,
                                toBucketList(bucketsForPositiveValues),
                                toBucketList(bucketsForNegativeValues),
                                labels,
                                exemplars,
                                createdTimeMillis
                        ),
                        this::doObserve
                );
            }

            @Override
            public DistributionObserver toObserver() {
                return this;
            }

            private List<ExponentialBucket> toBucketList(ConcurrentHashMap<Integer, LongAdder> map) {
                ExponentialBucket[] result = new ExponentialBucket[map.size()];
                int i=0;
                for (Map.Entry<Integer, LongAdder> entry : map.entrySet()) {
                    result[i++] = new ExponentialBucket(entry.getValue().sum(), entry.getKey());
                }
                return Arrays.asList(result);
            }
        }
    }

    static {
        // See bounds in client_golang's histogram implementation.
        EXPONENTIAL_BOUNDS = new double[8][];
        for (int schema = 1; schema <= 8; schema++) {
            EXPONENTIAL_BOUNDS[schema - 1] = new double[1 << schema];
            EXPONENTIAL_BOUNDS[schema - 1][0] = 0.5;
            // https://github.com/open-telemetry/opentelemetry-proto/blob/main/opentelemetry/proto/metrics/v1/metrics.proto#L501
            double base = Math.pow(2, Math.pow(2, -schema));
            for (int i = 1; i < EXPONENTIAL_BOUNDS[schema - 1].length; i++) {
                if (i % 2 == 0 && schema > 1) {
                    // Use previously calculated value for increased precision, see comment in client_golang's implementation.
                    EXPONENTIAL_BOUNDS[schema - 1][i] = EXPONENTIAL_BOUNDS[schema - 2][i / 2];
                } else {
                    EXPONENTIAL_BOUNDS[schema - 1][i] = EXPONENTIAL_BOUNDS[schema - 1][i - 1] * base;
                }
            }
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }
    public static class Builder extends ObservingMetric.Builder<Histogram.Builder, Histogram> {

        private MetricType metricType = MetricType.EXPONENTIAL_BUCKETS_HISTOGRAM;

        private Builder() {
            super(Collections.singletonList("le"));
        }

        @Override
        protected MetricType getType() {
            return metricType;
        }

        public FixedBucketsHistogramBuilder withBuckets(double... buckets) {
            this.metricType = MetricType.EXPLICIT_BUCKETS_HISTOGRAM;
            return new FixedBucketsHistogramBuilder().withBuckets(buckets);
        }

        public FixedBucketsHistogramBuilder withDefaultBuckets() {
            return withBuckets(DEFAULT_BUCKETS);
        }

        public FixedBucketsHistogramBuilder withLinearBuckets(double start, double width, int count) {
            this.metricType = MetricType.EXPLICIT_BUCKETS_HISTOGRAM;
            return new FixedBucketsHistogramBuilder().withLinearBuckets(start, width, count);
        }

        public FixedBucketsHistogramBuilder withExponentialBuckets(double start, double factor, int count) {
            this.metricType = MetricType.EXPLICIT_BUCKETS_HISTOGRAM;
            return new FixedBucketsHistogramBuilder().withExponentialBuckets(start, factor, count);
        }

        public ExponentialBucketsHistogramBuilder withSchema(int schema) {
            return new ExponentialBucketsHistogramBuilder().withSchema(schema);
        }

        public ExponentialBucketsHistogramBuilder withZeroThreshold(double zeroThreshold) {
            return new ExponentialBucketsHistogramBuilder().withZeroThreshold(zeroThreshold);
        }

        public ExponentialBucketsHistogramBuilder withMaxBuckets(int maxBuckets) {
            return new ExponentialBucketsHistogramBuilder().withMaxBuckets(maxBuckets);
        }

        @Override
        public Histogram build() {
            return new ExponentialBucketsHistogram(new ExponentialBucketsHistogramBuilder());
        }
        class FixedBucketsHistogramBuilder {

            private double[] upperBounds;

            private FixedBucketsHistogramBuilder() {
            }

            public FixedBucketsHistogramBuilder withBuckets(double... upperBounds) {
                this.upperBounds = upperBounds;
                for (double bound : upperBounds) {
                    if (Double.isNaN(bound)) {
                        throw new IllegalArgumentException("Cannot use NaN as upper bound for a histogram");
                    }
                }
                return this;
            }

            public FixedBucketsHistogramBuilder withLinearBuckets(double start, double width, int count) {
                this.upperBounds = new double[count];
                // Use BigDecimal to avoid weird bucket boundaries like 0.7000000000000001.
                BigDecimal s = new BigDecimal(Double.toString(start));
                BigDecimal w = new BigDecimal(Double.toString(width));
                for (int i = 0; i < count; i++) {
                    upperBounds[i] = s.add(w.multiply(new BigDecimal(i))).doubleValue();
                }
                return this;
            }

            public FixedBucketsHistogramBuilder withExponentialBuckets(double start, double factor, int count) {
                upperBounds = new double[count];
                for (int i = 0; i < count; i++) {
                    upperBounds[i] = start * Math.pow(factor, i);
                }
                return this;
            }

            public FixedBucketsHistogramBuilder withExemplars() {
                Builder.this.withExemplars();
                return this;
            }

            public FixedBucketsHistogramBuilder withoutExemplars() {
                Builder.this.withoutExemplars();
                return this;
            }

            public FixedBucketsHistogramBuilder withExemplarConfig(ExemplarConfig exemplarConfig) {
                Builder.this.withExemplarConfig(exemplarConfig);
                return this;
            }

            public FixedBucketsHistogramBuilder withLabelNames(String... labelNames) {
                Builder.this.withLabelNames(labelNames);
                return this;
            }

            public FixedBucketsHistogramBuilder withName(String name) {
                Builder.this.withName(name);
                return this;
            }

            public FixedBucketsHistogramBuilder withUnit(String unit) {
                Builder.this.withUnit(unit);
                return this;
            }

            public FixedBucketsHistogramBuilder withHelp(String help) {
                Builder.this.withHelp(help);
                return this;
            }

            public FixedBucketsHistogramBuilder withConstLabels(Labels constLabels) {
                Builder.this.withConstLabels(constLabels);
                return this;
            }

            private Builder getHistogramBuilder() {
                return Builder.this;
            }

            public FixedBucketsHistogram build() {
                return new FixedBucketsHistogram(this);
            }
        }

        class ExponentialBucketsHistogramBuilder {

            private int schema = 5;
            private double zeroThreshold = Double.MIN_NORMAL;
            private int maxBuckets = Integer.MAX_VALUE;
            private ExponentialBucketsHistogramBuilder() {}

            public ExponentialBucketsHistogramBuilder withSchema(int schema) {
                if (schema < -4 || schema > 8) {
                    throw new IllegalArgumentException("Unsupported schema " + schema + ": expecting -4 <= schema <= 8.");
                }
                this.schema = schema;
                return this;
            }

            public ExponentialBucketsHistogramBuilder withZeroThreshold(double zeroThreshold) {
                if (zeroThreshold < 0) {
                    throw new IllegalArgumentException("Illegal zeroThreshold " + zeroThreshold + ": zeroThreshold must be >= 0");
                }
                this.zeroThreshold = zeroThreshold;
                return this;
            }

            public ExponentialBucketsHistogramBuilder withMaxBuckets(int maxBuckets) {
                this.maxBuckets = maxBuckets;
                return this;
            }

            public ExponentialBucketsHistogramBuilder withExemplars() {
                Builder.this.withExemplars();
                return this;
            }

            public ExponentialBucketsHistogramBuilder withoutExemplars() {
                Builder.this.withoutExemplars();
                return this;
            }

            public ExponentialBucketsHistogramBuilder withExemplarConfig(ExemplarConfig exemplarConfig) {
                Builder.this.withExemplarConfig(exemplarConfig);
                return this;
            }

            public ExponentialBucketsHistogramBuilder withLabelNames(String... labelNames) {
                Builder.this.withLabelNames(labelNames);
                return this;
            }

            public ExponentialBucketsHistogramBuilder withName(String name) {
                Builder.this.withName(name);
                return this;
            }

            public ExponentialBucketsHistogramBuilder withUnit(String unit) {
                Builder.this.withUnit(unit);
                return this;
            }

            public ExponentialBucketsHistogramBuilder withHelp(String help) {
                Builder.this.withHelp(help);
                return this;
            }

            public ExponentialBucketsHistogramBuilder withConstLabels(Labels constLabels) {
                Builder.this.withConstLabels(constLabels);
                return this;
            }

            public ExponentialBucketsHistogram build() {
                return new ExponentialBucketsHistogram(this);
            }

            private Builder getHistogramBuilder() {
                return Builder.this;
            }
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}

package io.prometheus.metrics.core;

import io.prometheus.metrics.model.Exemplars;
import io.prometheus.metrics.model.HistogramSnapshot;
import io.prometheus.metrics.model.NativeHistogramBuckets;
import io.prometheus.metrics.model.ClassicHistogramBuckets;
import io.prometheus.metrics.model.Labels;
import io.prometheus.metrics.observer.DistributionObserver;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;

// TODO: The default should be a histogram with both, classic buckets and native buckets for transition.
public class Histogram extends ObservingMetric<DistributionObserver, Histogram.HistogramData> implements DistributionObserver {

    private final int CLASSIC_HISTOGRAM = Integer.MIN_VALUE;

    private static final double[][] NATIVE_BOUNDS;
    public static final double[] DEFAULT_CLASSIC_UPPER_BOUNDS = new double[]{.005, .01, .025, .05, .075, .1, .25, .5, .75, 1, 2.5, 5, 7.5, 10};

    private final double[] classicUpperBounds; // null or empty for native histograms?
    private final int nativeSchema; // integer in [-4, 8]
    private final double nativeZeroThreshold;
    private final int nativeMaxBuckets;

    private Histogram(Histogram.Builder builder) {
        super(builder);
        SortedSet<Double> upperBounds = new TreeSet<>();
        if (builder.upperBounds != null) {
            for (double upperBound : builder.upperBounds) {
                upperBounds.add(upperBound);
            }
            upperBounds.add(Double.POSITIVE_INFINITY);
        }
        this.classicUpperBounds = new double[upperBounds.size()];
        int i = 0;
        for (double upperBound : upperBounds) {
            this.classicUpperBounds[i++] = upperBound;
        }
        this.nativeSchema = builder.nativeSchema;
        this.nativeZeroThreshold = builder.zeroThreshold;
        this.nativeMaxBuckets = builder.maxBuckets;
    }

    public class HistogramData extends MetricData<DistributionObserver> implements DistributionObserver {
        private volatile int schema = Histogram.this.nativeSchema; // integer in [-4, 8]
        private final double zeroThreshold = Histogram.this.nativeZeroThreshold;
        private final ConcurrentHashMap<Integer, LongAdder> bucketsForPositiveValues = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<Integer, LongAdder> bucketsForNegativeValues = new ConcurrentHashMap<>();
        private final LongAdder count = new LongAdder();
        private final DoubleAdder sum = new DoubleAdder();
        private final LongAdder zeroCount = new LongAdder();
        private final long createdTimeMillis = System.currentTimeMillis();
        private final Buffer<HistogramSnapshot.HistogramData> buffer = new Buffer<>();
        private final LongAdder[] buckets;

        private HistogramData() {
            buckets = new LongAdder[classicUpperBounds.length];
            for (int i = 0; i < classicUpperBounds.length; i++) {
                buckets[i] = new LongAdder();
            }
        }

        @Override
        public void observe(double amount) {
            if (Double.isNaN(amount) || Double.isInfinite(amount)) {
                return;
            }
            if (!buffer.append(amount)) {
                doObserve(amount);
            }
            if (isExemplarsEnabled() && hasSpanContextSupplier()) {
                // TODO: Does that work for both native and classic histograms?
                lazyInitExemplarSampler(exemplarConfig, null, classicUpperBounds);
                exemplarSampler.observe(amount);
            }
        }

        @Override
        public void observeWithExemplar(double amount, Labels labels) {
            if (Double.isNaN(amount) || Double.isInfinite(amount)) {
                return;
            }
            if (!buffer.append(amount)) {
                doObserve(amount);
            }
            if (isExemplarsEnabled()) {
                // TODO: Does that work for both native and classic histograms?
                lazyInitExemplarSampler(exemplarConfig, null, classicUpperBounds);
                exemplarSampler.observeWithExemplar(amount, labels);
            }
        }

        private void doObserve(double value) {
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                // This cannot happen because the observe() methods don't pass NaN or infinite to doObserve()
                throw new IllegalArgumentException("value must not be NaN or infinite.");
            }
            if (classicUpperBounds != null) {
            for (int i = 0; i < classicUpperBounds.length; ++i) {
                // The last bucket is +Inf, so we always increment.
                if (value <= classicUpperBounds[i]) {
                    buckets[i].add(1);
                    break;
                }
            }
            }
            if (nativeSchema != CLASSIC_HISTOGRAM) {
                if (value > zeroThreshold) {
                    addToBucket(bucketsForPositiveValues, value);
                } else if (value < -zeroThreshold) {
                    addToBucket(bucketsForNegativeValues, -value);
                } else {
                    zeroCount.add(1);
                }
            }
            sum.add(value);
            count.increment(); // must be the last step, because count is used to signal that the operation is complete.
        }

        public HistogramSnapshot.HistogramData collect(Labels labels) {
            Exemplars exemplars = exemplarSampler != null ? exemplarSampler.collect() : Exemplars.EMPTY;
            return buffer.run(
                    expectedCount -> count.sum() == expectedCount,
                    () -> {
                        if (classicUpperBounds == null || classicUpperBounds.length == 0) { // TODO: null or empty?
                            // native only
                            return new HistogramSnapshot.HistogramData(
                                    schema,
                                    zeroCount.sum(),
                                    zeroThreshold,
                                    toBucketList(bucketsForPositiveValues),
                                    toBucketList(bucketsForNegativeValues),
                                    sum.sum(),
                                    labels,
                                    exemplars,
                                    createdTimeMillis);
                        } else if (nativeSchema == CLASSIC_HISTOGRAM) {
                            // classic only
                            return new HistogramSnapshot.HistogramData(
                                    ClassicHistogramBuckets.of(classicUpperBounds, buckets),
                                    sum.sum(),
                                    labels,
                                    exemplars,
                                    createdTimeMillis);
                        } else {
                            // hybrid: classic and native
                            return new HistogramSnapshot.HistogramData(
                                    ClassicHistogramBuckets.of(classicUpperBounds, buckets),
                                    schema,
                                    zeroCount.sum(),
                                    zeroThreshold,
                                    toBucketList(bucketsForPositiveValues),
                                    toBucketList(bucketsForNegativeValues),
                                    sum.sum(),
                                    labels,
                                    exemplars,
                                    createdTimeMillis);
                        }
                    },
                    this::doObserve
            );
        }

        @Override
        public DistributionObserver toObserver() {
            return this;
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
            if (numberOfBuckets == Integer.MAX_VALUE || numberOfBuckets > nativeMaxBuckets) {
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
                return findIndex(NATIVE_BOUNDS[schema - 1], frac) + (exp - 1) * NATIVE_BOUNDS[schema - 1].length;
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


        private NativeHistogramBuckets toBucketList(ConcurrentHashMap<Integer, LongAdder> map) {
            int[] bucketIndexes = new int[map.size()];
            long[] counts = new long[map.size()];
            int i = 0;
            for (Map.Entry<Integer, LongAdder> entry : map.entrySet()) {
                bucketIndexes[i] = entry.getKey();
                counts[i] = entry.getValue().sum();
                i++;
            }
            return NativeHistogramBuckets.of(bucketIndexes, counts);
        }
    }

    @Override
    public HistogramSnapshot collect() {
        return (HistogramSnapshot) super.collect();
    }

    @Override
    protected HistogramSnapshot collect(List<Labels> labels, List<HistogramData> metricData) {
        List<HistogramSnapshot.HistogramData> data = new ArrayList<>(labels.size());
        for (int i = 0; i < labels.size(); i++) {
            data.add(metricData.get(i).collect(labels.get(i)));
        }
        return new HistogramSnapshot(getMetadata(), data);
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
    protected HistogramData newMetricData() {
        return new HistogramData();
    }

    static {
        // See bounds in client_golang's histogram implementation.
        NATIVE_BOUNDS = new double[8][];
        for (int schema = 1; schema <= 8; schema++) {
            NATIVE_BOUNDS[schema - 1] = new double[1 << schema];
            NATIVE_BOUNDS[schema - 1][0] = 0.5;
            // https://github.com/open-telemetry/opentelemetry-proto/blob/main/opentelemetry/proto/metrics/v1/metrics.proto#L501
            double base = Math.pow(2, Math.pow(2, -schema));
            for (int i = 1; i < NATIVE_BOUNDS[schema - 1].length; i++) {
                if (i % 2 == 0 && schema > 1) {
                    // Use previously calculated value for increased precision, see comment in client_golang's implementation.
                    NATIVE_BOUNDS[schema - 1][i] = NATIVE_BOUNDS[schema - 2][i / 2];
                } else {
                    NATIVE_BOUNDS[schema - 1][i] = NATIVE_BOUNDS[schema - 1][i - 1] * base;
                }
            }
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder extends ObservingMetric.Builder<Histogram.Builder, Histogram> {

        private final int CLASSIC_HISTOGRAM = Integer.MIN_VALUE;

        private double[] upperBounds = DEFAULT_CLASSIC_UPPER_BOUNDS;
        private int nativeSchema = 5;
        private double zeroThreshold = Double.MIN_NORMAL;
        private int maxBuckets = Integer.MAX_VALUE;

        private Builder() {
            super(Collections.singletonList("le"));
        }

        public Builder asNativeHistogram() {
            upperBounds = null; // null or empty?
            return this;
        }

        public Builder asClassicHistogram() {
            nativeSchema = CLASSIC_HISTOGRAM;
            return this;
        }

        @Override
        public Histogram build() {
            return new Histogram(this);
        }


        public Builder withBuckets(double... upperBounds) {
            this.upperBounds = upperBounds;
            for (double bound : upperBounds) {
                if (Double.isNaN(bound)) {
                    throw new IllegalArgumentException("Cannot use NaN as upper bound for a histogram");
                }
            }
            return this;
        }

        public Builder withLinearBuckets(double start, double width, int count) {
            this.upperBounds = new double[count];
            // Use BigDecimal to avoid weird bucket boundaries like 0.7000000000000001.
            BigDecimal s = new BigDecimal(Double.toString(start));
            BigDecimal w = new BigDecimal(Double.toString(width));
            for (int i = 0; i < count; i++) {
                upperBounds[i] = s.add(w.multiply(new BigDecimal(i))).doubleValue();
            }
            return this;
        }

        // TODO: Confusing because this enables classic buckets
        public Builder withDefaultBuckets() {
            this.upperBounds = DEFAULT_CLASSIC_UPPER_BOUNDS; // TODO copy
            return this;
        }

        // TODO: This is confusing because it does not refer to OpenTelemetry's exponential histograms.
        public Builder withExponentialBuckets(double start, double factor, int count) {
            upperBounds = new double[count];
            for (int i = 0; i < count; i++) {
                upperBounds[i] = start * Math.pow(factor, i);
            }
            return this;
        }

        public Builder withNativeSchema(int nativeSchema) {
            if (nativeSchema < -4 || nativeSchema > 8) {
                throw new IllegalArgumentException("Unsupported native histogram schema " + nativeSchema + ": expecting -4 <= schema <= 8.");
            }
            this.nativeSchema = nativeSchema;
            return this;
        }

        public Builder withNativeZeroThreshold(double nativeZeroThreshold) {
            if (zeroThreshold < 0) {
                throw new IllegalArgumentException("Illegal native zero threshold " + zeroThreshold + ": must be >= 0");
            }
            this.zeroThreshold = nativeZeroThreshold;
            return this;
        }

        public Builder withMaxNativeBuckets(int maxNativeBuckets) {
            this.maxBuckets = maxNativeBuckets;
            return this;
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}

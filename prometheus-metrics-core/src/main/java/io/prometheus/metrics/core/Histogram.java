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
    public static final double[] DEFAULT_CLASSIC_UPPER_BOUNDS = new double[]{.005, .01, .025, .05, .1, .25, .5, 1, 2.5, 5, 10};

    private final double[] classicUpperBounds; // null or empty for native histograms?
    private final int nativeSchema; // integer in [-4, 8]
    private final double nativeMaxZeroThreshold;
    private final double nativeMinZeroThreshold;
    private final int nativeMaxBuckets;

    private Histogram(Histogram.Builder builder) {
        super(builder);
        SortedSet<Double> upperBounds = new TreeSet<>();
        if (builder.classicUpperBounds != null) {
            for (double upperBound : builder.classicUpperBounds) {
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
        this.nativeMaxZeroThreshold = builder.nativeMaxZeroThreshold;
        this.nativeMinZeroThreshold = builder.nativeMinZeroThreshold;
        this.nativeMaxBuckets = builder.nativeMaxBuckets;
    }

    public class HistogramData extends MetricData<DistributionObserver> implements DistributionObserver {
        private volatile int nativeSchema = Histogram.this.nativeSchema; // integer in [-4, 8]
        private volatile double nativeCurrentZeroThreshold = Histogram.this.nativeMinZeroThreshold;
        private final ConcurrentHashMap<Integer, LongAdder> nativeBucketsForPositiveValues = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<Integer, LongAdder> nativeBucketsForNegativeValues = new ConcurrentHashMap<>();
        private final LongAdder count = new LongAdder();
        private final DoubleAdder sum = new DoubleAdder();
        private final LongAdder nativeZeroCount = new LongAdder();
        private final long createdTimeMillis = System.currentTimeMillis();
        private final Buffer<HistogramSnapshot.HistogramData> buffer = new Buffer<>();
        private final LongAdder[] classicBuckets;

        private HistogramData() {
            classicBuckets = new LongAdder[classicUpperBounds.length];
            for (int i = 0; i < classicUpperBounds.length; i++) {
                classicBuckets[i] = new LongAdder();
            }
        }

        @Override
        public void observe(double amount) {
            if (Double.isNaN(amount)) {
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
            if (Double.isNaN(value)) {
                // This cannot happen because the observe() methods don't pass NaN or infinite to doObserve()
                throw new IllegalArgumentException("value must not be NaN or infinite.");
            }
            if (classicUpperBounds != null) {
                for (int i = 0; i < classicUpperBounds.length; ++i) {
                    // The last bucket is +Inf, so we always increment.
                    if (value <= classicUpperBounds[i]) {
                        classicBuckets[i].add(1);
                        break;
                    }
                }
            }
            boolean nativeBucketCreated = false;
            if (Histogram.this.nativeSchema != CLASSIC_HISTOGRAM) {
                if (value > nativeCurrentZeroThreshold) {
                    nativeBucketCreated = addToBucket(nativeBucketsForPositiveValues, value);
                } else if (value < -nativeCurrentZeroThreshold) {
                    nativeBucketCreated = addToBucket(nativeBucketsForNegativeValues, -value);
                } else {
                    nativeZeroCount.add(1);
                }
            }
            sum.add(value);
            count.increment(); // must be the last step, because count is used to signal that the operation is complete.
            if (nativeBucketCreated) {
                maybeScaleDown();
            }
        }

        public HistogramSnapshot.HistogramData collect(Labels labels) {
            Exemplars exemplars = exemplarSampler != null ? exemplarSampler.collect() : Exemplars.EMPTY;
            return buffer.run(
                    expectedCount -> count.sum() == expectedCount,
                    () -> {
                        if (classicUpperBounds == null || classicUpperBounds.length == 0) { // TODO: null or empty?
                            // native only
                            return new HistogramSnapshot.HistogramData(
                                    nativeSchema,
                                    nativeZeroCount.sum(),
                                    nativeCurrentZeroThreshold,
                                    toBucketList(nativeBucketsForPositiveValues),
                                    toBucketList(nativeBucketsForNegativeValues),
                                    sum.sum(),
                                    labels,
                                    exemplars,
                                    createdTimeMillis);
                        } else if (Histogram.this.nativeSchema == CLASSIC_HISTOGRAM) {
                            // classic only
                            return new HistogramSnapshot.HistogramData(
                                    ClassicHistogramBuckets.of(classicUpperBounds, classicBuckets),
                                    sum.sum(),
                                    labels,
                                    exemplars,
                                    createdTimeMillis);
                        } else {
                            // hybrid: classic and native
                            return new HistogramSnapshot.HistogramData(
                                    ClassicHistogramBuckets.of(classicUpperBounds, classicBuckets),
                                    nativeSchema,
                                    nativeZeroCount.sum(),
                                    nativeCurrentZeroThreshold,
                                    toBucketList(nativeBucketsForPositiveValues),
                                    toBucketList(nativeBucketsForNegativeValues),
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


        private boolean addToBucket(ConcurrentHashMap<Integer, LongAdder> buckets, double value) {
            boolean newBucketCreated = false;
            int bucketIndex;
            if (Double.isInfinite(value)) {
                bucketIndex = findBucketIndex(Double.MAX_VALUE) + 1;
            } else {
                bucketIndex = findBucketIndex(value);
            }
            // debug: the IllegalStateException should never happen
            // todo: remove and write a unit test for findBucketIndex() instead
            double base = Math.pow(2, Math.pow(2, -nativeSchema));
            if (!(Math.pow(base, bucketIndex - 1) < value && value <= (Math.pow(base, bucketIndex)) + 0.00000000001)) { // (2^(1/4))^4 should be 2, but is 1.9999999999999998
                throw new IllegalStateException("Bucket index " + bucketIndex + ": Invariance violated: " + Math.pow(base, bucketIndex - 1) + " < " + value + " <= " + Math.pow(base, bucketIndex));
            }
            LongAdder bucketCount = buckets.get(bucketIndex);
            if (bucketCount == null) {
                LongAdder newBucketCount = new LongAdder();
                LongAdder existingBucketCount = buckets.putIfAbsent(bucketIndex, newBucketCount);
                bucketCount = existingBucketCount == null ? newBucketCount : existingBucketCount;
                newBucketCreated = true;
            }
            bucketCount.increment();
            return newBucketCreated;
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

            if (nativeSchema >= 1) {
                return findIndex(NATIVE_BOUNDS[nativeSchema - 1], frac) + (exp - 1) * NATIVE_BOUNDS[nativeSchema - 1].length;
            } else {
                int result = exp;
                if (frac == 0.5) {
                    result--;
                }
                int div = 1 << -nativeSchema;
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

        private void maybeScaleDown() {
            int numberOfBuckets = nativeBucketsForPositiveValues.size() + nativeBucketsForNegativeValues.size();
            if (numberOfBuckets <= nativeMaxBuckets || nativeSchema == -4) {
                return;
            }
            buffer.run(
                    expectedCount -> count.sum() == expectedCount,
                    () -> {
                        // Now we are in the synchronized block while new observations go into the buffer.
                        // Check again if we need to limit the bucket size, because another thread might
                        // have limited it in the meantime.
                        int nBuckets = nativeBucketsForPositiveValues.size() + nativeBucketsForNegativeValues.size();
                        if (nBuckets <= nativeMaxBuckets || nativeSchema == -4) {
                            return null;
                        }
                        if (maybeWidenZeroBucket()) {
                            return null;
                        }
                        doubleBucketWidth();
                        return null;
                    },
                    this::doObserve
            );
        }

        private boolean maybeWidenZeroBucket() {
            if (nativeCurrentZeroThreshold >= nativeMaxZeroThreshold) {
                return false;
            }
            int smallestIndex = findSmallestIndex(nativeBucketsForPositiveValues);
            int smallestNegativeIndex = findSmallestIndex(nativeBucketsForNegativeValues);
            if (smallestNegativeIndex < smallestIndex) {
                smallestIndex = smallestNegativeIndex;
            }
            if (smallestIndex == Integer.MAX_VALUE) {
                return false;
            }
            double newZeroThreshold = nativeBucketIndexToUpperBound(nativeSchema, smallestIndex);
            if (newZeroThreshold > nativeMaxZeroThreshold) {
                return false;
            }
            mergeWithZeroBucket(smallestIndex, nativeBucketsForPositiveValues);
            mergeWithZeroBucket(smallestIndex, nativeBucketsForNegativeValues);
            nativeCurrentZeroThreshold = newZeroThreshold;
            return true;
        }

        private void mergeWithZeroBucket(int index, Map<Integer, LongAdder> buckets) {
            LongAdder count = buckets.remove(index);
            if (count != null) {
                nativeZeroCount.add(count.sum());
            }
        }

        private double nativeBucketIndexToUpperBound(int schema, int index) {
            double result = calcUpperBound(schema, index);
            if (Double.isInfinite(result)) {
                // The last bucket boundary should always be MAX_VALUE, so that the +Inf bucket counts only
                // actual +Inf observations.
                // However, MAX_VALUE is not a natural bucket boundary, so we introduce MAX_VALUE
                // as an artificial boundary before +Inf.
                double previousBucketBoundary = calcUpperBound(schema, index-1);
                if (Double.isFinite(previousBucketBoundary) && previousBucketBoundary < Double.MAX_VALUE) {
                    return Double.MAX_VALUE;
                }
            }
            return result;
        }

        private double calcUpperBound(int schema, int index) {
            // The following reduces the numerical error for index > 0.
            // TODO: Refactor and use an algorithm as in client_golang's getLe()
            double factor = 1.0;
            while (index > 0) {
                if (index % 2 == 0) {
                    index /= 2;
                    schema -= 1;
                } else {
                    index -= 1;
                    factor *= Math.pow(2, Math.pow(2, -schema));
                }
            }
            // The following is the actual formula.
            // Without numerical errors it would be sufficient to just use this line and remove the code above.
            return factor * Math.pow(2, index * Math.pow(2, -schema));
        }

        private int findSmallestIndex(Map<Integer, LongAdder> nativeBuckets) {
            int result = Integer.MAX_VALUE;
            for (int key : nativeBuckets.keySet()) {
                if (key < result) {
                    result = key;
                }
            }
            return result;
        }

        private void doubleBucketWidth() {
            doubleBucketWidth(nativeBucketsForPositiveValues);
            doubleBucketWidth(nativeBucketsForNegativeValues);
            nativeSchema--;
        }

        private void doubleBucketWidth(Map<Integer, LongAdder> buckets) {
            int[] keys = new int[buckets.size()];
            long[] values = new long[keys.length];
            int i = 0;
            for (Map.Entry<Integer, LongAdder> entry : buckets.entrySet()) {
                keys[i] = entry.getKey();
                values[i] = entry.getValue().sum();
                i++;
            }
            buckets.clear();
            for (i = 0; i < keys.length; i++) {
                int index = (keys[i] + 1) / 2;
                LongAdder count = buckets.get(index);
                if (count == null) {
                    count = new LongAdder();
                    buckets.put(index, count);
                }
                count.add(values[i]);
            }
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

        private double[] classicUpperBounds = DEFAULT_CLASSIC_UPPER_BOUNDS;
        private int nativeSchema = 5;
        private double nativeMaxZeroThreshold = -1; // negative value means not set
        private double nativeMinZeroThreshold = Math.pow(2.0, -128);
        private int nativeMaxBuckets = Integer.MAX_VALUE;

        private Builder() {
            super(Collections.singletonList("le"));
        }

        public Builder asNativeHistogram() {
            classicUpperBounds = null; // null or empty?
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


        public Builder withClassicBuckets(double... upperBounds) {
            this.classicUpperBounds = upperBounds;
            for (double bound : upperBounds) {
                if (Double.isNaN(bound)) {
                    throw new IllegalArgumentException("Cannot use NaN as upper bound for a histogram");
                }
            }
            return this;
        }

        public Builder withClassicLinearBuckets(double start, double width, int count) {
            this.classicUpperBounds = new double[count];
            // Use BigDecimal to avoid weird bucket boundaries like 0.7000000000000001.
            BigDecimal s = new BigDecimal(Double.toString(start));
            BigDecimal w = new BigDecimal(Double.toString(width));
            for (int i = 0; i < count; i++) {
                classicUpperBounds[i] = s.add(w.multiply(new BigDecimal(i))).doubleValue();
            }
            return this;
        }

        // TODO: Confusing because this enables classic buckets
        public Builder withClassicDefaultBuckets() {
            this.classicUpperBounds = DEFAULT_CLASSIC_UPPER_BOUNDS; // TODO copy
            return this;
        }

        // TODO: This is confusing because it does not refer to OpenTelemetry's exponential histograms.
        public Builder withClassicExponentialBuckets(double start, double factor, int count) {
            classicUpperBounds = new double[count];
            for (int i = 0; i < count; i++) {
                classicUpperBounds[i] = start * Math.pow(factor, i);
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

        public Builder withNativeMaxZeroThreshold(double nativeMaxZeroThreshold) {
            if (nativeMaxZeroThreshold < 0) {
                throw new IllegalArgumentException("Illegal native zero threshold " + nativeMaxZeroThreshold + ": must be >= 0");
            }
            this.nativeMaxZeroThreshold = nativeMaxZeroThreshold;
            if (nativeMinZeroThreshold > nativeMaxZeroThreshold) {
                nativeMinZeroThreshold = nativeMaxZeroThreshold;
            }
            return this;
        }

        public Builder withNativeMaxBuckets(int nativeMaxBuckets) {
            this.nativeMaxBuckets = nativeMaxBuckets;
            return this;
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}

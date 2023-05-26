package io.prometheus.metrics.core;

import io.prometheus.metrics.model.ClassicHistogramBuckets;
import io.prometheus.metrics.model.Exemplars;
import io.prometheus.metrics.model.HistogramSnapshot;
import io.prometheus.metrics.model.Labels;
import io.prometheus.metrics.model.NativeHistogramBuckets;
import io.prometheus.metrics.observer.DistributionObserver;
import io.prometheus.metrics.util.Scheduler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;

/**
 * Prometheus supports two internal representations of histograms:
 * <ol>
 *     <li><i>Classic Histograms</i> have a fixed number of buckets with fixed bucket boundaries.</li>
 *     <li><i>Native Histograms</i> have an infinite number of buckets with a dynamic resolution.
 *         Prometheus native histograms are the same as OpenTelemetry's exponential histograms.</li>
 * </ol>
 * By default, a histogram maintains both representations. In Text format the classic histogram will be exposed,
 * in Protobuf format both representations will be exposed. This is great for migrating from classic histograms
 * to native histograms.
 * <p>
 * If you want the classic representation only, use {@link Histogram.Builder#classicHistogramOnly}.
 * If you want the native representation only, use {@link Histogram.Builder#nativeHistogramOnly}.
 */
public class Histogram extends ObservingMetric<DistributionObserver, Histogram.HistogramData> implements DistributionObserver {

    private final int CLASSIC_HISTOGRAM = Integer.MIN_VALUE;
    private static final double[][] NATIVE_BOUNDS;
    public static final double[] DEFAULT_CLASSIC_UPPER_BOUNDS = new double[]{.005, .01, .025, .05, .1, .25, .5, 1, 2.5, 5, 10};

    private final double[] classicUpperBounds; // empty for native histograms

    // The schema defines the resolution of the native histogram.
    // Schema is Prometheus terminology, in OpenTelemetry it's named "scale".
    // The formula for the bucket boundaries at position "index" is:
    //
    // base := base = (2^(2^-scale))
    // lowerBound := base^(index-1)
    // upperBound := base^(index)
    //
    // Note that this is off-by-one compared to OpenTelemetry.
    //
    // Example: With schema 0 the bucket boundaries are ... 1/16, 1/8, 1/4, 1/2, 1, 2, 4, 8, 16, ...
    // Each increment in schema doubles the number of buckets.
    //
    // The initialNativeSchema is the schema we start with. The histogram will automatically scale down
    // if the number of native histogram buckets exceeds nativeMaxBuckets.
    private final int initialNativeSchema; // integer in [-4, 8]

    // Native histogram buckets get smaller and smaller the closer they get to zero.
    // To avoid wasting a lot of buckets for observations fluctuating around zero, we consider all
    // values in [-zeroThreshold, +zeroThreshold] to be equal to zero.
    //
    // The zeroThreshold is initialized with minZeroThreshold, and will grow up to maxZeroThreshold if
    // the number of native histogram buckets exceeds nativeMaxBuckets.
    private final double nativeMinZeroThreshold;
    private final double nativeMaxZeroThreshold;

    // When the number of native histogram buckets becomes larger than nativeMaxBuckets,
    // an attempt is made to reduce the number of buckets:
    // (1) Reset if the last reset is longer than the reset duration ago
    // (2) Increase the zero bucket width if it's smaller than nativeMaxZeroThreshold
    // (3) Decrease the nativeSchema, i.e. merge pairs of neighboring buckets into one
    private final int nativeMaxBuckets;

    // If the number of native histogram buckets exceeds nativeMaxBuckets,
    // the histogram may reset (all values set to zero) after nativeResetDurationSeconds is expired.
    private final long nativeResetDurationSeconds; // 0 indicates no reset

    private Histogram(Histogram.Builder builder) {
        super(builder);
        SortedSet<Double> upperBounds = new TreeSet<>();
        if (builder.classicUpperBounds != null) {
            for (double upperBound : builder.classicUpperBounds) {
                upperBounds.add(upperBound);
            }
            upperBounds.add(Double.POSITIVE_INFINITY);
        }
        this.classicUpperBounds = new double[upperBounds.size()]; // empty if this is a pure native histogram
        int i = 0;
        for (double upperBound : upperBounds) {
            this.classicUpperBounds[i++] = upperBound;
        }
        this.initialNativeSchema = builder.nativeSchema;
        this.nativeMaxZeroThreshold = builder.nativeMaxZeroThreshold < 0 ? builder.DEFAULT_MAX_ZERO_THRESHOLD : builder.nativeMaxZeroThreshold;
        this.nativeMinZeroThreshold = builder.nativeMinZeroThreshold < 0 ? builder.DEFAULT_MIN_ZERO_THRESHOLD : builder.nativeMinZeroThreshold;
        this.nativeMaxBuckets = builder.nativeMaxBuckets;
        this.nativeResetDurationSeconds = builder.nativeResetDurationSeconds;
    }

    public class HistogramData extends MetricData<DistributionObserver> implements DistributionObserver {
        private final LongAdder[] classicBuckets;
        private final ConcurrentHashMap<Integer, LongAdder> nativeBucketsForPositiveValues = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<Integer, LongAdder> nativeBucketsForNegativeValues = new ConcurrentHashMap<>();
        private final LongAdder nativeZeroCount = new LongAdder();
        private final LongAdder count = new LongAdder();
        private final DoubleAdder sum = new DoubleAdder();
        private volatile int nativeSchema = initialNativeSchema; // integer in [-4, 8] or CLASSIC_HISTOGRAM
        private volatile double nativeZeroThreshold = Histogram.this.nativeMinZeroThreshold;
        private volatile long createdTimeMillis = System.currentTimeMillis();
        private final Buffer<HistogramSnapshot.HistogramData> buffer = new Buffer<>();
        private volatile boolean resetDurationExpired = false;

        private HistogramData() {
            classicBuckets = new LongAdder[classicUpperBounds.length];
            for (int i = 0; i < classicUpperBounds.length; i++) {
                classicBuckets[i] = new LongAdder();
            }
            maybeScheduleNextReset();
        }

        @Override
        public void observe(double amount) {
            if (Double.isNaN(amount)) {
                // See https://github.com/prometheus/client_golang/issues/1275 on ignoring NaN observations.
                return;
            }
            if (!buffer.append(amount)) {
                doObserve(amount);
            }
            if (isExemplarsEnabled() && hasSpanContextSupplier()) {
                lazyInitExemplarSampler(exemplarConfig, null, classicUpperBounds);
                exemplarSampler.observe(amount);
            }
        }

        @Override
        public void observeWithExemplar(double value, Labels labels) {
            if (Double.isNaN(value)) {
                // See https://github.com/prometheus/client_golang/issues/1275 on ignoring NaN observations.
                return;
            }
            if (!buffer.append(value)) {
                doObserve(value);
            }
            if (isExemplarsEnabled()) {
                lazyInitExemplarSampler(exemplarConfig, null, classicUpperBounds);
                exemplarSampler.observeWithExemplar(value, labels);
            }
        }

        private void doObserve(double value) {
            // classicUpperBounds is an empty array if this is a pure native histogram.
            for (int i = 0; i < classicUpperBounds.length; ++i) {
                // The last bucket is +Inf, so we always increment.
                if (value <= classicUpperBounds[i]) {
                    classicBuckets[i].add(1);
                    break;
                }
            }
            boolean nativeBucketCreated = false;
            if (Histogram.this.initialNativeSchema != CLASSIC_HISTOGRAM) {
                if (value > nativeZeroThreshold) {
                    nativeBucketCreated = addToNativeBucket(value, nativeBucketsForPositiveValues);
                } else if (value < -nativeZeroThreshold) {
                    nativeBucketCreated = addToNativeBucket(-value, nativeBucketsForNegativeValues);
                } else {
                    nativeZeroCount.add(1);
                }
            }
            sum.add(value);
            count.increment(); // must be the last step, because count is used to signal that the operation is complete.
            maybeResetOrScaleDown(value, nativeBucketCreated);
        }

        public HistogramSnapshot.HistogramData collect(Labels labels) {
            Exemplars exemplars = exemplarSampler != null ? exemplarSampler.collect() : Exemplars.EMPTY;
            return buffer.run(
                    expectedCount -> count.sum() == expectedCount,
                    () -> {
                        if (classicUpperBounds.length == 0) {
                            // native only
                            return new HistogramSnapshot.HistogramData(
                                    nativeSchema,
                                    nativeZeroCount.sum(),
                                    nativeZeroThreshold,
                                    toBucketList(nativeBucketsForPositiveValues),
                                    toBucketList(nativeBucketsForNegativeValues),
                                    sum.sum(),
                                    labels,
                                    exemplars,
                                    createdTimeMillis);
                        } else if (Histogram.this.initialNativeSchema == CLASSIC_HISTOGRAM) {
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
                                    nativeZeroThreshold,
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

        private boolean addToNativeBucket(double value, ConcurrentHashMap<Integer, LongAdder> buckets) {
            boolean newBucketCreated = false;
            int bucketIndex;
            if (Double.isInfinite(value)) {
                bucketIndex = findBucketIndex(Double.MAX_VALUE) + 1;
            } else {
                bucketIndex = findBucketIndex(value);
            }
            LongAdder bucketCount = buckets.get(bucketIndex);
            if (bucketCount == null) {
                LongAdder newBucketCount = new LongAdder();
                LongAdder existingBucketCount = buckets.putIfAbsent(bucketIndex, newBucketCount);
                if (existingBucketCount == null) {
                    newBucketCreated = true;
                    bucketCount = newBucketCount;
                } else {
                    bucketCount = existingBucketCount;
                }
            }
            bucketCount.increment();
            return newBucketCreated;
        }

        private int findBucketIndex(double value) {
            // Preconditions:
            // Double.isNan(value) is false;
            // Double.isInfinite(value) is false;
            // value > 0
            // ---
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
                int bucketIndex = exp;
                if (frac == 0.5) {
                    bucketIndex--;
                }
                int offset = (1 << -nativeSchema) - 1;
                bucketIndex = (bucketIndex + offset) >> -nativeSchema;
                return bucketIndex;
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

        /**
         * Make sure that the number of native buckets does not exceed nativeMaxBuckets.
         * <ul>
         *     <li>If the histogram has already been scaled down (nativeSchema < initialSchema)
         *         reset after resetIntervalExpired to get back to the original schema.</li>
         *     <li>If a new bucket was created and we now exceed nativeMaxBuckets
         *         run maybeScaleDown() to scale down</li>
         * </ul>
         */
        private void maybeResetOrScaleDown(double value, boolean nativeBucketCreated) {
            AtomicBoolean wasReset = new AtomicBoolean(false);
            if (resetDurationExpired && nativeSchema < initialNativeSchema) {
                buffer.run(expectedCount -> count.sum() == expectedCount,
                        () -> {
                            if (maybeReset()) {
                                wasReset.set(true);
                            }
                            return null;
                        },
                        this::doObserve);
            } else if (nativeBucketCreated) {
                maybeScaleDown(wasReset);
            }
            if (wasReset.get()) {
                // We just discarded the newly observed value. Observe it again.
                if (!buffer.append(value)) {
                    doObserve(value);
                }
            }
        }

        private void maybeScaleDown(AtomicBoolean wasReset) {
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
                        if (maybeReset()) {
                            wasReset.set(true);
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

        private boolean maybeReset() {
            if (!resetDurationExpired) {
                return false;
            }
            resetDurationExpired = false;
            buffer.reset();
            nativeBucketsForPositiveValues.clear();
            nativeBucketsForNegativeValues.clear();
            nativeZeroCount.reset();
            count.reset();
            sum.reset();
            for (int i = 0; i < classicBuckets.length; i++) {
                classicBuckets[i].reset();
            }
            nativeZeroThreshold = nativeMinZeroThreshold;
            nativeSchema = Histogram.this.initialNativeSchema;
            createdTimeMillis = System.currentTimeMillis();
            if (exemplarSampler != null) {
                exemplarSampler.reset();
            }
            maybeScheduleNextReset();
            return true;
        }

        private boolean maybeWidenZeroBucket() {
            if (nativeZeroThreshold >= nativeMaxZeroThreshold) {
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
            nativeZeroThreshold = newZeroThreshold;
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
                double previousBucketBoundary = calcUpperBound(schema, index - 1);
                if (Double.isFinite(previousBucketBoundary) && previousBucketBoundary < Double.MAX_VALUE) {
                    return Double.MAX_VALUE;
                }
            }
            return result;
        }

        private double calcUpperBound(int schema, int index) {
            // The actual formula is:
            // ---
            // base := 2^(2^-schema);
            // upperBound := base^index;
            // ---
            // The following implementation reduces the numerical error for index > 0.
            // It's not very efficient. We should refactor and use an algorithm as in client_golang's getLe()
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

        private void maybeScheduleNextReset() {
            if (nativeResetDurationSeconds > 0) {
                Scheduler.schedule(() -> resetDurationExpired = true, nativeResetDurationSeconds, TimeUnit.SECONDS);
            }
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
        private final double DEFAULT_MIN_ZERO_THRESHOLD = Math.pow(2.0, -128);
        private final double DEFAULT_MAX_ZERO_THRESHOLD = Math.pow(2.0, -128);

        private double[] classicUpperBounds = DEFAULT_CLASSIC_UPPER_BOUNDS;
        private int nativeSchema = 5;
        private double nativeMaxZeroThreshold = -1; // negative value means not set
        private double nativeMinZeroThreshold = -1; // negative value means not set
        private int nativeMaxBuckets = Integer.MAX_VALUE; // TODO: Default 160?

        private long nativeResetDurationSeconds = 0L;

        private Builder() {
            super(Collections.singletonList("le"));
        }

        /**
         * Use the native histogram representation only, i.e. don't maintain classic histogram buckets.
         * See {@link Histogram} for more info.
         */
        public Builder nativeHistogramOnly() {
            if (nativeSchema == CLASSIC_HISTOGRAM) {
                throw new IllegalArgumentException("Cannot call nativeHistogramOnly() after calling classicHistogramOnly().");
            }
            classicUpperBounds = null;
            return this;
        }

        /**
         * Use the classic histogram representation only, i.e. don't maintain native histogram buckets.
         * See {@link Histogram} for more info.
         */
        public Builder classicHistogramOnly() {
            if (classicUpperBounds == null) {
                throw new IllegalArgumentException("Cannot call classicHistogramOnly() after calling nativeHistogramOnly().");
            }
            nativeSchema = CLASSIC_HISTOGRAM;
            return this;
        }

        @Override
        public Histogram build() {
            return new Histogram(this);
        }

        /**
         * Set the upper bounds for the classic histogram buckets.
         * Default is {@link Histogram#DEFAULT_CLASSIC_UPPER_BOUNDS}.
         * If the +Inf bucket is missing it will be added.
         * If upperBounds contains duplicates the duplicates will be removed.
         */
        public Builder withClassicBuckets(double... upperBounds) {
            this.classicUpperBounds = upperBounds;
            for (double bound : upperBounds) {
                if (Double.isNaN(bound)) {
                    throw new IllegalArgumentException("Cannot use NaN as upper bound for a histogram");
                }
            }
            return this;
        }

        /**
         * Create classic histogram buckets with linear bucket boundaries.
         * <p>
         * Example: {@code withClassicLinearBuckets(1.0, 0.5, 10)} creates bucket boundaries
         * {@code [[1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0, 5.5]}.
         *
         * @param start is the first bucket boundary
         * @param width is the width of each bucket
         * @param count is the total number of buckets, including start
         */
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

        /**
         * Create classic histogram bucxkets with exponential boundaries.
         * <p>
         * Example: {@code withClassicExponentialBuckets(1.0, 2.0, 10)} creates bucket bounaries
         * {@code [1.0, 2.0, 4.0, 8.0, 16.0, 32.0, 64.0, 128.0, 256.0, 512.0]}
         *
         * @param start is the first bucket boundary
         * @param factor growth factor
         * @param count total number of buckets, including start
         */
        public Builder withClassicExponentialBuckets(double start, double factor, int count) {
            classicUpperBounds = new double[count];
            for (int i = 0; i < count; i++) {
                classicUpperBounds[i] = start * Math.pow(factor, i);
            }
            return this;
        }

        /**
         * The schema is a number in [-4, 8] defining the resolution of the native histogram. Default is 5.
         * <p>
         * The higher the schema, the finer the resolution.
         * Schema is Prometheus terminology. In OpenTelemetry it's called "scale".
         * <p>
         * Note that the schema for a histogram may be automatically decreased at runtime if the number
         * of native histogram buckets exceeds {@link #withNativeMaxBuckets(int)}.
         * <p>
         * The following table shows:
         * <ul>
         *     <li>factor: The growth factor for bucket boundaries, i.e. next bucket boundary = growth factor * previous bucket boundary.
         *     <li>max quantile error: The maximum error for quantiles calculated using the Prometheus histogram_quantile() function, relative to the observed value, assuming harmonic mean.
         * </ul>
         * <table border>
         *     <tr>
         *         <td>schema</td><td>factor</td><td>max quantile error</td>
         *     </tr>
         *     <tr>
         *         <td>-4</td><td>65.536</td>99%<td></td>
         *     </tr>
         *     <tr>
         *         <td>-3</td><td>256</td>99%<td></td>
         *     </tr>
         *     <tr>
         *         <td>-2</td><td>16</td><td>88%</td>
         *     </tr>
         *     <tr>
         *         <td>-1</td><td>4</td><td>60%</td>
         *     </tr>
         *     <tr>
         *         <td>0</td><td>2</td><td>33%</td>
         *     </tr>
         *     <tr>
         *         <td>1</td><td>1.4142...</td><td>17%</td>
         *     </tr>
         *     <tr>
         *         <td>2</td><td>1.1892...</td><td>9%</td>
         *     </tr>
         *     <tr>
         *         <td>3</td><td>1.1090...</td><td>4%</td>
         *     </tr>
         *     <tr>
         *         <td>4</td><td>1.0442...</td><td>2%</td>
         *     </tr>
         *     <tr>
         *         <td>5</td><td>1.0218...</td><td>1%</td>
         *     </tr>
         *     <tr>
         *         <td>6</td><td>1.0108...</td><td>0.5%</td>
         *     </tr>
         *     <tr>
         *         <td>7</td><td>1.0054...</td><td>0.3%</td>
         *     </tr>
         *     <tr>
         *         <td>8</td><td>1.0027...</td><td>0.1%</td>
         *     </tr>
         * </table>
         */
        public Builder withNativeSchema(int nativeSchema) {
            if (nativeSchema < -4 || nativeSchema > 8) {
                throw new IllegalArgumentException("Unsupported native histogram schema " + nativeSchema + ": expecting -4 <= schema <= 8.");
            }
            this.nativeSchema = nativeSchema;
            return this;
        }

        /**
         * Native histogram buckets get smaller and smaller the closer they get to zero.
         * To avoid wasting a lot of buckets for observations fluctuating around zero, we consider all
         * values in [-zeroThreshold, +zeroThreshold] to be equal to zero.
         * <p>
         * The zeroThreshold is initialized with minZeroThreshold, and will grow up to maxZeroThreshold if
         * the number of native histogram buckets exceeds nativeMaxBuckets.
         * <p>
         * Default is {@link #DEFAULT_MAX_ZERO_THRESHOLD}.
         */
        public Builder withNativeMaxZeroThreshold(double nativeMaxZeroThreshold) {
            if (nativeMaxZeroThreshold < 0) {
                throw new IllegalArgumentException("Illegal native max zero threshold " + nativeMaxZeroThreshold + ": must be >= 0");
            }
            this.nativeMaxZeroThreshold = nativeMaxZeroThreshold;
            if (nativeMinZeroThreshold == -1 && DEFAULT_MIN_ZERO_THRESHOLD > nativeMaxZeroThreshold) {
                nativeMinZeroThreshold = nativeMaxZeroThreshold;
            }
            return this;
        }

        /**
         * Native histogram buckets get smaller and smaller the closer they get to zero.
         * To avoid wasting a lot of buckets for observations fluctuating around zero, we consider all
         * values in [-zeroThreshold, +zeroThreshold] to be equal to zero.
         * <p>
         * The zeroThreshold is initialized with minZeroThreshold, and will grow up to maxZeroThreshold if
         * the number of native histogram buckets exceeds nativeMaxBuckets.
         * <p>
         * Default is {@link #DEFAULT_MIN_ZERO_THRESHOLD}.
         */
        public Builder withNativeMinZeroThreshold(double nativeMinZeroThreshold) {
            if (nativeMinZeroThreshold < 0) {
                throw new IllegalArgumentException("Illegal native min zero threshold " + nativeMinZeroThreshold + ": must be >= 0");
            }
            this.nativeMinZeroThreshold = nativeMinZeroThreshold;
            if (nativeMaxZeroThreshold == -1 && DEFAULT_MAX_ZERO_THRESHOLD < nativeMinZeroThreshold) {
                nativeMaxZeroThreshold = nativeMinZeroThreshold;
            }
            return this;
        }

        /**
         * Limit the number of native buckets.
         * <p>
         * If the number of native buckets exceeds the maximum, the {@link #withNativeSchema(int)} is decreased,
         * i.e. the resolution of the histogram is decreased to reduce the number of buckets.
         */
        public Builder withNativeMaxBuckets(int nativeMaxBuckets) {
            this.nativeMaxBuckets = nativeMaxBuckets;
            return this;
        }

        /**
         * If the histogram needed to be scaled down because {@link #withNativeMaxBuckets(int)} was exceeded,
         * reset the histogram after a certain time interval to go back to the original {@link #withNativeSchema(int)}.
         * <p>
         * Reset means all values are set to zero. A good value might be 24h.
         * <p>
         * Default is no reset.
         */
        public Builder withNativeResetInterval(long duration, TimeUnit unit) {
            // TODO: reset interval isn't tested yet
            if (duration <= 0) {
                throw new IllegalArgumentException(duration + ": value > 0 expected");
            }
            nativeResetDurationSeconds = unit.toSeconds(duration);
            return this;
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}

package io.prometheus.client;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SparseHistogram extends SimpleCollector<SparseHistogram.Child> {

    private final int schema; // integer in [-4, 8]
    private final double base; // base == 2^(2^-schema)
    private final double zeroThreshold;
    private final static double[][] bounds;

    public static Builder build() {
        return new Builder();
    }

    // There is no definition of a text format for sparse histograms yet.
    // We produce a format emulating explicit histograms here.
    // This will change once a format for sparse histograms is defined.
    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples.Sample> samples = new ArrayList<MetricFamilySamples.Sample>();
        for (Map.Entry<List<String>, SparseHistogram.Child> entry : children.entrySet()) {
            List<String> labelNamesWithLe = new ArrayList<String>(labelNames);
            labelNamesWithLe.add("bucket_index"); // quick hack for manual verification, will be removed
            labelNamesWithLe.add("le");
            SparseHistogram.Child child = entry.getValue();
            long count = 0;
            if (child.bucketsForNegativeValues.size() > 1 || child.bucketsForNegativeValues.get(Integer.MAX_VALUE).intValue() > 0) {
                List<Integer> bucketIndexes = new ArrayList<Integer>(child.bucketsForNegativeValues.keySet());
                Collections.sort(bucketIndexes, Collections.<Integer>reverseOrder());
                for (Integer bucketIndex : bucketIndexes) {
                    count = count + child.bucketsForNegativeValues.get(bucketIndex).intValue();
                    List<String> labelValuesWithGe = new ArrayList<String>(labelNamesWithLe.size());
                    labelValuesWithGe.addAll(entry.getKey());
                    labelValuesWithGe.add(Integer.MAX_VALUE == bucketIndex ? "-Inf" : Integer.toString(bucketIndex));
                    labelValuesWithGe.add(doubleToGoString(-bucketIndexToUpperBound(bucketIndex-1)));
                    samples.add(new MetricFamilySamples.Sample(fullname + "_bucket", labelNamesWithLe, labelValuesWithGe, count));
                }
            }
            List<Integer> bucketIndexes =  new ArrayList<Integer>(child.bucketsForPositiveValues.keySet());
            Collections.sort(bucketIndexes);
            for (Integer bucketIndex : bucketIndexes) {
                count = count + child.bucketsForPositiveValues.get(bucketIndex).intValue();
                List<String> labelValuesWithLe = new ArrayList<String>(labelNamesWithLe.size());
                labelValuesWithLe.addAll(entry.getKey());
                labelValuesWithLe.add(Integer.MAX_VALUE == bucketIndex ? "+Inf" : Integer.toString(bucketIndex));
                labelValuesWithLe.add(doubleToGoString(bucketIndexToUpperBound(bucketIndex)));
                samples.add(new MetricFamilySamples.Sample(fullname + "_bucket", labelNamesWithLe, labelValuesWithLe, count));
            }
            samples.add(new MetricFamilySamples.Sample(fullname + "_count", labelNames, entry.getKey(), child.count.intValue()));
            samples.add(new MetricFamilySamples.Sample(fullname + "_sum", labelNames, entry.getKey(), child.sum.doubleValue()));
            samples.add(new MetricFamilySamples.Sample(fullname + "_schema", labelNames, entry.getKey(), child.schema));
            samples.add(new MetricFamilySamples.Sample(fullname + "_zero_count", labelNames, entry.getKey(), child.zeroCount.doubleValue()));
            samples.add(new MetricFamilySamples.Sample(fullname + "_zero_threshold", labelNames, entry.getKey(), child.zeroThreshold));
            if (Environment.includeCreatedSeries()) {
                samples.add(new MetricFamilySamples.Sample(fullname + "_created", labelNames, entry.getKey(), child.created / 1000.0));
            }
        }

        return familySamplesList(Type.HISTOGRAM, samples);
    }

    private double bucketIndexToUpperBound(int i) {
        // returns Double.POSITIVE_INFINITY for i == Integer.MAX_VALUE
        return Math.pow(base, i);
    }

    @Override
    protected SparseHistogram.Child newChild() {
        return new SparseHistogram.Child(schema, zeroThreshold);
    }

    public static class Builder extends SimpleCollector.Builder<SparseHistogram.Builder, SparseHistogram> {

        private int schema;
        private double zeroThreshold = Double.MIN_NORMAL;

        public Builder withSchema(int schema) {
            if (schema < -4 || schema > 8) {
                throw new IllegalStateException("unsupported schema " + schema);
            }
            this.schema = schema;
            return this;
        }

        public Builder withZeroThreshold(double zeroThreshold) {
            if (zeroThreshold < 0 || Double.isNaN(zeroThreshold)) {
                throw new IllegalStateException(zeroThreshold + ": illegal zeroThreshold");
            }
            this.zeroThreshold = zeroThreshold;
            return this;
        }

        public static SparseHistogram.Builder build(String name, String help) {
            return new SparseHistogram.Builder().name(name).help(help);
        }

        public static SparseHistogram.Builder build() {
            return new SparseHistogram.Builder();
        }

        @Override
        public SparseHistogram create() {
            dontInitializeNoLabelsChild = true;
            return new SparseHistogram(this);
        }
    }

    SparseHistogram(Builder builder) {
        super(builder);
        this.schema = builder.schema; // The Builder guarantees that schema is in [-4, 8].
        this.zeroThreshold = builder.zeroThreshold;
        this.base = Math.pow(2, Math.pow(2, -schema));
        initializeNoLabelsChild();
    }

    public static class Child {
        private final int schema; // integer in [-4, 8]
        private final double zeroThreshold;
        private final ConcurrentHashMap<Integer, DoubleAdder> bucketsForPositiveValues = new ConcurrentHashMap<Integer, DoubleAdder>();
        private final ConcurrentHashMap<Integer, DoubleAdder> bucketsForNegativeValues = new ConcurrentHashMap<Integer, DoubleAdder>();
        private final DoubleAdder count = new DoubleAdder();
        private final DoubleAdder sum = new DoubleAdder();
        private final DoubleAdder zeroCount = new DoubleAdder();
        private final long created = System.currentTimeMillis();

        private Child(int schema, double zeroThreshold) {
            this.schema = schema;
            this.zeroThreshold = zeroThreshold;
            bucketsForPositiveValues.put(Integer.MAX_VALUE, new DoubleAdder());
            bucketsForNegativeValues.put(Integer.MAX_VALUE, new DoubleAdder());
        }

        public void observe(double value) {
            if (Double.isNaN(value)) {
                return;
            } else if (value > zeroThreshold) {
                observe(bucketsForPositiveValues, value);
            } else if (value < zeroThreshold) {
                observe(bucketsForNegativeValues, -value);
            } else {
                zeroCount.add(1);
            }
        }

        private void observe(ConcurrentHashMap<Integer, DoubleAdder> buckets, double value) {
            int bucketIndex = Double.isInfinite(value) ? Integer.MAX_VALUE : findBucketIndex(value);
            // debug: the IllegalStateException should never happen
            double base = Math.pow(2, Math.pow(2, -schema));
            if (!Double.isInfinite(value) && !(Math.pow(base, bucketIndex-1) < value && value <= Math.pow(base, bucketIndex))) {
                throw new IllegalStateException("Bucket index " + bucketIndex + ": Invariance violated: " + Math.pow(base, bucketIndex-1) + " < " + value + " <= " + Math.pow(base, bucketIndex));
            }
            DoubleAdder bucketCount = buckets.get(bucketIndex);
            if (bucketCount == null) {
                DoubleAdder newBucketCount = new DoubleAdder();
                DoubleAdder existingBucketCount = buckets.putIfAbsent(bucketIndex, newBucketCount);
                bucketCount = existingBucketCount == null ? newBucketCount : existingBucketCount;
            }
            bucketCount.add(1);
            sum.add(value);
            count.add(1);
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
            return findIndex(bounds[schema - 1], frac) + (exp - 1) * bounds[schema - 1].length;
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
    }

    static {
        // See bounds in client_golang's histogram implementation.
        bounds = new double[8][];
        for (int schema = 1; schema <= 8; schema++) {
            bounds[schema - 1] = new double[1 << schema];
            bounds[schema - 1][0] = 0.5;
            // https://github.com/open-telemetry/opentelemetry-proto/blob/main/opentelemetry/proto/metrics/v1/metrics.proto#L501
            double base = Math.pow(2, Math.pow(2, -schema));
            for (int i = 1; i < bounds[schema - 1].length; i++) {
                if (i % 2 == 0 && schema > 1) {
                    // Use previously calculated value for increased precision, see comment in client_golang's implementation.
                    bounds[schema - 1][i] = bounds[schema - 2][i / 2];
                } else {
                    bounds[schema - 1][i] = bounds[schema - 1][i - 1] * base;
                }
            }
        }
    }
}

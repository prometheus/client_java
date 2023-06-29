package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.config.MetricProperties;
import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.model.snapshots.Exemplars;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.Quantile;
import io.prometheus.metrics.model.snapshots.Quantiles;
import io.prometheus.metrics.model.snapshots.SummarySnapshot;
import io.prometheus.metrics.core.observer.DistributionDataPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;

public class Summary extends StatefulMetric<DistributionDataPoint, Summary.DataPoint> implements DistributionDataPoint {

    private final boolean exemplarsEnabled;
    private final List<CKMSQuantiles.Quantile> quantiles; // Can be empty, but can never be null.
    private final long maxAgeSeconds;
    private final int ageBuckets;

    private Summary(Builder builder, PrometheusProperties prometheusProperties) {
        super(builder);
        MetricProperties[] properties = getMetricProperties(builder, prometheusProperties);
        this.exemplarsEnabled = getConfigProperty(properties, MetricProperties::getExemplarsEnabled);
        this.quantiles = Collections.unmodifiableList(makeQuantiles(properties));
        this.maxAgeSeconds = getConfigProperty(properties, MetricProperties::getSummaryMaxAgeSeconds);
        this.ageBuckets = getConfigProperty(properties, MetricProperties::getSummaryNumberOfAgeBuckets);
    }

    private List<CKMSQuantiles.Quantile> makeQuantiles(MetricProperties[] properties) {
        List<CKMSQuantiles.Quantile> result = new ArrayList<>();
        double[] quantiles = getConfigProperty(properties, MetricProperties::getSummaryQuantiles);
        double[] errors = getConfigProperty(properties, MetricProperties::getSummaryQuantileErrors);
        if (quantiles != null) {
            for (int i=0; i<quantiles.length; i++) {
                result.add(new CKMSQuantiles.Quantile(quantiles[i], errors[i]));
            }
        }
        return result;
    }

    @Override
    protected boolean isExemplarsEnabled() {
        return exemplarsEnabled;
    }

    @Override
    public SummarySnapshot collect() {
        return (SummarySnapshot) super.collect();
    }

    @Override
    protected SummarySnapshot collect(List<Labels> labels, List<DataPoint> metricData) {
        List<SummarySnapshot.SummaryDataPointSnapshot> data = new ArrayList<>(labels.size());
        for (int i = 0; i < labels.size(); i++) {
            data.add(metricData.get(i).collect(labels.get(i)));
        }
        return new SummarySnapshot(getMetadata(), data);
    }

    @Override
    protected DataPoint newDataPoint() {
        return new DataPoint();
    }

    @Override
    public void observe(double amount) {
        getNoLabels().observe(amount);
    }

    @Override
    public void observeWithExemplar(double amount, Labels labels) {
        getNoLabels().observeWithExemplar(amount, labels);
    }


    public class DataPoint implements DistributionDataPoint {

        private final LongAdder count = new LongAdder();
        private final DoubleAdder sum = new DoubleAdder();
        private final TimeWindowQuantiles quantileValues;
        private final Buffer buffer = new Buffer();

        private final long createdTimeMillis = System.currentTimeMillis();

        private DataPoint() {
            if (quantiles.size() > 0) {
                quantileValues = new TimeWindowQuantiles(quantiles.toArray(new CKMSQuantiles.Quantile[]{}), maxAgeSeconds, ageBuckets);
            } else {
                quantileValues = null;
            }
        }

        @Override
        public void observe(double amount) {
            if (!buffer.append(amount)) {
                doObserve(amount);
            }
        }

        @Override
        public void observeWithExemplar(double amount, Labels labels) {
            // TODO: Exemplars for summaries not implemented yet
            observe(amount);
        }

        private void doObserve(double amount) {
            sum.add(amount);
            if (quantileValues != null) {
                quantileValues.insert(amount);
            }
            // count must be incremented last, because in collect() the count
            // indicates the number of completed observations.
            count.increment();
        }

        public SummarySnapshot.SummaryDataPointSnapshot collect(Labels labels) {
            return buffer.run(
                    expectedCount -> count.sum() == expectedCount,
                    // TODO Exemplars (are hard-coded as empty in the line below)
                    () -> new SummarySnapshot.SummaryDataPointSnapshot(count.sum(), sum.sum(), makeQuantiles(), labels, Exemplars.EMPTY, createdTimeMillis),
                    this::doObserve
            );
        }

        private List<CKMSQuantiles.Quantile> getQuantiles() {
            return quantiles;
        }

        private Quantiles makeQuantiles() {
            Quantile[] quantiles = new Quantile[getQuantiles().size()];
            for (int i = 0; i < getQuantiles().size(); i++) {
                CKMSQuantiles.Quantile quantile = getQuantiles().get(i);
                quantiles[i] = new Quantile(quantile.quantile, quantileValues.get(quantile.quantile));
            }
            return Quantiles.of(quantiles);
        }
    }


    public static class Builder extends StatefulMetric.Builder<Summary.Builder, Summary> {

        public static final long DEFAULT_MAX_AGE_SECONDS = TimeUnit.MINUTES.toSeconds(5);
        public static final int DEFAULT_AGE_BUCKETS = 5;
        private final List<CKMSQuantiles.Quantile> quantiles = new ArrayList<>();
        private Long maxAgeSeconds;
        private Integer ageBuckets;

        private Builder(PrometheusProperties properties) {
            super(Collections.singletonList("quantile"), properties);
        }

        public Builder quantile(double quantile, double error) {
            if (quantile < 0.0 || quantile > 1.0) {
                throw new IllegalArgumentException("Quantile " + quantile + " invalid: Expected number between 0.0 and 1.0.");
            }
            if (error < 0.0 || error > 1.0) {
                throw new IllegalArgumentException("Error " + error + " invalid: Expected number between 0.0 and 1.0.");
            }
            quantiles.add(new CKMSQuantiles.Quantile(quantile, error));
            return this;
        }

        public Builder maxAgeSeconds(long maxAgeSeconds) {
            if (maxAgeSeconds <= 0) {
                throw new IllegalArgumentException("maxAgeSeconds cannot be " + maxAgeSeconds);
            }
            this.maxAgeSeconds = maxAgeSeconds;
            return this;
        }

        public Builder ageBuckets(int ageBuckets) {
            if (ageBuckets <= 0) {
                throw new IllegalArgumentException("ageBuckets cannot be " + ageBuckets);
            }
            this.ageBuckets = ageBuckets;
            return this;
        }

        @Override
        protected MetricProperties toProperties() {
            double[] quantiles = null;
            double[] quantileErrors = null;
            if (!this.quantiles.isEmpty()) {
                quantiles = new double[this.quantiles.size()];
                quantileErrors = new double[this.quantiles.size()];
                for (int i = 0; i < this.quantiles.size(); i++) {
                    quantiles[i] = this.quantiles.get(i).quantile;
                    quantileErrors[i] = this.quantiles.get(i).epsilon;
                }
            }
            return MetricProperties.newBuilder()
                    .withExemplarsEnabled(exemplarsEnabled)
                    .withSummaryQuantiles(quantiles)
                    .withSummaryQuantileErrors(quantileErrors)
                    .withSummaryNumberOfAgeBuckets(ageBuckets)
                    .withSummaryMaxAgeSeconds(maxAgeSeconds)
                    .build();
        }

        @Override
        public MetricProperties getDefaultProperties() {
            return MetricProperties.newBuilder()
                    .withExemplarsEnabled(true)
                    .withSummaryNumberOfAgeBuckets(DEFAULT_AGE_BUCKETS)
                    .withSummaryMaxAgeSeconds(DEFAULT_MAX_AGE_SECONDS)
                    .build();
        }

        @Override
        public Summary build() {
            return new Summary(this, properties);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    public static Summary.Builder newBuilder() {
        return new Builder(PrometheusProperties.getInstance());
    }

    public static Summary.Builder newBuilder(PrometheusProperties config) {
        return new Builder(config);
    }
}

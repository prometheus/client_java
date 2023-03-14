package io.prometheus.metrics.core;

import io.prometheus.metrics.model.*;
import io.prometheus.metrics.observer.DistributionObserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;

public class Summary extends ObservingMetric<DistributionObserver, Summary.SummaryData> implements DistributionObserver {

    final List<CKMSQuantiles.Quantile> quantiles; // Can be empty, but can never be null.
    final long maxAgeSeconds;
    final int ageBuckets;

    private Summary(Builder builder) {
        super(builder);
        this.quantiles = Collections.unmodifiableList(new ArrayList<>(builder.quantiles));
        this.maxAgeSeconds = builder.maxAgeSeconds;
        this.ageBuckets = builder.ageBuckets;
    }

    @Override
    public SummarySnapshot collect() {
        return (SummarySnapshot) super.collect();
    }

    @Override
    protected SummarySnapshot collect(List<Labels> labels, List<SummaryData> metricData) {
        List<SummarySnapshot.SummaryData> data = new ArrayList<>(labels.size());
        for (int i=0; i<labels.size(); i++) {
            data.add(metricData.get(i).collect(labels.get(i)));
        }
        return new SummarySnapshot(getMetadata(), data);
    }

    @Override
    protected SummaryData newMetricData() {
        return new SummaryData();
    }

    @Override
    public void observe(double amount) {
        getNoLabels().observe(amount);
    }

    @Override
    public void observeWithExemplar(double amount, Labels labels) {
        getNoLabels().observeWithExemplar(amount, labels);
    }


    public class SummaryData extends MetricData<DistributionObserver> implements DistributionObserver {

        private final LongAdder count = new LongAdder();
        private final DoubleAdder sum = new DoubleAdder();
        private final TimeWindowQuantiles quantileValues;
        private final Buffer<SummarySnapshot.SummaryData> buffer = new Buffer<>();

        private final long createdTimeMillis = System.currentTimeMillis();

        private SummaryData() {
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

        public SummarySnapshot.SummaryData collect(Labels labels) {
            return buffer.run(
                    expectedCount -> count.sum() == expectedCount,
                    // TODO Exemplars (are hard-coded as empty in the line below)
                    () -> new SummarySnapshot.SummaryData(count.sum(), sum.sum(), makeQuantiles(), labels, Exemplars.EMPTY, createdTimeMillis),
                    this::doObserve
            );
        }

        @Override
        public DistributionObserver toObserver() {
            return this;
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


    public static class Builder extends ObservingMetric.Builder<Summary.Builder, Summary> {

        private final List<CKMSQuantiles.Quantile> quantiles = new ArrayList<>();
        private long maxAgeSeconds = TimeUnit.MINUTES.toSeconds(10);
        private int ageBuckets = 5;

        private Builder() {
            super(Collections.singletonList("quantile"));
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
        public Summary build() {
            return new Summary(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}

package io.prometheus.metrics.exporter.opentelemetry.otelmodel;

import io.prometheus.metrics.model.snapshots.DataPointSnapshot;
import io.prometheus.metrics.model.snapshots.Exemplars;
import io.prometheus.metrics.shaded.io_opentelemetry_1_28_0.api.common.Attributes;
import io.prometheus.metrics.shaded.io_opentelemetry_1_28_0.api.common.AttributesBuilder;
import io.prometheus.metrics.shaded.io_opentelemetry_1_28_0.sdk.metrics.data.Data;
import io.prometheus.metrics.shaded.io_opentelemetry_1_28_0.sdk.metrics.data.DoubleExemplarData;
import io.prometheus.metrics.shaded.io_opentelemetry_1_28_0.sdk.metrics.data.MetricDataType;
import io.prometheus.metrics.shaded.io_opentelemetry_1_28_0.sdk.metrics.data.PointData;
import io.prometheus.metrics.model.snapshots.Exemplar;
import io.prometheus.metrics.model.snapshots.Labels;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

abstract class PrometheusData<T extends PointData> implements Data<T> {

    private final MetricDataType type;

    public PrometheusData(MetricDataType type) {
        this.type = type;
    }

    public MetricDataType getType() {
        return type;
    }

    protected Attributes labelsToAttributes(Labels labels) {
        if (labels.isEmpty()) {
            return Attributes.empty();
        } else {
            AttributesBuilder builder = Attributes.builder();
            for (int i=0; i<labels.size(); i++) {
                builder.put(labels.getName(i), labels.getValue(i));
            }
            return builder.build();
        }
    }

    protected List<DoubleExemplarData> convertExemplar(Exemplar exemplar) {
        return convertExemplars(Exemplars.of(exemplar));
    }

    protected List<DoubleExemplarData> convertExemplars(Exemplars exemplars) {
        // TODO: Exemplars not implemented yet.
        return Collections.emptyList();
    }

    protected long getStartEpochNanos(DataPointSnapshot dataPoint) {
        return dataPoint.hasCreatedTimestamp() ? TimeUnit.MILLISECONDS.toNanos(dataPoint.getCreatedTimestampMillis()) : 0L;
    }

    protected long getEpochNanos(DataPointSnapshot dataPoint, long currentTimeMillis) {
        return dataPoint.hasScrapeTimestamp() ? TimeUnit.MILLISECONDS.toNanos(dataPoint.getScrapeTimestampMillis()) : TimeUnit.MILLISECONDS.toNanos(currentTimeMillis);
    }
}

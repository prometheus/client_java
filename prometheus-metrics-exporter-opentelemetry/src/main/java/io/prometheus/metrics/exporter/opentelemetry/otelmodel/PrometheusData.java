package io.prometheus.metrics.exporter.opentelemetry.otelmodel;

import io.prometheus.metrics.model.snapshots.*;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.api.common.Attributes;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.api.common.AttributesBuilder;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.api.trace.SpanContext;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.api.trace.TraceFlags;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.api.trace.TraceState;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.Data;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.DoubleExemplarData;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.MetricDataType;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.PointData;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.internal.data.ImmutableDoubleExemplarData;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
        if (exemplar == null) {
            return Collections.emptyList();
        }
        return convertExemplars(Exemplars.of(exemplar));
    }

    protected List<DoubleExemplarData> convertExemplars(Exemplars exemplars) {
        return StreamSupport.stream(exemplars.spliterator(), false)
                .map(this::toDoubleExemplarData)
                .collect(Collectors.toList());
    }

    protected DoubleExemplarData toDoubleExemplarData(Exemplar exemplar) {
        if (exemplar == null) {
            return null;
        }
        
        AttributesBuilder filteredAttributesBuilder = Attributes.builder();
        String traceId = null;
        String spanId = null;
        for (Label label : exemplar.getLabels()) {
            if (label.getName().equals(Exemplar.TRACE_ID)) {
                traceId = label.getValue();
            }
            else if (label.getName().equals(Exemplar.SPAN_ID)) {
                spanId = label.getValue();
            } else {
                filteredAttributesBuilder.put(label.getName(), label.getValue());
            }
        }
        Attributes filteredAttributes = filteredAttributesBuilder.build();

        SpanContext spanContext = (traceId != null && spanId != null) 
                ? SpanContext.create(traceId, spanId, TraceFlags.getSampled(), TraceState.getDefault()) 
                : SpanContext.getInvalid();

        return ImmutableDoubleExemplarData.create(
                filteredAttributes, 
                TimeUnit.MILLISECONDS.toNanos(exemplar.getTimestampMillis()), 
                spanContext,
                exemplar.getValue());
    }

    protected long getStartEpochNanos(DataPointSnapshot dataPoint) {
        return dataPoint.hasCreatedTimestamp() ? TimeUnit.MILLISECONDS.toNanos(dataPoint.getCreatedTimestampMillis()) : 0L;
    }

    protected long getEpochNanos(DataPointSnapshot dataPoint, long currentTimeMillis) {
        return dataPoint.hasScrapeTimestamp() ? TimeUnit.MILLISECONDS.toNanos(dataPoint.getScrapeTimestampMillis()) : TimeUnit.MILLISECONDS.toNanos(currentTimeMillis);
    }

}

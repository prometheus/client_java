package io.prometheus.metrics.model;

import java.util.Collection;

public final class MetricMetadata {
    private final String name;
    private final String help;
    private final MetricType type;
    private final String unit;

    public MetricMetadata(String name, String help, MetricType type, String unit) {
        this.name = name;
        this.help = help;
        this.type = type;
        this.unit = unit;
    }

    public String getName() {
        return name;
    }

    public String getHelp() {
        return help;
    }

    public MetricType getType() {
        return type;
    }

    public String getUnit() {
        return unit;
    }
}

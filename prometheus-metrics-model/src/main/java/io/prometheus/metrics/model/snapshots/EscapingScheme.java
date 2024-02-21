package io.prometheus.metrics.model.snapshots;

import static io.prometheus.metrics.model.snapshots.PrometheusNaming.ESCAPING_KEY;
import static io.prometheus.metrics.model.snapshots.PrometheusNaming.nameEscapingScheme;

public enum EscapingScheme {
    NO_ESCAPING("allow-utf-8"),
    UNDERSCORE_ESCAPING("underscores"),
    DOTS_ESCAPING("dots"),
    VALUE_ENCODING_ESCAPING("values"),
    ;

    public final String getValue() {
        return value;
    }

    private final String value;

    EscapingScheme(String value) {
        this.value = value;
    }

    public static EscapingScheme fromAcceptHeader(String acceptHeader) {
        for (String p : acceptHeader.split(";")) {
            String[] toks = p.split("=");
            if (toks.length != 2) {
                continue;
            }
            String key = toks[0].trim();
            String value = toks[1].trim();
            if (key.equals(ESCAPING_KEY)) {
                try {
                    return EscapingScheme.forString(value);
                } catch (IllegalArgumentException e) {
                    return nameEscapingScheme;
                }
            }
        }
        return nameEscapingScheme;
    }

    private static EscapingScheme forString(String value) {
        switch(value) {
            case "allow-utf-8":
                return NO_ESCAPING;
            case "underscores":
                return UNDERSCORE_ESCAPING;
            case "dots":
                return DOTS_ESCAPING;
            case "values":
                return VALUE_ENCODING_ESCAPING;
            default:
                throw new IllegalArgumentException("Unknown escaping scheme: " + value);
        }
    }

    public String toHeaderFormat() {
        return "; " + ESCAPING_KEY + "=" + value;
    }
}

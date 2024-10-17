package io.prometheus.metrics.exporter.pushgateway;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class SchemeTest {

    @Test
    void fromString() {
        assertThat(Scheme.HTTP).hasToString("http");
        assertThat(Scheme.HTTPS).hasToString("https");
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> Scheme.fromString("ftp"))
            .withMessage("ftp: Unsupported scheme. Expecting 'http' or 'https'.");
    }
}

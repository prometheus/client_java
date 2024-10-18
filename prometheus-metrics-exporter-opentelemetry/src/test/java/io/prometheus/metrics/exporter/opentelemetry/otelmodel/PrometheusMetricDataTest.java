package io.prometheus.metrics.exporter.opentelemetry.otelmodel;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.collect.ImmutableMap;
import io.prometheus.metrics.model.snapshots.Unit;
import org.junit.jupiter.api.Test;

class PrometheusMetricDataTest {

  ImmutableMap<Object, Object> translations =
      ImmutableMap.builder()
          .put("days", "d")
          .put("hours", "h")
          .put("minutes", "min")
          .put("seconds", "s")
          .put("milliseconds", "ms")
          .put("microseconds", "us")
          .put("nanoseconds", "ns")
          .put("bytes", "By")
          .put("kibibytes", "KiBy")
          .put("mebibytes", "MiBy")
          .put("gibibytes", "GiBy")
          .put("tibibytes", "TiBy")
          .put("kilobytes", "KBy")
          .put("megabytes", "MBy")
          .put("gigabytes", "GBy")
          .put("terabytes", "TBy")
          .put("meters", "m")
          .put("volts", "V")
          .put("amperes", "A")
          .put("joules", "J")
          .put("watts", "W")
          .put("grams", "g")
          .put("celsius", "Cel")
          .put("hertz", "Hz")
          .put("percent", "%")
          .build();

  @Test
  void convertUnit() {
    translations.forEach(
        (unit, expected) -> {
          assertEquals(expected, PrometheusMetricData.convertUnit(new Unit(unit.toString())));
        });
  }
}

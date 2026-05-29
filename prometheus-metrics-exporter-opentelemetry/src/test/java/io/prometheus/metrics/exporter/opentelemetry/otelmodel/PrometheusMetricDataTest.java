package io.prometheus.metrics.exporter.opentelemetry.otelmodel;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.*;

import io.prometheus.metrics.model.snapshots.Unit;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PrometheusMetricDataTest {

  Map<Object, Object> translations =
      Map.ofEntries(
          entry("days", "d"),
          entry("hours", "h"),
          entry("minutes", "min"),
          entry("seconds", "s"),
          entry("milliseconds", "ms"),
          entry("microseconds", "us"),
          entry("nanoseconds", "ns"),
          entry("bytes", "By"),
          entry("kibibytes", "KiBy"),
          entry("mebibytes", "MiBy"),
          entry("gibibytes", "GiBy"),
          entry("tibibytes", "TiBy"),
          entry("kilobytes", "KBy"),
          entry("megabytes", "MBy"),
          entry("gigabytes", "GBy"),
          entry("terabytes", "TBy"),
          entry("meters", "m"),
          entry("volts", "V"),
          entry("amperes", "A"),
          entry("joules", "J"),
          entry("watts", "W"),
          entry("grams", "g"),
          entry("celsius", "Cel"),
          entry("hertz", "Hz"),
          entry("percent", "%"));

  @Test
  void convertUnit() {
    translations.forEach(
        (unit, expected) -> {
          assertEquals(expected, PrometheusMetricData.convertUnit(new Unit(unit.toString())));
        });
  }
}

package io.prometheus.metrics.core.exemplars;

import java.lang.reflect.Field;
import java.util.function.LongSupplier;

public class ExemplarSamplerConfigTestUtil {

  private static ExemplarSamplerConfig getConfig(Object metric, String fieldName)
      throws NoSuchFieldException, IllegalAccessException {
    Field configField = metric.getClass().getDeclaredField(fieldName);
    configField.setAccessible(true);
    return (ExemplarSamplerConfig) configField.get(metric);
  }

  private static void setRetentionPeriod(ExemplarSamplerConfig config, String name, long value)
      throws IllegalAccessException, NoSuchFieldException {
    Field field = config.getClass().getDeclaredField(name);
    field.setAccessible(true);
    field.set(config, value);
  }

  public static void setMinRetentionPeriodMillis(Object metric, long value)
      throws NoSuchFieldException, IllegalAccessException {
    ExemplarSamplerConfig config = getConfig(metric, "exemplarSamplerConfig");
    setRetentionPeriod(config, "minRetentionPeriodMillis", value);
  }

  public static void setSampleIntervalMillis(Object metric, long value)
      throws NoSuchFieldException, IllegalAccessException {
    ExemplarSamplerConfig config = getConfig(metric, "exemplarSamplerConfig");
    setRetentionPeriod(config, "sampleIntervalMillis", value);
  }

  public static void setExemplarSamplerTimeSource(Object metric, LongSupplier timeSource)
      throws NoSuchFieldException, IllegalAccessException {
    Field timeSourceField = metric.getClass().getDeclaredField("exemplarSamplerTimeSource");
    timeSourceField.setAccessible(true);
    timeSourceField.set(metric, timeSource);
  }
}

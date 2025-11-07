package io.prometheus.metrics.config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import javax.annotation.Nullable;

class Util {

  @Nullable
  static String getProperty(String name, Map<Object, Object> properties) {
    Object object = properties.remove(name);
    if (object != null) {
      return object.toString();
    }
    return null;
  }

  @Nullable
  static Boolean loadBoolean(String name, Map<Object, Object> properties)
      throws PrometheusPropertiesException {
    String property = getProperty(name, properties);
    if (property != null) {
      if (!"true".equalsIgnoreCase(property) && !"false".equalsIgnoreCase(property)) {
        throw new PrometheusPropertiesException(
            String.format("%s: Expecting 'true' or 'false'. Found: %s", name, property));
      }
      return Boolean.parseBoolean(property);
    }
    return null;
  }

  @Nullable
  static List<Double> toList(@Nullable double... values) {
    if (values == null) {
      return null;
    }
    List<Double> result = new ArrayList<>(values.length);
    for (double value : values) {
      result.add(value);
    }
    return result;
  }

  @Nullable
  static String loadString(String name, Map<Object, Object> properties)
      throws PrometheusPropertiesException {
    return getProperty(name, properties);
  }

  @Nullable
  static String loadStringAddSuffix(String name, Map<Object, Object> properties, String suffix) {
    Object object = properties.remove(name);
    if (object != null) {
      return object + suffix;
    }
    return null;
  }

  @Nullable
  static List<String> loadStringList(String name, Map<Object, Object> properties)
      throws PrometheusPropertiesException {
    String property = getProperty(name, properties);
    if (property != null) {
      return Arrays.asList(property.split("\\s*,\\s*"));
    }
    return null;
  }

  @Nullable
  static List<Double> loadDoubleList(String name, Map<Object, Object> properties)
      throws PrometheusPropertiesException {
    String property = getProperty(name, properties);
    if (property != null) {
      String[] numbers = property.split("\\s*,\\s*");
      Double[] result = new Double[numbers.length];
      for (int i = 0; i < numbers.length; i++) {
        try {
          if ("+Inf".equals(numbers[i].trim())) {
            result[i] = Double.POSITIVE_INFINITY;
          } else {
            result[i] = Double.parseDouble(numbers[i]);
          }
        } catch (NumberFormatException e) {
          throw new PrometheusPropertiesException(
              name + "=" + property + ": Expecting comma separated list of double values");
        }
      }
      return Arrays.asList(result);
    }
    return null;
  }

  // Map is represented as "key1=value1,key2=value2"
  static Map<String, String> loadMap(String name, Map<Object, Object> properties)
      throws PrometheusPropertiesException {
    Map<String, String> result = new HashMap<>();
    String property = getProperty(name, properties);
    if (property != null) {
      String[] pairs = property.split(",");
      for (String pair : pairs) {
        if (pair.contains("=")) {
          String[] keyValue = pair.split("=", 2);
          if (keyValue.length == 2) {
            String key = keyValue[0].trim();
            String value = keyValue[1].trim();
            if (!key.isEmpty() && !value.isEmpty()) {
              result.putIfAbsent(key, value);
            }
          }
        }
      }
    }
    return result;
  }

  @Nullable
  static Integer loadInteger(String name, Map<Object, Object> properties)
      throws PrometheusPropertiesException {
    String property = getProperty(name, properties);
    if (property != null) {
      try {
        return Integer.parseInt(property);
      } catch (NumberFormatException e) {
        throw new PrometheusPropertiesException(
            name + "=" + property + ": Expecting integer value");
      }
    }
    return null;
  }

  @Nullable
  static Double loadDouble(String name, Map<Object, Object> properties)
      throws PrometheusPropertiesException {
    String property = getProperty(name, properties);
    if (property != null) {
      try {
        return Double.parseDouble(property);
      } catch (NumberFormatException e) {
        throw new PrometheusPropertiesException(name + "=" + property + ": Expecting double value");
      }
    }
    return null;
  }

  @Nullable
  static Long loadLong(String name, Map<Object, Object> properties)
      throws PrometheusPropertiesException {
    String property = getProperty(name, properties);
    if (property != null) {
      try {
        return Long.parseLong(property);
      } catch (NumberFormatException e) {
        throw new PrometheusPropertiesException(name + "=" + property + ": Expecting long value");
      }
    }
    return null;
  }

  @Nullable
  static Duration loadOptionalDuration(String name, Map<Object, Object> properties)
      throws PrometheusPropertiesException {

    Long value = loadLong(name, properties);

    assertValue(value, t -> t >= 0, "Expecting value >= 0.", null, name);

    if (value == null || value == 0) {
      return null;
    }
    return Duration.ofSeconds(value);
  }

  static <T extends Number> void assertValue(
      @Nullable T number,
      Predicate<T> predicate,
      String message,
      @Nullable String prefix,
      String name)
      throws PrometheusPropertiesException {
    if (number != null && !predicate.test(number)) {
      String fullKey = prefix == null ? name : prefix + "." + name;
      String fullMessage = String.format("%s: %s Found: %s", fullKey, message, number);
      throw new PrometheusPropertiesException(fullMessage);
    }
  }
}

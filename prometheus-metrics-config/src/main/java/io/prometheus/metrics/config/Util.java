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
  static String getProperty(String prefix, String propertyName, PropertySource propertySource) {
    return propertySource.getProperty(prefix, propertyName);
  }

  @Nullable
  static Boolean loadBoolean(String prefix, String propertyName, PropertySource propertySource)
      throws PrometheusPropertiesException {
    String property = getProperty(prefix, propertyName, propertySource);
    if (property != null) {
      String fullKey = prefix.isEmpty() ? propertyName : prefix + "." + propertyName;
      if (!"true".equalsIgnoreCase(property) && !"false".equalsIgnoreCase(property)) {
        throw new PrometheusPropertiesException(
            invalidValueMessage(fullKey, "Expecting 'true' or 'false'.", property));
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
  static String loadString(String prefix, String propertyName, PropertySource propertySource)
      throws PrometheusPropertiesException {
    return getProperty(prefix, propertyName, propertySource);
  }

  @Nullable
  static String loadStringAddSuffix(
      String prefix, String propertyName, PropertySource propertySource, String suffix) {
    String value = propertySource.getProperty(prefix, propertyName);
    if (value != null) {
      return value + suffix;
    }
    return null;
  }

  @Nullable
  static List<String> loadStringList(
      String prefix, String propertyName, PropertySource propertySource)
      throws PrometheusPropertiesException {
    String property = getProperty(prefix, propertyName, propertySource);
    if (property != null) {
      return Arrays.asList(property.split("\\s*,\\s*"));
    }
    return null;
  }

  @Nullable
  static List<Double> loadDoubleList(
      String prefix, String propertyName, PropertySource propertySource)
      throws PrometheusPropertiesException {
    String property = getProperty(prefix, propertyName, propertySource);
    if (property != null) {
      String fullKey = prefix.isEmpty() ? propertyName : prefix + "." + propertyName;
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
              invalidValueMessage(
                  fullKey, "Expecting comma separated list of double values", property));
        }
      }
      return Arrays.asList(result);
    }
    return null;
  }

  // Map is represented as "key1=value1,key2=value2"
  static Map<String, String> loadMap(
      String prefix, String propertyName, PropertySource propertySource)
      throws PrometheusPropertiesException {
    Map<String, String> result = new HashMap<>();
    String property = getProperty(prefix, propertyName, propertySource);
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
  static Integer loadInteger(String prefix, String propertyName, PropertySource propertySource)
      throws PrometheusPropertiesException {
    String property = getProperty(prefix, propertyName, propertySource);
    if (property != null) {
      String fullKey = prefix.isEmpty() ? propertyName : prefix + "." + propertyName;
      try {
        return Integer.parseInt(property);
      } catch (NumberFormatException e) {
        throw new PrometheusPropertiesException(
            invalidValueMessage(fullKey, "Expecting integer value", property));
      }
    }
    return null;
  }

  @Nullable
  static Double loadDouble(String prefix, String propertyName, PropertySource propertySource)
      throws PrometheusPropertiesException {
    String property = getProperty(prefix, propertyName, propertySource);
    if (property != null) {
      String fullKey = prefix.isEmpty() ? propertyName : prefix + "." + propertyName;
      try {
        return Double.parseDouble(property);
      } catch (NumberFormatException e) {
        throw new PrometheusPropertiesException(
            invalidValueMessage(fullKey, "Expecting double value", property));
      }
    }
    return null;
  }

  @Nullable
  static Long loadLong(String prefix, String propertyName, PropertySource propertySource)
      throws PrometheusPropertiesException {
    String property = getProperty(prefix, propertyName, propertySource);
    if (property != null) {
      String fullKey = prefix.isEmpty() ? propertyName : prefix + "." + propertyName;
      try {
        return Long.parseLong(property);
      } catch (NumberFormatException e) {
        throw new PrometheusPropertiesException(
            invalidValueMessage(fullKey, "Expecting long value", property));
      }
    }
    return null;
  }

  @Nullable
  static Duration loadOptionalDuration(
      String prefix, String propertyName, PropertySource propertySource)
      throws PrometheusPropertiesException {

    Long value = loadLong(prefix, propertyName, propertySource);

    assertValue(value, t -> t >= 0, "Expecting value >= 0.", prefix, propertyName);

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
      String propertyName)
      throws PrometheusPropertiesException {
    if (number != null && !predicate.test(number)) {
      String fullKey =
          (prefix == null || prefix.isEmpty()) ? propertyName : prefix + "." + propertyName;
      String fullMessage = String.format("%s: %s Found: %s", fullKey, message, number);
      throw new PrometheusPropertiesException(fullMessage);
    }
  }

  static String invalidValueMessage(String fullKey, String message, String found) {
    String separator = message.endsWith(".") ? " " : ". ";
    return fullKey + ": " + message + separator + "Found: " + escape(found);
  }

  static String escape(String value) {
    StringBuilder result = new StringBuilder(value.length() + 2);
    result.append('"');
    int maxLength = Math.min(value.length(), 100);
    for (int i = 0; i < maxLength; i++) {
      char c = value.charAt(i);
      switch (c) {
        case '\b':
          result.append("\\b");
          break;
        case '\t':
          result.append("\\t");
          break;
        case '\n':
          result.append("\\n");
          break;
        case '\f':
          result.append("\\f");
          break;
        case '\r':
          result.append("\\r");
          break;
        case '"':
          result.append("\\\"");
          break;
        case '\\':
          result.append("\\\\");
          break;
        default:
          if (c < 0x20 || c == 0x7f) {
            result.append(String.format("\\u%04x", (int) c));
          } else {
            result.append(c);
          }
      }
    }
    if (value.length() > maxLength) {
      result.append("...");
    }
    result.append('"');
    return result.toString();
  }
}

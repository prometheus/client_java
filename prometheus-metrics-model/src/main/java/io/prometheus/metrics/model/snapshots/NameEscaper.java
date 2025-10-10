package io.prometheus.metrics.model.snapshots;

import io.prometheus.metrics.config.EscapingScheme;

public class NameEscaper {
  /**
   * Escapes the incoming name according to the provided escaping scheme. Depending on the rules of
   * escaping, this may cause no change in the string that is returned (especially NO_ESCAPING,
   * which by definition is a noop). This method does not do any validation of the name.
   */
  public static String escapeName(String name, EscapingScheme scheme) {
    if (name.isEmpty() || !needsEscaping(name, scheme)) {
      return name;
    }

    StringBuilder escaped = new StringBuilder();
    switch (scheme) {
      case ALLOW_UTF8:
        return name;
      case UNDERSCORE_ESCAPING:
        for (int i = 0; i < name.length(); ) {
          int c = name.codePointAt(i);
          if (PrometheusNames.isValidLegacyChar(c, i)) {
            escaped.appendCodePoint(c);
          } else {
            escaped.append('_');
          }
          i += Character.charCount(c);
        }
        return escaped.toString();
      case DOTS_ESCAPING:
        // Do not early return for legacy valid names, we still escape underscores.
        for (int i = 0; i < name.length(); ) {
          int c = name.codePointAt(i);
          if (c == '_') {
            escaped.append("__");
          } else if (c == '.') {
            escaped.append("_dot_");
          } else if (PrometheusNames.isValidLegacyChar(c, i)) {
            escaped.appendCodePoint(c);
          } else {
            escaped.append("__");
          }
          i += Character.charCount(c);
        }
        return escaped.toString();
      case VALUE_ENCODING_ESCAPING:
        escaped.append("U__");
        for (int i = 0; i < name.length(); ) {
          int c = name.codePointAt(i);
          if (c == '_') {
            escaped.append("__");
          } else if (PrometheusNames.isValidLegacyChar(c, i)) {
            escaped.appendCodePoint(c);
          } else if (!PrometheusNames.isValidUtf8Char(c)) {
            escaped.append("_FFFD_");
          } else {
            escaped.append('_');
            escaped.append(Integer.toHexString(c));
            escaped.append('_');
          }
          i += Character.charCount(c);
        }
        return escaped.toString();
      default:
        throw new IllegalArgumentException("Invalid escaping scheme " + scheme);
    }
  }

  static boolean needsEscaping(String name, EscapingScheme scheme) {
    return !PrometheusNames.isValidLegacyMetricName(name)
        || (scheme == EscapingScheme.DOTS_ESCAPING && (name.contains(".") || name.contains("_")));
  }
}

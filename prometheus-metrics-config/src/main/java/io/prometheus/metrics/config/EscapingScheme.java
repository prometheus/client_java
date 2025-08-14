package io.prometheus.metrics.config;

import javax.annotation.Nullable;

public enum EscapingScheme {
  /** NO_ESCAPING indicates that a name will not be escaped. */
  NO_ESCAPING("allow-utf-8"),

  /** UNDERSCORE_ESCAPING replaces all legacy-invalid characters with underscores. */
  UNDERSCORE_ESCAPING("underscores"),

  /**
   * DOTS_ESCAPING is similar to UNDERSCORE_ESCAPING, except that dots are converted to `_dot_` and
   * pre-existing underscores are converted to `__`.
   */
  DOTS_ESCAPING("dots"),

  /**
   * VALUE_ENCODING_ESCAPING prepends the name with `U__` and replaces all invalid characters with
   * the Unicode value, surrounded by underscores. Single underscores are replaced with double
   * underscores.
   */
  VALUE_ENCODING_ESCAPING("values");

  private static final String ESCAPING_KEY = "escaping";

  /** Default escaping scheme for names when not specified. */
  public static final EscapingScheme DEFAULT = UNDERSCORE_ESCAPING;

  public final String getValue() {
    return value;
  }

  private final String value;

  EscapingScheme(String value) {
    this.value = value;
  }

  /**
   * fromAcceptHeader returns an EscapingScheme depending on the Accept header. Iff the header
   * contains an escaping=allow-utf-8 term, it will select NO_ESCAPING. If a valid "escaping" term
   * exists, that will be used. Otherwise, the global default will be returned.
   */
  public static EscapingScheme fromAcceptHeader(@Nullable String acceptHeader) {
    if (acceptHeader != null) {
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
            // If the escaping parameter is unknown, ignore it.
            return DEFAULT;
          }
        }
      }
    }
    return DEFAULT;
  }

  static EscapingScheme forString(String value) {
    switch (value) {
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

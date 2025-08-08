package io.prometheus.metrics.config;

/**
 * ValidationScheme is an enum for determining how metric and label names will be validated by this
 * library.
 */
public enum ValidationScheme {
  /**
   * LEGACY_VALIDATION is a setting that requires that metric and label names conform to the
   * original character requirements.
   */
  LEGACY_VALIDATION,

  /** UTF_8_VALIDATION only requires that metric and label names be valid UTF-8 strings. */
  UTF_8_VALIDATION
}

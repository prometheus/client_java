package io.prometheus.metrics.exporter.common;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

class InvalidQueryParameterException extends RuntimeException {

  InvalidQueryParameterException(String message) {
    super(message);
  }

  // decode with Charset is only available in Java 10+, but we want to support Java 8
  @SuppressWarnings("JdkObsolete")
  static String urlDecode(String value) throws UnsupportedEncodingException {
    try {
      return URLDecoder.decode(value, "UTF-8");
    } catch (IllegalArgumentException e) {
      throw new InvalidQueryParameterException("Invalid percent-encoding in query string");
    }
  }
}

package io.prometheus.metrics.exporter.common;

class InvalidQueryParameterException extends RuntimeException {

  InvalidQueryParameterException(String message) {
    super(message);
  }
}

package io.prometheus.client;

class NanoTimeProvider {
  long nanoTime() {
    return System.nanoTime();
  }
}

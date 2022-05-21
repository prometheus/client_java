package io.prometheus.client.exporter.common.formatter;

import io.prometheus.client.TextFormatter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class DefaultMetricsWriter extends TextFormatter.MetricsWriter {

  private final ByteArrayOutputStream buffer;

  public DefaultMetricsWriter(ByteArrayOutputStream buffer) {
    if (buffer == null) {
      throw new IllegalArgumentException();
    }

    this.buffer = buffer;
  }

  @Override
  public void write(byte[] bytes, int offset, int length) throws IOException {
    this.buffer.write(bytes, offset, length);
  }

  @Override
  public void write(char[] cbuf, int off, int len) throws IOException {
    int end = off + len;
    for (int idx = off; idx < end; idx++) {
      this.write(cbuf[idx]);
    }
  }

  @Override
  public void write(int c) throws IOException {
    this.buffer.write(c);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getBuffer() {
    return (T) this.buffer;
  }

  @Override
  public TextFormatter.MetricsWriter append(TextFormatter.MetricsWriter other) throws IOException {
    Object otherBuffer = other.getBuffer();
    if (!(otherBuffer instanceof ByteArrayOutputStream)) {
      throw new IllegalArgumentException();
    }

    ((ByteArrayOutputStream) otherBuffer).writeTo(this.buffer);
    return this;
  }

  @Override
  public void write(String str, int off, int len) throws IOException {
    int end = off + len;
    for (int idx = off; idx < end; idx++) {
      this.write(str.charAt(idx));
    }
  }

  @Override
  public void flush() throws IOException {
    // noop
  }

  @Override
  public void close() throws IOException {
    // noop
  }
}

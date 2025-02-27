package io.prometheus.metrics.expositionformats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;

class TextFormatUtilTest {

  @Test
  void writeEscapedLabelValue() throws IOException {
    assertEquals("aa\\\\bb\\\"cc\\ndd\\nee\\\\ff\\\"gg", escape("aa\\bb\"cc\ndd\nee\\ff\"gg"));
    assertEquals("\\\\", escape("\\"));
    assertEquals("\\\\\\\\", escape("\\\\"));
    assertEquals("text", escape("text"));
  }

  private static String escape(String s) throws IOException {
    StringWriter writer = new StringWriter();
    TextFormatUtil.writeEscapedLabelValue(writer, s);
    return writer.toString();
  }

  @Test
  void testWriteTimestamp() throws IOException {
    assertThat(writeTimestamp(true)).isEqualTo("1000");
    assertThat(writeTimestamp(false)).isEqualTo("1.000");
  }

  private static String writeTimestamp(boolean timestampsInMs) throws IOException {
    StringWriter writer = new StringWriter();
    TextFormatUtil.writeTimestamp(writer, 1000, timestampsInMs);
    return writer.toString();
  }
}

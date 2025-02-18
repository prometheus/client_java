package io.prometheus.metrics.expositionformats;

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
}

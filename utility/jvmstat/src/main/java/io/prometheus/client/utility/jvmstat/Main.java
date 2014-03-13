package io.prometheus.client.utility.jvmstat;

import io.prometheus.client.Prometheus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;

public class Main {
  public static void main(String ...unused) throws JvmstatMonitor.AttachmentError, IOException {

      new JvmstatMonitor().run();

    final ByteArrayOutputStream bs = new ByteArrayOutputStream();
    final PrintWriter pw = new PrintWriter(bs);

    Prometheus.defaultDumpJson(pw);
    pw.flush();
    bs.flush();
    return;
  }
}

package io.prometheus.metrics.expositionformats;

import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.TextFormat;
import com.google.protobuf.Timestamp;

public class ProtobufUtil {

  static Timestamp timestampFromMillis(long timestampMillis) {
    return Timestamp.newBuilder()
        .setSeconds(timestampMillis / 1000L)
        .setNanos((int) (timestampMillis % 1000L * 1000000L))
        .build();
  }

  public static String shortDebugString(MessageOrBuilder protobufData) {
    return TextFormat.printer().emittingSingleLine(true).printToString(protobufData);
  }
}

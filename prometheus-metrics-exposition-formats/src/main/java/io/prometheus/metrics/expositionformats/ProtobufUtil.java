package io.prometheus.metrics.expositionformats;

import io.prometheus.metrics.shaded.com_google_protobuf_3_25_3.Timestamp;

public class ProtobufUtil {

    static Timestamp timestampFromMillis(long timestampMillis) {
        return Timestamp.newBuilder()
                .setSeconds(timestampMillis / 1000L)
                .setNanos((int) (timestampMillis % 1000L * 1000000L))
                .build();
    }
}

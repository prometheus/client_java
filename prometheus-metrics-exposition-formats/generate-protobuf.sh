#!/bin/bash

set -e

# We use the shaded protobuf JAR from the protobuf-shaded module.
# I could not figure out how to use a protoc Maven plugin to use the shaded module, so I ran this command to generate the sources manually.

# The version string must be the same as in protobuf-shaded/pom.xml.
#export PROTOBUF_VERSION_STRING="3_21_7"
export PROTOBUF_VERSION_STRING="3_25_3"

rm -rf src/main/protobuf/*
curl -sL https://raw.githubusercontent.com/prometheus/client_model/master/io/prometheus/client/metrics.proto -o src/main/protobuf/metrics.proto
sed -i "s/java_package = \"io.prometheus.client\"/java_package = \"io.prometheus.metrics.expositionformats.generated.com_google_protobuf_${PROTOBUF_VERSION_STRING}\"/" src/main/protobuf/metrics.proto
rm -rf src/main/generated/*
protoc --java_out src/main/generated src/main/protobuf/metrics.proto
sed -i "s/com\\.google\\.protobuf/io.prometheus.metrics.shaded.com_google_protobuf_${PROTOBUF_VERSION_STRING}/g" "src/main/generated/io/prometheus/metrics/expositionformats/generated/com_google_protobuf_${PROTOBUF_VERSION_STRING}/Metrics.java"

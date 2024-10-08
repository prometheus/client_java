#!/bin/bash

set -euo pipefail

# We use the shaded protobuf JAR from the protobuf-shaded module.
# I could not figure out how to use a protoc Maven plugin to use the shaded module, so I ran this command to generate the sources manually.

TARGET_DIR=$1
PROTOBUF_VERSION_STRING=$2

echo "Generating protobuf sources for version $PROTOBUF_VERSION_STRING in $TARGET_DIR"

rm -rf src/main/protobuf/*
curl -sL https://raw.githubusercontent.com/prometheus/client_model/master/io/prometheus/client/metrics.proto -o src/main/protobuf/metrics.proto
sed -i "s/java_package = \"io.prometheus.client\"/java_package = \"io.prometheus.metrics.expositionformats.generated.com_google_protobuf_${PROTOBUF_VERSION_STRING}\"/" src/main/protobuf/metrics.proto
rm -rf $TARGET_DIR/*
protoc --java_out $TARGET_DIR src/main/protobuf/metrics.proto


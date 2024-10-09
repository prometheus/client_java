#!/bin/bash

set -euo pipefail

# We use the shaded protobuf JAR from the protobuf-shaded module.
# I could not figure out how to use a protoc Maven plugin to use the shaded module, so I ran this command to generate the sources manually.

TARGET_DIR=$1
PROTO_DIR=src/main/protobuf
PROTOBUF_VERSION_STRING=$2

echo "Generating protobuf sources for version $PROTOBUF_VERSION_STRING in $TARGET_DIR"

rm -rf TARGET_DIR || true
mkdir -p $TARGET_DIR
rm -rf $PROTO_DIR || true
mkdir -p $PROTO_DIR

curl -sL https://raw.githubusercontent.com/prometheus/client_model/master/io/prometheus/client/metrics.proto -o $PROTO_DIR/metrics.proto
sed -i "s/java_package = \"io.prometheus.client\"/java_package = \"io.prometheus.metrics.expositionformats.generated.com_google_protobuf_${PROTOBUF_VERSION_STRING}\"/" $PROTO_DIR/metrics.proto
protoc --java_out $TARGET_DIR $PROTO_DIR/metrics.proto

# stop the build if there class is not up-to-date
# show local changes
DIFF=$(git diff)
if [[ ${REQUIRE_PROTO_UP_TO_DATE:-false} == "true" && -n "$DIFF" ]]; then
  echo "Generated protobuf sources are not up-to-date. Please run 'PROTO_GENERATION=true mvn clean install' and commit the changes."
  echo "Local changes:"
  echo "$DIFF"
  exit 1
fi

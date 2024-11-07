#!/bin/bash

set -euo pipefail

# We use the shaded protobuf JAR from the protobuf-shaded module.
# I could not figure out how to use a protoc Maven plugin to use the shaded module, so I ran this command to generate the sources manually.

TARGET_DIR=$1
PROTO_DIR=src/main/protobuf
PROTOBUF_VERSION_STRING=$2
PROTOBUF_VERSION="${PROTOBUF_VERSION_STRING//_/.}"

echo "Generating protobuf sources for version $PROTOBUF_VERSION in $TARGET_DIR"

rm -rf $TARGET_DIR
mkdir -p $TARGET_DIR
rm -rf $PROTO_DIR || true
mkdir -p $PROTO_DIR

OLD_PACKAGE=$(sed -nE 's/import (io.prometheus.metrics.expositionformats.generated.*).Metrics;/\1/p' src/main/java/io/prometheus/metrics/expositionformats/internal/PrometheusProtobufWriterImpl.java)
PACKAGE="io.prometheus.metrics.expositionformats.generated.com_google_protobuf_${PROTOBUF_VERSION_STRING}"

if [[ $OLD_PACKAGE != "$PACKAGE" ]]; then
  echo "Replacing package $OLD_PACKAGE with $PACKAGE in all java files"
  find .. -type f -name "*.java" -exec sed -i "s/$OLD_PACKAGE/$PACKAGE/g" {} +
fi

curl -sL https://raw.githubusercontent.com/prometheus/client_model/master/io/prometheus/client/metrics.proto -o $PROTO_DIR/metrics.proto

sed -i "s/java_package = \"io.prometheus.client\"/java_package = \"$PACKAGE\"/" $PROTO_DIR/metrics.proto
protoc --java_out $TARGET_DIR $PROTO_DIR/metrics.proto
sed -i '1 i\//CHECKSTYLE:OFF: checkstyle' $(find src/main/generated/io -type f)
sed -i -e $'$a\\\n//CHECKSTYLE:ON: checkstyle' $(find src/main/generated/io -type f)

GENERATED_WITH=$(grep -oP '\/\/ Protobuf Java Version: \K.*' "$TARGET_DIR/${PACKAGE//\.//}"/Metrics.java)

if [[ $GENERATED_WITH != "$PROTOBUF_VERSION" ]]; then
  echo "Generated protobuf sources version $GENERATED_WITH does not match provided version $PROTOBUF_VERSION"
  echo "Please update the protoc version .tool-versions to the latest version of https://github.com/protocolbuffers/protobuf/releases"
  echo "Please use https://github.com/asdf-vm/asdf - this will use the version specified in .tool-versions"
  exit 1
fi

STATUS=$(git status --porcelain)
if [[ ${REQUIRE_PROTO_UP_TO_DATE:-false} == "true" && -n "$STATUS" ]]; then
  echo "Generated protobuf sources are not up-to-date. Please run 'PROTO_GENERATION=true mvn clean install' and commit the changes."
  echo "Local changes:"
  echo "$STATUS"
  exit 1
fi


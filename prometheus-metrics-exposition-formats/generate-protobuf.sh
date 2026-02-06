#!/bin/bash

set -euo pipefail

# We use the shaded protobuf JAR from the protobuf-shaded module.
# I could not figure out how to use a protoc Maven plugin to use the shaded module,
# so I ran this command to generate the sources manually.

# Use gsed on macOS (requires: brew install gnu-sed) for in-place edits
# BSD sed requires -i '' for in-place with no backup; GNU sed uses -i alone.
if [[ "$OSTYPE" == "darwin"* ]] && command -v gsed >/dev/null 2>&1; then
	SED='gsed'
	SED_I=(-i)
else
	SED='sed'
	# BSD sed: -i requires backup extension; '' = no backup
	[[ "$OSTYPE" == "darwin"* ]] && SED_I=(-i '') || SED_I=(-i)
fi

# Use mise-provided protoc if available
if command -v mise >/dev/null 2>&1; then
	PROTOC="mise exec -- protoc"
else
	PROTOC='protoc'
fi

TARGET_DIR=$1
PROTO_DIR=src/main/protobuf
PROTOBUF_VERSION_STRING=$2
PROTOBUF_VERSION="${PROTOBUF_VERSION_STRING//_/.}"

echo "Generating protobuf sources for version $PROTOBUF_VERSION in $TARGET_DIR"

rm -rf "$TARGET_DIR"
mkdir -p "$TARGET_DIR"
rm -rf $PROTO_DIR || true
mkdir -p $PROTO_DIR

OLD_PACKAGE=$($SED -nE 's/import (io.prometheus.metrics.expositionformats.generated.*).Metrics;/\1/p' src/main/java/io/prometheus/metrics/expositionformats/internal/PrometheusProtobufWriterImpl.java)
PACKAGE="io.prometheus.metrics.expositionformats.generated.com_google_protobuf_${PROTOBUF_VERSION_STRING}"

if [[ $OLD_PACKAGE != "$PACKAGE" ]]; then
	echo "Replacing package $OLD_PACKAGE with $PACKAGE in all java files"
	find .. -type f -name "*.java" -exec "${SED}" "${SED_I[@]}" "s/$OLD_PACKAGE/$PACKAGE/g" {} +
fi

curl -sL https://raw.githubusercontent.com/prometheus/client_model/master/io/prometheus/client/metrics.proto -o $PROTO_DIR/metrics.proto

"${SED}" "${SED_I[@]}" "s/java_package = \"io.prometheus.client\"/java_package = \"$PACKAGE\"/" $PROTO_DIR/metrics.proto
$PROTOC --java_out "$TARGET_DIR" $PROTO_DIR/metrics.proto
find src/main/generated/io -type f -exec "${SED}" "${SED_I[@]}" '1 i\
//CHECKSTYLE:OFF: checkstyle' {} \;
find src/main/generated/io -type f -exec "${SED}" "${SED_I[@]}" -e $'$a\\\n//CHECKSTYLE:ON: checkstyle' {} \;

GENERATED_WITH=$($SED -n 's/.*\/\/ Protobuf Java Version: \(.*\)/\1/p' "$TARGET_DIR/${PACKAGE//\.//}"/Metrics.java)

function help() {
	echo "Please use https://mise.jdx.dev/ - this will use the version specified in mise.toml"
	echo "Generated protobuf sources are not up-to-date. Please run 'mise run generate' and commit the changes."
	echo "NOTE:"
	echo "1. You should only run 'mise run generate' in a PR from renovate"
	echo "2. The PR should update both '<protobuf-java.version>' in pom.xml and protoc in mise.toml"
	echo "   - but at least <protobuf-java.version>. If not, wait until renovate updates the PR."
}

if [[ $GENERATED_WITH != "$PROTOBUF_VERSION" ]]; then
	echo "Generated protobuf sources version $GENERATED_WITH does not match provided version $PROTOBUF_VERSION"
	help
	exit 1
fi

STATUS=$(git status --porcelain)
if [[ ${REQUIRE_PROTO_UP_TO_DATE:-false} == "true" && -n "$STATUS" ]]; then
	help
	echo "Local changes:"
	echo "$STATUS"
	exit 1
fi
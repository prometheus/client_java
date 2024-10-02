#!/bin/bash

set -e

# We use the shaded protobuf JAR from the protobuf-shaded module.
# I could not figure out how to use a protoc Maven plugin to use the shaded module, so I ran this command to generate the sources manually.

rm -rf src/main/protobuf/*
curl -sL https://raw.githubusercontent.com/prometheus/client_model/master/io/prometheus/client/metrics.proto -o src/main/protobuf/metrics.proto
rm -rf src/main/generated/*
protoc --java_out src/main/generated src/main/protobuf/metrics.proto

#!/usr/bin/env bash

set -euo pipefail

function first_artifact_id() {
  local bom_file="$1"
  grep '<artifactId>' "$bom_file" | head -n 2 | tail -n 1 | sed 's/.*<artifactId>\(.*\)<\/artifactId>.*/\1/'
}

function add_dir() {
  local dir="$1"
  if [[ ! -d "$dir" ]]; then
    echo "Directory $dir does not exist."
    exit 1
  fi

  if [[ $ignore_dirs =~ $dir ]]; then
    echo "Skipping $dir"
    return
  fi

  if [[ ! -f "$dir/pom.xml" ]]; then
    echo "File $dir/pom.xml does not exist."
    exit 1
  fi

  artifact_id=$(first_artifact_id "$dir/pom.xml")
  if [[ -z "$artifact_id" ]]; then
    echo "No artifactId found in $dir/pom.xml"
    exit 1
  fi

  echo "Found artifactId '$artifact_id' in $dir/pom.xml"
  # add to want
  if [[ -z "${want+x}" ]]; then
    want="$artifact_id"
  else
    want="$want
$artifact_id"
  fi
}

declare want
ignore_dirs="prometheus-metrics-parent"

for dir in prometheus-metrics*; do
  add_dir "$dir"
done
for dir in prometheus-metrics-tracer/prometheus-metrics* ; do
  if [[ -d "$dir" ]]; then
    add_dir "$dir"
  fi
done

want=$(echo "$want" | sort | uniq)
have="$(grep '<artifactId>prometheus-metrics' prometheus-metrics-bom/pom.xml | sed 's/.*<artifactId>\(.*\)<\/artifactId>.*/\1/'| sort)"

if [[ "$want" != "$have" ]]; then
  echo "The BOM file prometheus-metrics-bom/bom.xml does not match the current directory contents."
  echo "Expected: $want"
  echo "Found: $have"

  diff -u <(echo "$have" ) <(echo "$want")

  exit 1
else
  echo "BOM file is up to date."
fi

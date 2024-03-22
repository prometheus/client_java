# Maintainer Notes

## Update Dependency Versions

Use the [Versions Maven Plugin](https://www.mojohaus.org/versions-maven-plugin/index.html). Rules are configured in [version-rules.xml](version-rules.xml).

```
./mvnw versions:use-latest-releases
```

The versions plugin does not catch the `otel.version` in `prometheus-metrics-exporter-opentelemetry`. This needs to be updated manually.

## Update Shaded Dependencies

There are two modules for shaded dependencies:
* `prometheus-metrics-shaded-opentelemetry`: OpenTelemetry data model.
* `prometheus-metrics-shaded-protobuf`: Google's protobuf library.

The shaded modules are commented out in the root `pom.xml`. Instead of using the shaded dependencies from the project, we use the latest shaded dependencies from Maven central (or from the local Maven repository in `~/.m2/repository/`). This way we can `include` the shaded package name directly. We find this easier than importing the original package name and have it renamed at build time.

In order to update dependencies of the shaded modules (like Google's protobuf library or the OpenTelemetry library), do the following:

Step 1: Install updated versions of the shaded dependencies in your local Maven repository.

* Update the dependency versions in the shaded modules (both `*.version` and `*.version.string`).
* `cd ./prometheus-metrics-shaded-dependencies ; ../mvnw install ; cd ..`

Step 2: Update `prometheus-metrics-expositon-formats`

* Change the version of the `prometheus-metrics-shaded-protobuf` dependency in `pom.xml` to `${project.version}`.
* Update `PROTOBUF_VERSION_STRING` in `generate-protobuf.sh` and run the script to update the source code.
* Use find-and-replace to update the version numbers in the imported package names in the source code of `prometheus-metrics-exposition-formats` and `prometheus-metrics-core`.

Step 3: Update `prometheus-metrics-exporter-opentelemetry`

* Change the version of the `prometheus-metrics-shaded-opentelemetry` dependency in `pom.xml` to `${project.version}`.
* Use find-and-replace to update the version numbers in the imported package names in the source code of `prometheus-metrics-exporter-opentelemetry`.

Step 4: Update `prometheus-metrics-bom`

* Set the shaded dependency version property to `${project.version}` in `prometheus-metrics-bom/pom.xml`

Step 5: Release

_see below_

## Release

Create a commit to temporarily add shaded dependencies to the project:

* Add the `prometheus-metrics-shaded-dependencies` module to the root `pom.xml`.
* Change the versions of the shaded dependencies to `${project.version}` in `prometheus-metrics-exporter-opentelemetry`, `prometheus-metrics-exposition-formats`, and `prometheus-metrics-bom`.

Release:

```
./mvnw release:prepare -DreleaseVersion=1.2.0 -DdevelopmentVersion=1.3.0-SNAPSHOT
./mvnw release:perform -DreleaseVersion=1.2.0 -DdevelopmentVersion=1.3.0-SNAPSHOT
```

`release:prepare` does GitHub tags and commits, while `release:perform` signs the artifacts and uploads them to the staging repositoring on [https://oss.sonatype.org](https://oss.sonatype.org).

After that, manually verify the uploaded artifacts on [https://oss.sonatype.org/#stagingRepositories](https://oss.sonatype.org/#stagingRepositories), click `Close` to trigger Sonatype's verification, and then `Release`.

Create a commit to remove dependencies from the build (undoing the first step):

* Comment out the `prometheus-metrics-shaded-dependencies` module to the root `pom.xml`.
* Change the versions of the shaded dependencies to the latest released version on Maven Central in `prometheus-metrics-exporter-opentelemetry`, `prometheus-metrics-exposition-formats`, and `prometheus-metrics-bom`.

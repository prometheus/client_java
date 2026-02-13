# Contributing

Prometheus uses GitHub to manage reviews of pull requests.

- If you have a trivial fix or improvement, go ahead and create a pull request,
  addressing (with `@...`) the maintainer of this repository (see
  [MAINTAINERS.md](MAINTAINERS.md)) in the
  description of the pull request.

- If you plan to do something more involved, first discuss your ideas
  on our [mailing list](https://groups.google.com/forum/?fromgroups#!forum/prometheus-developers).
  This will avoid unnecessary work and surely give you and us a good deal
  of inspiration.

## Formatting

This repository uses [Google Java Format](https://github.com/google/google-java-format) to format
the code.

Run `./mvnw spotless:apply` to format the code (only changed files) before committing.

Or run all the linters:

`mise run lint:super-linter`

## Running Tests

If you're getting errors when running tests:

- Make sure that the IDE uses only the "Maven Shade" dependency of "
  prometheus-metrics-exposition-formats" and the "prometheus-metrics-tracer\*" dependencies.

### Running native tests

```shell
mise --cd .mise/envs/native run native-test
```

### Avoid failures while running tests

- Use `-Dspotless.check.skip=true` to skip the formatting check during development.
- Use `-Dcoverage.skip=true` to skip the coverage check during development.
- Use `-Dcheckstyle.skip=true` to skip the checkstyle check during development.
- Use `-Dwarnings=-nowarn` to skip the warnings during development.

Combine all with

```shell
./mvnw install -DskipTests -Dspotless.check.skip=true -Dcoverage.skip=true \
  -Dcheckstyle.skip=true -Dwarnings=-nowarn
```

or simply

```shell
mise run compile
```

## Updating the Protobuf Java Classes

The generated protobuf `Metrics.java` lives in a versioned package
(e.g., `...generated.com_google_protobuf_4_33_5`) that changes with each
protobuf release. A stable extending class at
`...generated/Metrics.java` reexports all types so that consumer code
only imports from the version-free package. On protobuf upgrades only
the `extends` clause in the stable class changes.

In the failing PR from renovate, run:

```shell
mise run generate
```

The script will:

1. Re-generate the protobuf sources with the new version.
2. Update the versioned package name in all Java files
   (including the stable `Metrics.java` extends clause).

Add the updated files to Git and commit them.

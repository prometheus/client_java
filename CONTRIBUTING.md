# Contributing

Prometheus uses GitHub to manage reviews of pull requests.

* If you have a trivial fix or improvement, go ahead and create a pull request,
  addressing (with `@...`) the maintainer of this repository (see
  [MAINTAINERS.md](MAINTAINERS.md)) in the description of the pull request.

* If you plan to do something more involved, first discuss your ideas
  on our [mailing list](https://groups.google.com/forum/?fromgroups#!forum/prometheus-developers).
  This will avoid unnecessary work and surely give you and us a good deal
  of inspiration.

## Formatting

This repository uses [Google Java Format](https://github.com/google/google-java-format) to format the code.

Run `./mvnw spotless:apply` to format the code (only changed files) before committing.

## Running Tests

If you're getting errors when running tests:

- Make sure that the IDE uses only the "Maven Shade" dependency of "prometheus-metrics-exposition-formats" and the "prometheus-metrics-tracer*" dependencies.

### Avoid failures while running tests

- Use `-Dspotless.check.skip=true` to skip the formatting check during development.
- Use `-Dcoverage.skip=true` to skip the coverage check during development.
- Use `-Dcheckstyle.skip=true` to skip the checkstyle check during development.
- Use `-Dwarnings=-nowarn` to skip the warnings during development.

Combine all with `mvn test -DskipTests=true -Dspotless.check.skip=true -Dcoverage.skip=true -Dcheckstyle.skip=true -Dwarnings=-nowarn`.

## Updating the Protobuf Java Classes

Use `PROTO_GENERATION=true mvn clean install` to generate protobuf classes.

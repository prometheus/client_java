# AGENTS.md

This file provides guidance to AI coding agents when working
with code in this repository.

## Build Commands

This project uses Maven with mise for task automation.
The Maven wrapper (`./mvnw`) is used for all builds.

```bash
# Full CI build (clean + install + all checks)
mise run ci

# Quick compile without tests or checks (fastest)
mise run compile

# Run unit tests only (skips formatting/coverage/checkstyle)
mise run test

# Run all tests including integration tests
mise run test-all

# Run a single test class
./mvnw test -Dtest=CounterTest \
  -Dcoverage.skip=true -Dcheckstyle.skip=true

# Run a single test method
./mvnw test -Dtest=CounterTest#testIncrement \
  -Dcoverage.skip=true -Dcheckstyle.skip=true

# Run tests in a specific module
./mvnw test -pl prometheus-metrics-core \
  -Dcoverage.skip=true -Dcheckstyle.skip=true

# Regenerate protobuf classes (after protobuf dep update)
mise run generate
```

## Architecture

The library follows a layered architecture where metrics
flow from core types through a registry to exporters:

```text
prometheus-metrics-core (user-facing API)
         │
         ▼ collect()
prometheus-metrics-model (immutable snapshots)
         │
         ▼
prometheus-metrics-exposition-formats
         │
         ▼
Exporters (httpserver, servlet, pushgateway, otel)
```

### Key Modules

- **prometheus-metrics-core**: User-facing metric types
  (Counter, Gauge, Histogram, Summary, Info, StateSet).
  All metrics implement `Collector` with `collect()`.
- **prometheus-metrics-model**: Internal read-only immutable
  snapshot types returned by `collect()`.
  Contains `PrometheusRegistry` for metric registration.
- **prometheus-metrics-config**: Runtime configuration via
  properties files or system properties.
- **prometheus-metrics-exposition-formats**: Converts
  snapshots to Prometheus exposition formats.
- **prometheus-metrics-tracer**: Exemplar support with
  OpenTelemetry tracing integration.
- **prometheus-metrics-simpleclient-bridge**: Allows legacy
  simpleclient 0.16.0 metrics to work with the new registry.

### Instrumentation Modules

Pre-built instrumentations:
`prometheus-metrics-instrumentation-jvm`, `-caffeine`,
`-guava`, `-dropwizard`, `-dropwizard5`.

## Code Style

- **Formatter**: Google Java Format (enforced via flint)
- **Line length**: 100 characters
  (enforced for ALL files including Markdown, Java, YAML)
- **Indentation**: 2 spaces
- **Static analysis**: `Error Prone` with NullAway
  (`io.prometheus.metrics` package)
- **Logger naming**: Logger fields must be named `logger`
  (not `log`, `LOG`, or `LOGGER`)
- **Assertions in tests**: Use static imports from AssertJ
  (`import static ...Assertions.assertThat`)
- **Empty catch blocks**: Use `ignored` as the variable name
- **Markdown code blocks**: Always specify language
  (e.g., ` ```java`, ` ```bash`, ` ```text`)

## Pull Requests

- PR titles must use semantic/conventional prefixes, for example
  `feat: ...`, `fix: ...`, `docs: ...`, `chore: ...`, or
  `test: ...`.
- Do not prefix PR titles with `[codex]`.
- Match the PR title type to the primary user-facing change.

## Linting

Run `mise run lint:fix` before committing changes.
If output includes `fixed`, keep those changes.
If output includes `partial` or `review`, address the remaining issues and
run `mise run lint:fix` again.

Example output:
flint: fixed: gofmt — commit before pushing | partial: cargo-clippy

**CRITICAL**: These checks MUST be run before creating any
commits. CI will fail if these checks fail.

### Java Files

- **ALWAYS** run `mise run build` after modifying Java files
  to ensure:
  - Code formatting via flint
  - Static analysis (`Error Prone` with NullAway)
  - Checkstyle validation
  - Build succeeds (tests are skipped;
    run `mise run test` or `mise run test-all` for tests)

## Testing

- JUnit 5 (Jupiter) with `@Test` annotations
- AssertJ for fluent assertions
- Mockito for mocking
- **Test visibility**: Test classes and test methods must be
  package-protected (no `public` modifier)
- Integration tests are in `integration-tests/` and run
  during `verify` phase
- Acceptance tests use OATs framework:
  `mise run acceptance-test`

## Documentation

- Docs live under `docs/content/` and use `$version` as a
  placeholder for the library version
- When publishing GitHub Pages,
  `mise run set-release-version-github-pages` replaces
  `$version` with the latest Git tag across all
  `docs/content/**/*.md` files
  (the published site is not versioned)
- Use `$version` for the Prometheus client version and
  `$otelVersion-alpha` for the OTel instrumentation
  version — never hardcode them

## Java Version

Source compatibility: Java 8. Tests run on Java 25
(configured in `mise.toml`).

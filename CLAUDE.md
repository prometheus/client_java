# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

This project uses Maven with mise for task automation. The Maven wrapper (`./mvnw`) is used for all builds.

```bash
# Full CI build (clean + install + all checks)
mise run ci

# Quick compile without tests or checks (fastest for development)
mise run compile

# Run unit tests only (skips formatting/coverage/checkstyle checks)
mise run test

# Run all tests including integration tests
mise run test-all

# Format code with Google Java Format
mise run format
# or directly: ./mvnw spotless:apply

# Run a single test class
./mvnw test -Dtest=CounterTest -Dspotless.check.skip=true -Dcoverage.skip=true -Dcheckstyle.skip=true

# Run a single test method
./mvnw test -Dtest=CounterTest#testIncrement -Dspotless.check.skip=true -Dcoverage.skip=true -Dcheckstyle.skip=true

# Run tests in a specific module
./mvnw test -pl prometheus-metrics-core -Dspotless.check.skip=true -Dcoverage.skip=true -Dcheckstyle.skip=true

# Regenerate protobuf classes (after protobuf dependency update)
mise run generate
```

## Architecture

The library follows a layered architecture where metrics flow from core types through a registry to exporters:

```
prometheus-metrics-core (user-facing API)
         │
         ▼ collect()
prometheus-metrics-model (immutable snapshots)
         │
         ▼
prometheus-metrics-exposition-formats (text/protobuf/OpenMetrics)
         │
         ▼
Exporters (httpserver, servlet, pushgateway, opentelemetry)
```

### Key Modules

- **prometheus-metrics-core**: User-facing metric types (Counter, Gauge, Histogram, Summary, Info, StateSet). All metrics implement the `Collector` interface with a `collect()` method.
- **prometheus-metrics-model**: Internal read-only immutable snapshot types returned by `collect()`. Contains `PrometheusRegistry` for metric registration.
- **prometheus-metrics-config**: Runtime configuration via properties files or system properties.
- **prometheus-metrics-exposition-formats**: Converts snapshots to Prometheus exposition formats.
- **prometheus-metrics-tracer**: Exemplar support with OpenTelemetry tracing integration.
- **prometheus-metrics-simpleclient-bridge**: Allows legacy simpleclient 0.16.0 metrics to work with the new registry.

### Instrumentation Modules

Pre-built instrumentations: `prometheus-metrics-instrumentation-jvm`, `-caffeine`, `-guava`, `-dropwizard`, `-dropwizard5`.

## Code Style

- **Formatter**: Google Java Format (enforced via Spotless)
- **Line length**: 100 characters (enforced for ALL files including Markdown, Java, YAML, etc.)
- **Indentation**: 2 spaces
- **Static analysis**: Error Prone with NullAway (`io.prometheus.metrics` package)
- **Logger naming**: Logger fields must be named `logger` (not `log`, `LOG`, or `LOGGER`)
- **Assertions in tests**: Use static imports from AssertJ (`import static org.assertj.core.api.Assertions.assertThat`)
- **Empty catch blocks**: Use `ignored` as the exception variable name
- **Markdown code blocks**: Always specify language (e.g., ` ```java`, ` ```bash`, ` ```text`)

## Linting and Validation

**CRITICAL**: These checks MUST be run before creating any commits. CI will fail if these checks fail.

### Java Files

- **ALWAYS** run `mise run build` after modifying Java files to ensure:
  - Code formatting (Spotless with Google Java Format)
  - Static analysis (Error Prone with NullAway)
  - Checkstyle validation
  - Build succeeds (tests are skipped; run `mise run test` or `mise run test-all` to execute tests)

### Non-Java Files (Markdown, YAML, JSON, shell scripts, etc.)

- **ALWAYS** run `mise run lint:super-linter` after modifying non-Java files
- Super-linter will **auto-fix** many issues (formatting, trailing whitespace, etc.)
- It only reports ERROR-level issues (configured via `LOG_LEVEL=ERROR` in `.github/super-linter.env`)
- Common issues caught:
  - Lines exceeding 100 characters in Markdown files
  - Missing language tags in fenced code blocks
  - Table formatting issues
  - YAML/JSON syntax errors

### Running Linters

```bash
# After modifying Java files (run BEFORE committing)
mise run build

# After modifying non-Java files (run BEFORE committing)
mise run lint:super-linter
```

## Testing

- JUnit 5 (Jupiter) with `@Test` annotations
- AssertJ for fluent assertions
- Mockito for mocking
- **Test visibility**: Test classes and test methods must be package-protected (no `public` modifier)
- Integration tests are in `integration-tests/` and run during `verify` phase
- Acceptance tests use OATs framework: `mise run acceptance-test`

## Documentation

- Docs live under `docs/content/` and use `$version` as a placeholder for the library version
- When publishing GitHub Pages, `mise run set-release-version-github-pages` replaces `$version` with the latest git tag across all `docs/content/**/*.md` files (the published site is not versioned)
- Use `$version` for the Prometheus client version and `$otelVersion-alpha` for the OTel instrumentation version — never hardcode them

## Java Version

Source compatibility: Java 8. Tests run on Java 25 (configured in `mise.toml`).

# Code Quality Improvement Plan

This document tracks code quality improvements for the Prometheus Java Client library. Work through these items incrementally across sessions.

## High Priority

### 1. Add Missing Test Coverage for Exporter Modules
- [ ] `prometheus-metrics-exporter-common` - base module, no tests
- [ ] `prometheus-metrics-exporter-servlet-jakarta` - no tests
- [ ] `prometheus-metrics-exporter-servlet-javax` - no tests
- [ ] `prometheus-metrics-exporter-opentelemetry-otel-agent-resources` - no tests

### 2. Eliminate Dropwizard Module Duplication
- [ ] Create shared base class or use generics for `prometheus-metrics-instrumentation-dropwizard` and `prometheus-metrics-instrumentation-dropwizard5` (~297 lines each, nearly identical)

### 3. Address Technical Debt (TODOs)
- [ ] `prometheus-metrics-core/src/main/java/io/prometheus/metrics/core/metrics/Histogram.java:965` - "reset interval isn't tested yet"
- [ ] `prometheus-metrics-core/src/main/java/io/prometheus/metrics/core/metrics/Summary.java:205` - "Exemplars (are hard-coded as empty)"
- [ ] `prometheus-metrics-core/src/main/java/io/prometheus/metrics/core/metrics/SlidingWindow.java:18` - "synchronized implementation, room for optimization"
- [ ] `prometheus-metrics-config/src/main/java/io/prometheus/metrics/config/PrometheusPropertiesLoader.java:105` - "Add environment variables like EXEMPLARS_ENABLED"
- [ ] `prometheus-metrics-exporter-opentelemetry/src/main/java/io/prometheus/metrics/exporter/opentelemetry/PrometheusMetricProducer.java:44` - "filter configuration for OpenTelemetry exporter"
- [ ] `prometheus-metrics-config/src/main/java/io/prometheus/metrics/config/ExporterOpenTelemetryProperties.java:7` - "JavaDoc missing"

### 4. Improve Exception Handling
Replace broad `catch (Exception e)` with specific exception types:
- [ ] `prometheus-metrics-instrumentation-dropwizard5/src/main/java/.../DropwizardExports.java:237`
- [ ] `prometheus-metrics-instrumentation-caffeine/src/main/java/.../CacheMetricsCollector.java:229`
- [ ] `prometheus-metrics-exporter-opentelemetry/src/main/java/.../PrometheusInstrumentationScope.java:47`
- [ ] `prometheus-metrics-exporter-opentelemetry/src/main/java/.../OtelAutoConfig.java:115`
- [ ] `prometheus-metrics-instrumentation-jvm/src/main/java/.../JvmNativeMemoryMetrics.java:166`
- [ ] `prometheus-metrics-exporter-httpserver/src/main/java/.../HttpExchangeAdapter.java:115`

## Medium Priority

### 5. Add Branch Coverage to JaCoCo
- [ ] Update `pom.xml` to add branch coverage requirement (~50% minimum)
```xml
<limit>
  <counter>BRANCH</counter>
  <value>COVEREDRATIO</value>
  <minimum>0.50</minimum>
</limit>
```

### 6. Raise Minimum Coverage Thresholds
Current thresholds to review:
- [ ] `prometheus-metrics-exporter-httpserver` - 45% (raise to 60%)
- [ ] `prometheus-metrics-instrumentation-dropwizard5` - 50% (raise to 60%)
- [ ] `prometheus-metrics-exposition-textformats` - 50% (raise to 60%)
- [ ] `prometheus-metrics-instrumentation-jvm` - 55% (raise to 60%)

### 7. Add SpotBugs
- [ ] Add `spotbugs-maven-plugin` to `pom.xml`
- [ ] Configure with appropriate rule set

### 8. Narrow Checkstyle Suppressions
- [ ] Review `checkstyle-suppressions.xml` - currently suppresses ALL Javadoc checks globally
- [ ] Narrow to specific packages/classes that need exceptions

## Lower Priority

### 9. Refactor Large Classes
- [ ] `prometheus-metrics-core/src/main/java/.../Histogram.java` (978 lines) - consider extracting native histogram logic

### 10. Document Configuration Classes
- [ ] `PrometheusPropertiesLoader` - add JavaDoc
- [ ] `ExporterProperties` and related classes - add JavaDoc
- [ ] `ExporterOpenTelemetryProperties` - add JavaDoc (noted in TODO)

### 11. Consolidate Servlet Exporter Duplication
- [ ] Extract common logic from `servlet-jakarta` and `servlet-javax` into `exporter-common`

### 12. Add Mutation Testing
- [ ] Add Pitest (`pitest-maven`) for critical modules
- [ ] Start with `prometheus-metrics-core` and `prometheus-metrics-model`

---

## Progress Notes

_Add notes here as items are completed:_

| Date | Item | Notes |
|------|------|-------|
| | | |

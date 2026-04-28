---
title: OpenMetrics 2.0 Preview
weight: 2
---

The Prometheus Java client library includes experimental support for the OpenMetrics 2.0 text
format.

{{< hint type=warning >}}
OpenMetrics 2.0 support is opt-in, experimental, and subject to change while the specification is
still in draft.
{{< /hint >}}

{{< toc >}}

## Enable OpenMetrics 2.0

To switch OpenMetrics responses from the legacy OM1 writer to the OM2 writer, set:

```properties
io.prometheus.openmetrics2.enabled=true
```

Programmatic configuration:

```java
PrometheusProperties properties = PrometheusProperties.builder()
    .enableOpenMetrics2(om2 -> {})
    .build();
```

Enabling `enableOpenMetrics2(...)` also enables the top-level `enabled` flag automatically, so you
only need to configure the sub-flags you want.

With `enabled=true` alone:

- OpenMetrics requests use the OM2 writer.
- Metric names are preserved as written by the application.
- Optional OM2 features such as `composite_values`, `exemplar_compliance`, and
  `native_histograms` remain off.

To enable OM2 only when the scraper explicitly requests `version=2.0.0`, set:

```properties
io.prometheus.openmetrics2.enabled=true
io.prometheus.openmetrics2.content_negotiation=true
```

Programmatic equivalent:

```java
PrometheusProperties properties = PrometheusProperties.builder()
    .enableOpenMetrics2(om2 -> om2.contentNegotiation(true))
    .build();
```

## Naming Behavior

OpenMetrics 2.0 removes OM1 suffix rewriting.

- Counters do not get `_total` appended automatically.
- Units do not get appended automatically.
- Info metrics still end in `_info` because that is required by the spec.

Examples:

| Metric builder input               | OM1 output        | OM2 output     |
| ---------------------------------- | ----------------- | -------------- |
| `Counter("events")`                | `events_total`    | `events`       |
| `Counter("events_total")`          | `events_total`    | `events_total` |
| `Counter("req").unit(BYTES)`       | `req_bytes_total` | `req`          |
| `Counter("req_bytes").unit(BYTES)` | `req_bytes_total` | `req_bytes`    |
| `Info("target")`                   | `target_info`     | `target_info`  |

This means OpenMetrics 2.0 does not apply OM1 suffix behavior such as appending `_total` or unit
suffixes, while the legacy OpenMetrics 1.0 and Prometheus text formats keep that existing suffix
behavior.

## Feature Flags

All OpenMetrics 2.0 flags default to `false`.

| Property                                         | Effect                                                                                 |
| ------------------------------------------------ | -------------------------------------------------------------------------------------- |
| `io.prometheus.openmetrics2.enabled`             | Metric names are preserved as written by the application.                              |
| `io.prometheus.openmetrics2.content_negotiation` | Apply OM2 behavior only when the scraper requests `version=2.0.0`.                     |
| `io.prometheus.openmetrics2.composite_values`    | Emit histograms, summaries, and gauge histograms as single composite lines with `st@`. |
| `io.prometheus.openmetrics2.exemplar_compliance` | Emit only OM2-compliant exemplars with timestamps.                                     |
| `io.prometheus.openmetrics2.native_histograms`   | Emit OM2 native histogram text fields.                                                 |

Enable all flags at once:

```java
PrometheusProperties properties = PrometheusProperties.builder()
    .enableOpenMetrics2(om2 -> om2.enableAll())
    .build();
```

Equivalent properties:

```properties
io.prometheus.openmetrics2.enabled=true
io.prometheus.openmetrics2.content_negotiation=true
io.prometheus.openmetrics2.composite_values=true
io.prometheus.openmetrics2.exemplar_compliance=true
io.prometheus.openmetrics2.native_histograms=true
```

## Content Negotiation

If `content_negotiation=false`, OpenMetrics 2.0 behavior is applied to OpenMetrics responses even
if the scraper requested OpenMetrics 1.0.

If `content_negotiation=true`, OpenMetrics 2.0 behavior is only used when the scraper explicitly
requests `version=2.0.0`. Otherwise the legacy OpenMetrics 1.0 response is returned.

## Native Histograms

With `io.prometheus.openmetrics2.native_histograms=true`, the OpenMetrics 2.0 writer emits native
histogram fields such as:

- `schema`
- `zero_threshold`
- `zero_count`
- positive and negative spans
- positive and negative buckets

OM2 native histogram output can coexist with classic histogram buckets. When both are present, the
native histogram sample is written first.

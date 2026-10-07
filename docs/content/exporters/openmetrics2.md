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
- Counter and unit suffixes are appended so that series names remain compatible with OM1.
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

By default, the OpenMetrics 2.0 writer keeps OM1 suffix behavior so that switching formats does not
rename existing series:

- Counters get `_total` appended when it is missing.
- Unit suffixes are appended when they are missing.
- Existing suffixes are not duplicated.
- Info metrics end in `_info` because that is required by the spec.

Examples:

| Metric builder input               | OM1 and default OM2 output | OM2 with `suffixes=false` |
| ---------------------------------- | -------------------------- | ------------------------- |
| `Counter("events")`                | `events_total`             | `events`                  |
| `Counter("events_total")`          | `events_total`             | `events_total`            |
| `Counter("req").unit(BYTES)`       | `req_bytes_total`          | `req`                     |
| `Counter("req_bytes").unit(BYTES)` | `req_bytes_total`          | `req_bytes`               |
| `Info("target")`                   | `target_info`              | `target_info`             |

To emit metric names exactly as written by the application, set:

```properties
io.prometheus.openmetrics2.suffixes=false
```

## Feature Flags

OpenMetrics 2.0 feature flags default to `false`, except `suffixes`, which defaults to `true` to
preserve series names when migrating from OM1.

| Property                                         | Effect                                                                                 |
| ------------------------------------------------ | -------------------------------------------------------------------------------------- |
| `io.prometheus.openmetrics2.enabled`             | Enable the OpenMetrics 2.0 writer.                                                      |
| `io.prometheus.openmetrics2.content_negotiation` | Apply OM2 behavior only when the scraper requests `version=2.0.0`.                     |
| `io.prometheus.openmetrics2.composite_values`    | Emit histograms, summaries, and gauge histograms as single composite lines with `st@`. |
| `io.prometheus.openmetrics2.exemplar_compliance` | Emit only OM2-compliant exemplars with timestamps.                                     |
| `io.prometheus.openmetrics2.native_histograms`   | Emit OM2 native histogram text fields.                                                 |
| `io.prometheus.openmetrics2.suffixes`            | Append counter and unit suffixes to preserve OM1 series names.                         |

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
io.prometheus.openmetrics2.suffixes=true
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

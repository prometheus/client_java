---
title: Unicode
weight: 2
---

The Prometheus Java client library allows all Unicode characters, that can be encoded as UTF-8.

At scrape time, some characters are replaced based on the `encoding` header according
to
the [Escaping scheme](https://github.com/prometheus/docs/blob/main/docs/instrumenting/escaping_schemes.md). <!-- editorconfig-checker-disable-line -->

For example, if you use the `underscores` escaping scheme, dots in metric and label names are
replaced with underscores, so that the metric name `http.server.duration` becomes
`http_server_duration`.

Prometheus servers that do not support Unicode at all will not pass the `encoding` header, and the
Prometheus Java client library will replace dots, as well as any character that is not in the legacy
character set (`a-zA-Z0-9_:`), with underscores by default.

When `escaping=allow-utf-8` is passed, add valid UTF-8 characters to the metric and label names
without replacing them. This allows you to use dots in metric and label names, as well as
other UTF-8 characters, without any replacements.

## PushGateway

When using the [Pushgateway](/exporters/pushgateway/), Unicode support has to be enabled
explicitly by setting `io.prometheus.exporter.pushgateway.escapingScheme` to `allow-utf-8` in the
Pushgateway configuration file - see
[Pushgateway configuration]({{< relref "/config/config.md#exporter-pushgateway-properties" >}})

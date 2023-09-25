---
title: Formats
weight: 1
---

All exporters the following exposition formats:

* OpenMetrics text format
* Prometheus text format
* Prometheus protobuf format

Moreover, gzip encoding is supported for each of these formats.

Scraping with a Prometheus server
---------------------------------

The Prometheus server sends an `Accept` header to specify which format is requested. By default, the Prometheus server will scrape OpenMetrics text format with gzip encoding. If the Prometheus server is started with `--enable-feature=native-histograms`, it will scrape Prometheus protobuf format instead.

Viewing with a Web Browser
--------------------------

If you view the `/metrics` endpoint with your Web browser you will see Prometheus text format. For quick debugging of the other formats, exporters provide a `debug` URL parameter:

* `/metrics?debug=openmetrics`: View OpenMetrics text format.
* `/metrics?debug=text`: View Prometheus text format.
* `/metrics?debug=prometheus-protobuf`: View a text representation of the Prometheus protobuf format.

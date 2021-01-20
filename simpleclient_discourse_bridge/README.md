# Disource Exporter Bridge for Prometheus

Table of Contents
=================

  * [Overrview](#overview)
  * [Using](#using)
  * [Format](#format) 

## Overview

Bridge for [discourse/prometheus_exporter](https://github.com/discourse/prometheus_exporter).

## Using

```java
// Construct the bridge, host and port are optional arguments
DiscourseBridge bridge = new DscourseBridge("localhost", 9394);
// Push the default registry once.
bridge.push(CollectorRegistry.defaultRegistry);

// Push the default registry, with default interval of 60 seconds.
Thread thread = bridge.start(CollectorRegistry.defaultRegistry, 60);
// Stop pushing.
thread.interrupt();
thread.join();
```

## Format

Metrics are pushed over TCP in the JSON format,
```
{
    "help": "The metric description",
    "type": "counter",
    "name": "my_metric_name",
    "keys": {
        "my_label": "value"
    },
    "value": 1
    [,"prometheus_exporter_action": ("increment"|"decrement")]
    [,"opts":{}]
}
```

Writes an HTTP [chunked](https://tools.ietf.org/html/rfc7230#section-4.1) request in the JSON format above.

For more information, See [prometheus_exporter/client.rb](https://github.com/discourse/prometheus_exporter/blob/master/lib/prometheus_exporter/client.rb).

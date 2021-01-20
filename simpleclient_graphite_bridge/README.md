# Graphite Bridge for Prometheus

Table of Contents
=================

  * [Using](#using)

## Using

Metrics are pushed over TCP in the Graphite plaintext format.

```java
Graphite g = new Graphite("localhost", 2003);
// Push the default registry once.
g.push(CollectorRegistry.defaultRegistry);

// Push the default registry every 60 seconds.
Thread thread = g.start(CollectorRegistry.defaultRegistry, 60);
// Stop pushing.
thread.interrupt();
thread.join();
```

# Prometheus JVM Client
It supports Java, Clojure, Scala, JRuby, and anything else that runs on the JVM.

## Using
### Assets
If you use Maven, you can simply reference the following
assets:

  * The Client
    * groupId: _io.prometheus_
    * artifactId: _client_
    * version: _0.0.2-SNAPSHOT_
  * Hotspot VM Metrics
    * groupId: _io.prometheus.client.utility_
    * artifactId: _hotspot_
    * version: _0.0.2-SNAPSHOT_
  * Exposition Servlet - Transferring Metrics to Prometheus Servers
    * groupId: _io.prometheus.client.utility_
    * artifactId: _servlet_
    * version: _0.0.2-SNAPSHOT_

### Getting Started
There are canonical examples defined in the class definition Javadoc headers in the _io.prometheus.client.metrics_ package.

## Building

    $ mvn compile

## Testing

    $ mvn test

## Documentation
The client is canonically documented with Javadoc.  Running the following will produce output local documentation
in _apidocs_ directories for you to read.

    $ mvn package

If you use the Mavenized version of the Prometheus client, you can also instruct Maven to download the Javadoc and
source artifacts.

## Contact
  * All of the core developers are accessible via the [Prometheus Developers Mailinglist](https://groups.google.com/forum/?fromgroups#!forum/prometheus-developers).


[![Build Status](https://travis-ci.org/prometheus/client_java.png?branch=master)](https://travis-ci.org/prometheus/client_java)

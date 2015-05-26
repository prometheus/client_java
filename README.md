# Prometheus JVM Client
It supports Java, Clojure, Scala, JRuby, and anything else that runs on the JVM.

## Using
### Assets
If you use Maven, you can simply reference the assets below.  The latest
version can be found on in the maven repository for
[io.prometheus](http://mvnrepository.com/artifact/io.prometheus) and
[io.prometheus.client.utility](http://mvnrepository.com/artifact/io.prometheus.client.utility).

#### Simpleclient

```
<!-- The client -->
<dependency>
  <groupId>io.prometheus</groupId>
  <artifactId>simpleclient</artifactId>
  <version>0.0.6</version>
</dependency>
<!-- Hotspot JVM metrics-->
<dependency>
  <groupId>io.prometheus</groupId>
  <artifactId>simpleclient_hotspot</artifactId>
  <version>0.0.6</version>
</dependency>
<!-- Exposition servlet-->
<dependency>
  <groupId>io.prometheus</groupId>
  <artifactId>simpleclient_servlet</artifactId>
  <version>0.0.6</version>
</dependency>
<!-- Exposition HTTP server-->
<dependency>
  <groupId>io.prometheus</groupId>
  <artifactId>simpleclient_httpserver</artifactId>
  <version>0.0.6</version>
</dependency>
<!-- Pushgateway exposition-->
<dependency>
  <groupId>io.prometheus</groupId>
  <artifactId>simpleclient_pushgateway</artifactId>
  <version>0.0.6</version>
</dependency>
```

### POJO
If you don't want to use Maven, just expose the metrics, use
`simpleclient` + `simpleclient_common` + `simpleclient_httpserver`,
like in the [example](./simpleclient_httpserver/src/test/java/io/prometheus/client/exporter/ExampleExporter.java).
This has no other dependency than a JRE 1.6 and the mentioned simpleclient jars - no jetty, no servlet server, as it uses only `com.sun.net.httpserver.HttpServer`.


#### Original client
```
<!-- The client -->
<dependency>
  <groupId>io.prometheus</groupId>
  <artifactId>client</artifactId>
  <version>0.0.6</version>
</dependency>
<!-- Hotspot 'jvmstat/perfdata' metrics -->
<dependency>
  <groupId>io.prometheus.client.utility</groupId>
  <artifactId>jvmstat</artifactId>
  <version>0.0.6</version>
</dependency>
<!-- Hotspot 'jvmstat/perfdata' metrics -->
<dependency>
  <groupId>io.prometheus.client.utility</groupId>
  <artifactId>jvmstat</artifactId>
  <version>0.0.6</version>
</dependency>
<!-- Hotspot VM metrics -->
<dependency>
  <groupId>io.prometheus.client.utility</groupId>
  <artifactId>metrics</artifactId>
  <version>0.0.6</version>
</dependency>
<!-- Exposition servlet -->
<dependency>
  <groupId>io.prometheus.client.utility</groupId>
  <artifactId>servlet</artifactId>
  <version>0.0.6</version>
</dependency>
```

### Getting Started
There are canonical examples defined in the class definition Javadoc of the client packages.

## Documentation
The client is canonically documented with Javadoc.  Running the following will produce local documentation
in _apidocs_ directories for you to read.

    $ mvn package

If you use the Mavenized version of the Prometheus client, you can also instruct Maven to download the Javadoc and
source artifacts.

<strong>Alternatively</strong>, you can also look at the generated [Java Client
Github Project Page](http://prometheus.github.io/client_java), but the raw
Javadoc in Java source in version control should be treated as the canonical
form of documentation.

## Maintenance of this Library
This suite is built and managed by [Maven](http://maven.apache.org), and the
artifacts are hosted on the [Sonatype OSS Asset Repository](https://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide).

All contributions to this library must follow, as far as practical, the
prevalent patterns in the library for consistency and the following style
guide: [Google Java Style](http://goo.gl/FfwVsc).  Depending upon your
development environment, you may be able to find an automatic formatter
and adherence checker that follows these rules.

### Building

    $ mvn compile

### Testing

    $ mvn test

Please note that tests on Travis may be unreliable due to the absence of
installed Maven artifacts.  Ensure that the current snapshot version is
deployed to Sonatype OSS Repository.

###  Deployment
These steps below are only useful if you are in a release engineering capacity
and want to publicize these changes for external users.  You will also need to
have your local Maven setup correctly along with valid and public GPG key and
adequate authorization on the Sonatype OSS Repository to submit new artifacts,
be they _staging_ or _release_ ones.

You should read the [Sonatype OSS Maven Repository Usage
Guide](http://goo.gl/Sp9No5) before performing any of the following:

#### Staging
    $  mvn clean deploy -DperformRelease=true

#### Release
    $ mvn release:clean -DperformRelease=true
    $ mvn release:prepare -DperformRelease=true
    $ mvn release:perform -DperformRelease=true

#### Documentation
Documentation can also be released to the public via the Github Pages subproduct
through the magic _gh-pages_ branch for a Github project.  Documentation is
generated via the following command:

    $ mvn javadoc:aggregate

It will need to be automatically merged into the _gh-pages_ branch, but that is
as simple as this:

    $ git checkout master
    $ mvn javadoc:aggregate
    $ git checkout gh-pages
    $ mv target/site/apidocs/ ./
    $ git status
    $ # Amend the branch as necessary.
    $ git commit
    $ git push

There is a Maven plugin to perform this work, but it is broken.  The
javadoc:aggregate step will emit documentation into
_target/site/apidocs_.  The reason that we use this aggregate step instead
of bare javadoc is that we want one comprehensive Javadoc emission that includes
all Maven submodules versus trying to manually concatenate this together.

Output documentation lives in the [Java Client Github Project
Page](http://prometheus.github.io/client_java).


## Contact
  * All of the core developers are accessible via the [Prometheus Developers Mailinglist](https://groups.google.com/forum/?fromgroups#!forum/prometheus-developers).


[![Build Status](https://travis-ci.org/prometheus/client_java.png?branch=master)](https://travis-ci.org/prometheus/client_java)

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
<!-- Pushgateway exposition-->
<dependency>
  <groupId>io.prometheus</groupId>
  <artifactId>simpleclient_pushgateway</artifactId>
  <version>0.0.6</version>
</dependency>
```

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

You should read the [Sonatype OSS Apache Maven
Guide](http://central.sonatype.org/pages/apache-maven.html) before performing any of the following:

#### Staging
    $ mvn release:clean release:prepare -Prelease
    $ mvn release:perform -Prelease

#### Release

Go to https://oss.sonatype.org/#stagingRepositories and Close the `ioprometheus-XXX` release.
Once it's closed, Release it. Wait for the new version to appear in
[The Central Repository](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22io.prometheus%22).

Send an email to the developer's mailing list announcing the release.


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

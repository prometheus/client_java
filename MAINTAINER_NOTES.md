# Maintainer Notes

## Update Dependency Versions

Use the [Versions Maven Plugin](https://www.mojohaus.org/versions-maven-plugin/index.html). Rules are configured in [version-rules.xml](version-rules.xml).

```
./mvnw versions:use-latest-releases
```

## Release

```
./mvnw release:prepare -DreleaseVersion=0.16.0 -DdevelopmentVersion=0.16.1-SNAPSHOT
./mvnw release:perform -DreleaseVersion=0.16.0 -DdevelopmentVersion=0.16.1-SNAPSHOT
```

`release:prepare` does Github tags and commits, while `release:perform` signs the artifacts and uploads them to the staging repositoring on [https://oss.sonatype.org](https://oss.sonatype.org).

After that, manually verify the uploaded artifacts on [https://oss.sonatype.org/#stagingRepositories](https://oss.sonatype.org/#stagingRepositories), click `Close` to trigger Sonatype's verification, and then `Release`.

Note: We release only the parent module and the modules starting with simpleclient. Currently, we manually remove the benchmark and integration test modules. Todo: Instead of manually removing these modules, we should reconfigure the build to make sure that these modules aren't released.

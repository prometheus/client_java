## Update Version

In a new PR, update the version in `pom.xml` using 

```shell
mvn versions:set -DnewVersion=<VERSION>
```

Commit the changes and open a PR.

## Publish Release via Github Workflow
   
On main branch, create a tag for the new version to trigger the release workflow.

```sh
git tag -a v<VERSION> -m "Release v<VERSION>"
git push origin v<VERSION>
```

This will trigger the release workflow which will deploy the release to Maven Central.

## Create a Release 

Wait for the release to appear on Maven Central at 
https://mvnrepository.com/artifact/io.prometheus/prometheus-metrics-core. 
Once it is available, create a release on Github.

1. Go to https://github.com/prometheus/client_java/releases
2. Click on "Choose a tag", enter the tag name (e.g. `v0.1.0`), and click "Create a new tag".
3. Click on "Generate release notes" to auto-generate the release notes based on the commits since the last release.
4. Click on "Publish release".

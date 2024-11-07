## Update Version

In a new PR, update the version in `pom.xml` using 

```shell
mvn versions:set -DnewVersion=<VERSION>
```

Commit the changes and open a PR.

## Create a Release 

1. Go to https://github.com/prometheus/client_java/releases
2. Click on "Choose a tag", enter the tag name (e.g. `v0.1.0`), and click "Create a new tag".
3. Click on "Generate release notes" to auto-generate the release notes based on the commits since the last release.
4. Click on "Publish release".

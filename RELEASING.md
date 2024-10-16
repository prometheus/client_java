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

## Create a Release 

1. Go to https://github.com/prometheus/client_java/releases
2. Click on "Choose a tag", enter the tag name (e.g. `v0.1.0`), and click "Create a new tag".
3. Click on "Generate release notes" to auto-generate the release notes based on the commits since the last release.
4. Click on "Publish release".

## Old

Release:

```
./mvnw release:prepare -DreleaseVersion=1.2.0 -DdevelopmentVersion=1.3.0-SNAPSHOT
./mvnw release:perform -DreleaseVersion=1.2.0 -DdevelopmentVersion=1.3.0-SNAPSHOT
```

`release:prepare` does GitHub tags and commits, while `release:perform` signs the artifacts and uploads them to the staging repositoring on [https://oss.sonatype.org](https://oss.sonatype.org).

After that, manually verify the uploaded artifacts on [https://oss.sonatype.org/#stagingRepositories](https://oss.sonatype.org/#stagingRepositories), click `Close` to trigger Sonatype's verification, and then `Release`.

Create a commit to remove dependencies from the build (undoing the first step):

1. Trigger the release process at https://github.com/prometheus/client_java/actions/workflows/release.yml
2. Create a GitHub release at https://github.com/prometheus/client_java/releases matching the version you selected in the first step

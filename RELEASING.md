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

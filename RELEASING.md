# Releasing Instructions for Prometheus Java Client

## Before the Release

If there have been significant changes since the last release, update the
benchmarks before creating a new release:

```shell
mise run update-benchmarks
```

## Create a Release

1. Go to <https://github.com/prometheus/client_java/releases/new>
2. Click on "Choose a tag", enter the tag name (e.g. `v0.1.0`), and click "Create a new tag".
3. Click on "Generate release notes" to auto-generate the release notes based on the commits since
   the last release.
4. Click on "Publish release".

## Major or minor release

After the release is created, bump the snapshot version in all
`pom.xml` files:

```shell
# Auto-increment the minor version (e.g. 1.5.0-SNAPSHOT -> 1.6.0-SNAPSHOT)
mise run bump-snapshot

# Or specify an explicit version (e.g. for a major bump)
mise run bump-snapshot 2.0.0-SNAPSHOT
```

## If the GPG key expired

1. Generate a new key:
   <https://central.sonatype.org/publish/requirements/gpg/#generating-a-key-pair>
2. Distribute the
   key: <https://central.sonatype.org/publish/requirements/gpg/#distributing-your-public-key>
3. use `gpg --armor --export-secret-keys YOUR_ID` to
   export ([docs](https://github.com/actions/setup-java/blob/main/docs/advanced-usage.md#gpg))
4. Update the
   passphrase: <https://github.com/prometheus/client_java/settings/secrets/actions/GPG_SIGNING_PASSPHRASE> <!-- editorconfig-checker-disable-line -->
5. Update the GPG
   key: <https://github.com/prometheus/client_java/settings/secrets/actions/GPG_SIGNING_KEY>

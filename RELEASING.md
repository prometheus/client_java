# Releasing Instructions for Prometheus Java Client

Releases are automated via
[release-please](https://github.com/googleapis/release-please).

## How It Works

1. Commits to `main` using
   [Conventional Commits](https://www.conventionalcommits.org/) are
   tracked by release-please.
2. Release-please maintains a release PR that accumulates changes and
   updates the changelog.
3. When the release PR is merged, release-please creates a GitHub
   release and a `vX.Y.Z` tag.
4. The tag triggers the existing `release.yml` workflow, which deploys
   to Maven Central.
5. After tagging, release-please opens a follow-up PR to bump the
   SNAPSHOT version in all `pom.xml` files.

## Patch Release (default)

Simply merge the release PR — release-please bumps the patch version
by default (e.g. `1.5.0` -> `1.5.1`).

## Minor or Major Release

Add a `release-as: X.Y.0` footer to any commit on `main`:

```text
feat: add new feature

release-as: 1.6.0
```

Alternatively, edit the release PR title to
`chore(main): release 1.6.0`.

## Before the Release

If there have been significant changes since the last release, update
the benchmarks before merging the release PR:

```shell
mise run update-benchmarks
```

## If the GPG Key Expired

1. Generate a new key:
   <https://central.sonatype.org/publish/requirements/gpg/#generating-a-key-pair>
2. Distribute the key:
   <https://central.sonatype.org/publish/requirements/gpg/#distributing-your-public-key>
3. Use `gpg --armor --export-secret-keys YOUR_ID` to export
   ([docs](https://github.com/actions/setup-java/blob/main/docs/advanced-usage.md#gpg))
4. Update the passphrase:
   <https://github.com/prometheus/client_java/settings/secrets/actions/GPG_SIGNING_PASSPHRASE>
5. Update the GPG key:
   <https://github.com/prometheus/client_java/settings/secrets/actions/GPG_SIGNING_KEY>

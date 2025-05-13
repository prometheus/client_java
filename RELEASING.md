# Create a Release

1. Go to <https://github.com/prometheus/client_java/releases/new>
2. Click on "Choose a tag", enter the tag name (e.g. `v0.1.0`), and click "Create a new tag".
3. Click on "Generate release notes" to auto-generate the release notes based on the commits since
   the last release.
4. Click on "Publish release".

## If the GPG key expired

1. Generate a new key:
   <https://docs.github.com/en/authentication/managing-commit-signature-verification/generating-a-new-gpg-key#generating-a-gpg-key> <!-- editorconfig-checker-disable-line -->
2. BUT use `gpg --armor --export-secret-keys YOUR_ID` to
   export ([docs](https://github.com/actions/setup-java/blob/main/docs/advanced-usage.md#gpg))
3. Update the
   passphrase: <https://github.com/prometheus/client_java/settings/secrets/actions/GPG_SIGNING_PASSPHRASE> <!-- editorconfig-checker-disable-line -->
4. Update the GPG
   key: <https://github.com/prometheus/client_java/settings/secrets/actions/GPG_SIGNING_KEY>

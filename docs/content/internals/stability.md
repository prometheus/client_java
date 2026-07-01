---
title: API stability
weight: 2
---

The published Java API surface is marked with the
[`@StableApi`](/client_java/api/io/prometheus/metrics/annotations/StableApi.html) annotation. The
annotation is opt-in: only annotated types and members are part of the stable, published API and
follow semantic versioning — backwards-incompatible changes happen only in a major version bump.
Unannotated public types are not part of the stability contract and may change in any release.

`@StableApi` can be applied to a type to publish the type and its members, or to individual
constructors, methods, and fields when only part of a public type is stable.

## API diff check

CI runs [japicmp](https://siom79.github.io/japicmp/) against a pinned baseline release and writes
the published API diffs under `docs/apidiffs/current_vs_latest/`. Pull requests must keep those
checked-in diffs up to date. Run it locally with:

```bash
mise run api-diff
```

Raw reports are written to `**/target/japicmp/*`.

The baseline version is tracked in `pom.xml` and updated by Renovate; the published baseline diffs
are stored under `docs/apidiffs/`.

Pull requests that change `docs/apidiffs/current_vs_latest/` are automatically labeled
`api-change` for additional maintainer review. If the committed API diff contains breaking-change
markers, the pull request is also labeled `breaking-api-change`.

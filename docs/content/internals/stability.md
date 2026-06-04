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

CI runs [japicmp](https://siom79.github.io/japicmp/) against a pinned baseline release and fails on
incompatible changes to the `@StableApi` surface. Run it locally with:

```bash
mise run api-diff
```

Reports are written to `**/target/japicmp/*`.

The baseline version is tracked in `pom.xml` and updated by Renovate; the published baseline diffs
are stored under `docs/apidiffs/`.

## Accepting breaking changes

Backwards-incompatible changes to the `@StableApi` surface are only allowed in a major version
bump. Within a major version line, the API diff check must pass.

When a major version is being prepared, intentional incompatible changes can be accepted by adding
the `breaking-api-change-accepted` label to the pull request. The label is not an escape hatch for
minor or patch releases.

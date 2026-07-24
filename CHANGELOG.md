# Changelog

## [1.8.1](https://github.com/prometheus/client_java/compare/v1.8.0...v1.8.1) (2026-07-24)


### Bug Fixes

* avoid protobuf debug reflection in native images ([#2251](https://github.com/prometheus/client_java/issues/2251)) ([7f899e7](https://github.com/prometheus/client_java/commit/7f899e79ded325256bd0e444e33696b5f194700d))
* **deps:** update dependency io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom-alpha to v2.29.0-alpha ([#2235](https://github.com/prometheus/client_java/issues/2235)) ([cf9f702](https://github.com/prometheus/client_java/commit/cf9f70219e103c6004d8c315fdcfab2cfd7c447d))
* **deps:** update dependency io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom-alpha to v2.30.0-alpha ([#2328](https://github.com/prometheus/client_java/issues/2328)) ([1ca2716](https://github.com/prometheus/client_java/commit/1ca27164b646266a10ead6ad1e2e6b5648567b2f))
* **deps:** update dependency io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom-alpha to v2.30.0-alpha ([#2330](https://github.com/prometheus/client_java/issues/2330)) ([07623c1](https://github.com/prometheus/client_java/commit/07623c14dedfeffed3eace5b8718127add250668))
* **deps:** update dependency org.apache.tomcat.embed:tomcat-embed-core to v11.0.23 ([#2241](https://github.com/prometheus/client_java/issues/2241)) ([a017f80](https://github.com/prometheus/client_java/commit/a017f80980d91a5fa8ffe930c820f836c3d1b2ff))
* **deps:** update dependency org.apache.tomcat.embed:tomcat-embed-core to v11.0.24 ([#2294](https://github.com/prometheus/client_java/issues/2294)) ([63967bd](https://github.com/prometheus/client_java/commit/63967bd36ebc638234742ec58ad28f6098a92b3a))
* **deps:** update jetty monorepo to v12.1.11 ([#2279](https://github.com/prometheus/client_java/issues/2279)) ([4dc54da](https://github.com/prometheus/client_java/commit/4dc54da0c5b768fd66710bd3736b95116df445c5))
* **deps:** update junit-framework monorepo to v6.1.2 ([#2300](https://github.com/prometheus/client_java/issues/2300)) ([5966d1d](https://github.com/prometheus/client_java/commit/5966d1d4fdfc30e3a7eb09b0a88da5b2e9dc07c5))
* **deps:** update otel.instrumentation.version ([#2236](https://github.com/prometheus/client_java/issues/2236)) ([158230d](https://github.com/prometheus/client_java/commit/158230d2418a0b6d1b80c590e8fc63c9e444072c))
* **deps:** update protobuf to v4.35.1 ([#2221](https://github.com/prometheus/client_java/issues/2221)) ([cf17073](https://github.com/prometheus/client_java/commit/cf17073eebbdf67bc67dcbea2af30f27a0190079))
* include license files in release source jars ([#2250](https://github.com/prometheus/client_java/issues/2250)) ([08cf925](https://github.com/prometheus/client_java/commit/08cf925b564247a497437e29e4a64ebb335cd328)), closes [#2216](https://github.com/prometheus/client_java/issues/2216)
* stabilize OpenTelemetry exporter builder API ([#2257](https://github.com/prometheus/client_java/issues/2257)) ([09e6e2d](https://github.com/prometheus/client_java/commit/09e6e2de9261122b2e03620f9c6264f389f2fd25))


### Documentation

* document semantic PR title guidance ([#2318](https://github.com/prometheus/client_java/issues/2318)) ([5e813a0](https://github.com/prometheus/client_java/commit/5e813a0a8cd488b678fcc55dca85bb8d5c84dbad))

## [1.8.0](https://github.com/prometheus/client_java/compare/v1.7.0...v1.8.0) (2026-06-11)


### Features

* Add custom labels to exemplars ([#2191](https://github.com/prometheus/client_java/issues/2191)) ([fd1f3e8](https://github.com/prometheus/client_java/commit/fd1f3e85177ec4d4e4922f22f3aa79dc2dd7e17e))
* add MetricMetadata.Builder, deprecate wide constructors ([#2202](https://github.com/prometheus/client_java/issues/2202)) ([adeef32](https://github.com/prometheus/client_java/commit/adeef32f303a9dfadee9b7702b255db193c9c533))


### Bug Fixes

* Avoid unnuecessary exemplar allocations ([#2209](https://github.com/prometheus/client_java/issues/2209)) ([0b6a91f](https://github.com/prometheus/client_java/commit/0b6a91f2bafe0fa15f6fe828f315103d8c20f9f9))
* **deps:** update spring boot to v4.1.0 ([#2213](https://github.com/prometheus/client_java/issues/2213)) ([df25c08](https://github.com/prometheus/client_java/commit/df25c0821605b7edf7b87b9874a65d3d529592a5))


### Documentation

* cover typed family descriptors and @StableApi since v1.6.1 ([#2181](https://github.com/prometheus/client_java/issues/2181)) ([7ca9f99](https://github.com/prometheus/client_java/commit/7ca9f99b8f1731315d2cf8f68247fc94174a8b3b))

## [1.7.0](https://github.com/prometheus/client_java/compare/v1.6.1...v1.7.0) (2026-06-03)


### Features

* Add StableApi marker and API diff check ([#2168](https://github.com/prometheus/client_java/issues/2168)) ([768fd3a](https://github.com/prometheus/client_java/commit/768fd3a7aab5f11f3558a35c0d6257b5a217a078))
* add typed metric family descriptors ([#2114](https://github.com/prometheus/client_java/issues/2114)) ([9c3b097](https://github.com/prometheus/client_java/commit/9c3b097f6842ffc08fb3a2ed00217c73a6c2b191))
* track api-diff baseline via Renovate and store diffs in docs/apidiffs ([#2174](https://github.com/prometheus/client_java/issues/2174)) ([3adb890](https://github.com/prometheus/client_java/commit/3adb89078df4bf3d7739886612d4cf051176a6f3))


### Bug Fixes

* **deps:** update dependency com.github.ben-manes.caffeine:caffeine to v3.2.4 ([#2088](https://github.com/prometheus/client_java/issues/2088)) ([144eb61](https://github.com/prometheus/client_java/commit/144eb61030d412afe83631b8f341d2cb1595ab1c))
* **deps:** update dependency io.dropwizard.metrics:metrics-core to v4.2.39 ([#2139](https://github.com/prometheus/client_java/issues/2139)) ([5817d13](https://github.com/prometheus/client_java/commit/5817d1395dc348b6634ea169264fd13f4ad56e82))
* **deps:** update dependency io.dropwizard.metrics5:metrics-core to v5.0.7 ([#2140](https://github.com/prometheus/client_java/issues/2140)) ([261c451](https://github.com/prometheus/client_java/commit/261c4510eefe156ad688e019b9239cfcfd39bd2b))
* **deps:** update dependency io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom-alpha to v2.28.0-alpha ([#2126](https://github.com/prometheus/client_java/issues/2126)) ([b62b5d0](https://github.com/prometheus/client_java/commit/b62b5d0ab4b8d3a1335286bd3d36e8c9ac5aa269))
* **deps:** update dependency io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom-alpha to v2.28.0-alpha ([#2127](https://github.com/prometheus/client_java/issues/2127)) ([e11ce3d](https://github.com/prometheus/client_java/commit/e11ce3de19daf5acd2f73ffb90c96689c172f3c3))
* **deps:** update dependency io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom-alpha to v2.28.1-alpha ([#2132](https://github.com/prometheus/client_java/issues/2132)) ([b09be38](https://github.com/prometheus/client_java/commit/b09be3882f0ad95ff299db41d706a2e52faa7525))
* **deps:** update dependency io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom-alpha to v2.28.1-alpha ([#2133](https://github.com/prometheus/client_java/issues/2133)) ([a241c16](https://github.com/prometheus/client_java/commit/a241c165927d3cbb91b97eedd52de9c9eff595d0))
* **deps:** update dependency org.apache.tomcat.embed:tomcat-embed-core to v11.0.22 ([#2099](https://github.com/prometheus/client_java/issues/2099)) ([22125c5](https://github.com/prometheus/client_java/commit/22125c5f531467030793fc48cb2308ff14bbcaa7))
* **deps:** update jetty monorepo to v12.1.10 ([#2169](https://github.com/prometheus/client_java/issues/2169)) ([ddd3991](https://github.com/prometheus/client_java/commit/ddd3991096d409a3e58ae2003ce13457a51b8876))
* **deps:** update jetty monorepo to v12.1.9 ([#2102](https://github.com/prometheus/client_java/issues/2102)) ([04bee70](https://github.com/prometheus/client_java/commit/04bee70efff866f8c4966643926905c28a4eae3a))
* **deps:** update protobuf ([#2129](https://github.com/prometheus/client_java/issues/2129)) ([320538a](https://github.com/prometheus/client_java/commit/320538a09efad128c6d80bcc3d6eecca394603db))
* Reduce allocations for classic histogram buckets  ([#2081](https://github.com/prometheus/client_java/issues/2081)) ([edd160a](https://github.com/prometheus/client_java/commit/edd160ab93254c80250d7cf58a1dcb399fef67a1))
* restore legacy suffix compatibility ([#2100](https://github.com/prometheus/client_java/issues/2100)) ([b2ae70f](https://github.com/prometheus/client_java/commit/b2ae70ffd4ac0830fb567319beae9d1c3ad8bc2f))
* restore reserved suffix stripping in `PrometheusNaming.sanitizeMetricName()` ([#2124](https://github.com/prometheus/client_java/issues/2124)) ([2d0f508](https://github.com/prometheus/client_java/commit/2d0f508efd2f5e009b6f09f6a9ccb451cf9f3b6f))


### Performance Improvements

* Refactored sorting to use optimized sort algorithms ([#2161](https://github.com/prometheus/client_java/issues/2161)) ([25b94fc](https://github.com/prometheus/client_java/commit/25b94fc16273659892af0132cedb71f57597adf7))


### Documentation

* clarify downstream adapter validation requirements ([#2101](https://github.com/prometheus/client_java/issues/2101)) ([ef8c75c](https://github.com/prometheus/client_java/commit/ef8c75cf352bddd0d3a2052c3f1b0c8b6103a6f4))
* Document OM2  ([#2059](https://github.com/prometheus/client_java/issues/2059)) ([45d753c](https://github.com/prometheus/client_java/commit/45d753c418f005fbb17bf7caca3dc94655717687))
* document PushGateway shading workaround ([#2106](https://github.com/prometheus/client_java/issues/2106)) ([8ca0eb8](https://github.com/prometheus/client_java/commit/8ca0eb8d79b800ad8d7a08f10762ed631f4f2a70))

## [1.6.1](https://github.com/prometheus/client_java/compare/v1.6.0...v1.6.1) (2026-04-27)

> Note: With the OM2 metric-name preservation fix in this release, OpenMetrics 2.0 can now be
> tested. It is still in progress and not ready for general use yet.

### Bug Fixes

* Preserve original metric names in OM2 output ([#2058](https://github.com/prometheus/client_java/issues/2058)) ([59a7a6d](https://github.com/prometheus/client_java/commit/59a7a6d4d5a9eb31c33167764b11ba96d6625b74))


### Documentation

* clarify 1.6.0 release notes ([#2062](https://github.com/prometheus/client_java/issues/2062)) ([9e5d591](https://github.com/prometheus/client_java/commit/9e5d591f4c2e8e0d39ce5141ac14fff057b09c67))
* Document semantic PR title guidance ([#2060](https://github.com/prometheus/client_java/issues/2060)) ([7277889](https://github.com/prometheus/client_java/commit/727788942cccbecfa57d75eee9fb3e942083a95e))

## [1.6.0](https://github.com/prometheus/client_java/compare/v1.5.1...v1.6.0) (2026-04-25)

> Note: OpenMetrics 2.0 support is still in progress and not ready for general use yet.
>
> As part of the OM2 work, metric-name suffix handling moved from metric creation time to scrape
> time. A positive side effect is that metric names are now more flexible across the board, for
> example names ending in suffixes like `_total` are accepted where they were previously rejected.
> To keep the Prometheus and OM1 output unambiguous, the registry tracks claimed exposition names
> and still rejects registrations that would collide at scrape time.
>
> Downstream adapter libraries that implement `MultiCollector` need their registration-time
> metadata to match the metric families they emit at scrape time. When upgrading to 1.6.0+, adapter
> registration metadata needs to stay aligned with emitted names, types, label names, and suffix
> behavior under the new collision model.
> See also: [Validation at registration only](docs/content/getting-started/registry.md#validation-at-registration-only)
>
> | Example | Before 1.6.0 | Since 1.6.0 | Reason |
> | --- | --- | --- | --- |
> | `Gauge("foo_total")` | Rejected | Allowed | Not breaking because this previously failed at registration, so no working setup changes behavior, and safe because `_total` suffix expansion applies to counters, not gauges. |
> | `Counter("events_total")` | Rejected | Allowed | Not breaking because the OM1 output is still `events_total`; only the builder now accepts the name. |
> | `Gauge("foo_total")` + `Histogram("foo")` | Rejected | Allowed | Not breaking because this combination used to be blocked even though the exposed names do not overlap. |
> | `Gauge("events_total")` + `Counter("events")` | Rejected | Rejected | Not breaking because the ambiguous OM1 output would still expose two `events_total` series. |
> | `Gauge("foo_count")` + `Histogram("foo")` | Allowed | Rejected | Intentionally breaking because the old behavior could expose a conflicting `foo_count` name at scrape time. |

### Features

* Relax metric name validation in Dropwizard5 ([#1985](https://github.com/prometheus/client_java/issues/1985)) ([deb782f](https://github.com/prometheus/client_java/commit/deb782f9fce60ffb1308a98b661c0a1ccb79a82b))


### Bug Fixes

* **deps:** update dependency com.google.guava:guava to v33.6.0-jre ([#2021](https://github.com/prometheus/client_java/issues/2021)) ([1382693](https://github.com/prometheus/client_java/commit/13826930b9c2f566040a6929090ef23c94e81796))
* **deps:** update dependency commons-io:commons-io to v2.22.0 ([#2044](https://github.com/prometheus/client_java/issues/2044)) ([9e05c1d](https://github.com/prometheus/client_java/commit/9e05c1d56b7b0de17ba5aaaa300eb6433cc70824))
* **deps:** update dependency io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom-alpha to v2.27.0-alpha ([#2022](https://github.com/prometheus/client_java/issues/2022)) ([30ac534](https://github.com/prometheus/client_java/commit/30ac534d860fb7c60a1e7835723a6cf0035ea7f7))
* **deps:** update dependency io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom-alpha to v2.27.0-alpha ([#2023](https://github.com/prometheus/client_java/issues/2023)) ([2d51a32](https://github.com/prometheus/client_java/commit/2d51a3251f6943cf1b03ba9ea8778ff052f83ef9))
* **deps:** update dependency io.prometheus:prometheus-metrics-bom to v1.5.1 ([#2004](https://github.com/prometheus/client_java/issues/2004)) ([650ce4b](https://github.com/prometheus/client_java/commit/650ce4b677f2ca65f5877e77260e403fa85533db))
* **deps:** update dependency org.apache.tomcat.embed:tomcat-embed-core to v11.0.21 ([#2005](https://github.com/prometheus/client_java/issues/2005)) ([7a36df7](https://github.com/prometheus/client_java/commit/7a36df7151e55adafd5bb5a72af81fd7bf8f1133))
* **deps:** update dependency org.springframework.boot:spring-boot-starter-parent to v4.0.5 ([#2006](https://github.com/prometheus/client_java/issues/2006)) ([0106c18](https://github.com/prometheus/client_java/commit/0106c18adffd6d3e829f73979917bdf7cc5f53dd))
* **deps:** update dependency org.springframework.boot:spring-boot-starter-parent to v4.0.6 ([#2046](https://github.com/prometheus/client_java/issues/2046)) ([40a9db8](https://github.com/prometheus/client_java/commit/40a9db868805e36fbaa0f9ac3d02becb17104cd0))
* **deps:** update jetty monorepo to v12.1.8 ([#2007](https://github.com/prometheus/client_java/issues/2007)) ([acab5b2](https://github.com/prometheus/client_java/commit/acab5b213e7661818470716158b3cfe67caae9da))
* **deps:** update protobuf ([#2024](https://github.com/prometheus/client_java/issues/2024)) ([8e2214e](https://github.com/prometheus/client_java/commit/8e2214e0a3ac2fe8a9194d9519dcee10f6c9a694))
* pass release tag as input to deploy workflow ([#1982](https://github.com/prometheus/client_java/issues/1982)) ([165c921](https://github.com/prometheus/client_java/commit/165c921c2508e073baa8f403b30e536ba9b43df9))
* pin grafana/otel-lgtm to 0.7.2 in OATs acceptance test ([#1992](https://github.com/prometheus/client_java/issues/1992)) ([f17ad9a](https://github.com/prometheus/client_java/commit/f17ad9ad9be2ed0a8519db094f9d8fe9a8a83c48))
* stabilize flaky timer and thread count tests ([#1973](https://github.com/prometheus/client_java/issues/1973)) ([ce5867b](https://github.com/prometheus/client_java/commit/ce5867b3e25e10c68a6face275732b994a80ec98))
* trigger Maven deploy from release-please via workflow_dispatch ([#1981](https://github.com/prometheus/client_java/issues/1981)) ([698f956](https://github.com/prometheus/client_java/commit/698f9565e825cdb0f58d2782131cb152cc13894a))

## [1.5.1](https://github.com/prometheus/client_java/compare/v1.5.0...v1.5.1) (2026-03-20)


### Bug Fixes

* **deps:** update dependency io.prometheus:prometheus-metrics-bom to v1.5.0 ([#1877](https://github.com/prometheus/client_java/issues/1877)) ([043fc57](https://github.com/prometheus/client_java/commit/043fc5742752fdc2f67f0219418030a190c53bde))
* **deps:** update dependency org.springframework.boot:spring-boot-starter-parent to v4.0.3 ([#1900](https://github.com/prometheus/client_java/issues/1900)) ([0d800d0](https://github.com/prometheus/client_java/commit/0d800d0a91578e48f34909472c183174fdf1d83e))
* **deps:** update jetty monorepo to v12.1.7 ([#1932](https://github.com/prometheus/client_java/issues/1932)) ([5bd3b79](https://github.com/prometheus/client_java/commit/5bd3b7932f454f3ed2cf55f26d6e1e1908d9ad16))
* **deps:** update junit-framework monorepo to v6.0.3 ([#1880](https://github.com/prometheus/client_java/issues/1880)) ([05ad751](https://github.com/prometheus/client_java/commit/05ad751a40053f11eae90b9e6cbd741814ca71a7))
* exclude standalone examples from `mise run format` ([#1931](https://github.com/prometheus/client_java/issues/1931)) ([537fb88](https://github.com/prometheus/client_java/commit/537fb88aae4048ab36041268f902afbbdce54a96))
* fix release-please PR title pattern and permissions ([#1978](https://github.com/prometheus/client_java/issues/1978)) ([d737978](https://github.com/prometheus/client_java/commit/d7379780f1351a1521c8d93d0544bffce49d02a6))
* Handle empty datapoints in otel exporter ([#1898](https://github.com/prometheus/client_java/issues/1898)) ([59c8552](https://github.com/prometheus/client_java/commit/59c8552f3d67c06d82344383b45e07fea8ed88b9))
* inline set-version logic in build-release.sh ([#1884](https://github.com/prometheus/client_java/issues/1884)) ([c050435](https://github.com/prometheus/client_java/commit/c050435a4153046c72d158991c4c8e064dfb24ec))
* reduce lychee retries to avoid compounding GitHub 429s ([#1940](https://github.com/prometheus/client_java/issues/1940)) ([cc17d6e](https://github.com/prometheus/client_java/commit/cc17d6e4346c9d51e054010fddc75cf8935cbc7d))
* remove version manipulation from build-release.sh ([#1886](https://github.com/prometheus/client_java/issues/1886)) ([93e2b6d](https://github.com/prometheus/client_java/commit/93e2b6da48abc03ba7d96ffff5020ab73c1ee8c1))
* trigger Maven deploy on release-please published events ([#1966](https://github.com/prometheus/client_java/issues/1966)) ([643d0e7](https://github.com/prometheus/client_java/commit/643d0e70c274e2a55024611c73a53545a65e94a0))
* use /tree/ instead of /blob/ for directory URL ([#1944](https://github.com/prometheus/client_java/issues/1944)) ([b81332e](https://github.com/prometheus/client_java/commit/b81332e3a09e465f956f118a2403e64b83771ae5))
* use maven release type for release-please ([#1967](https://github.com/prometheus/client_java/issues/1967)) ([ff3bd2d](https://github.com/prometheus/client_java/commit/ff3bd2d329acdb6761044846559c353a526c0384))


### Documentation

* document DCO sign-off requirement for contributions ([#1937](https://github.com/prometheus/client_java/issues/1937)) ([0860e77](https://github.com/prometheus/client_java/commit/0860e7742b08ab019d129ea24c348191c3f9e0da))

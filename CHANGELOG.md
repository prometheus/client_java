# Changelog

## [1.6.0](https://github.com/prometheus/client_java/compare/v1.5.1...v1.6.0) (2026-03-19)


### Features

* Add readTimeout and conenctionTimeout as configurable parameters ([#1658](https://github.com/prometheus/client_java/issues/1658)) ([e7aa3c6](https://github.com/prometheus/client_java/commit/e7aa3c603e0aa0c69abb174ae8d9144124e80d5b))
* adds createdTimestamp to counters exposed via prometheus protouf protocol ([#1532](https://github.com/prometheus/client_java/issues/1532)) ([b89a721](https://github.com/prometheus/client_java/commit/b89a7218add14a5f0b5ee6f3c7e4cedd6d55894f))


### Bug Fixes

* **deps:** update dependency com.github.ben-manes.caffeine:caffeine to v3.2.3 ([#1649](https://github.com/prometheus/client_java/issues/1649)) ([2b87200](https://github.com/prometheus/client_java/commit/2b87200ecea6a23ccc66276de915abac621e127c))
* **deps:** update dependency com.google.guava:guava to v33.5.0-jre ([#1573](https://github.com/prometheus/client_java/issues/1573)) ([290276a](https://github.com/prometheus/client_java/commit/290276a4b102ff1bbfe0b74eeb622b24787eff2c))
* **deps:** update dependency com.google.protobuf:protobuf-java to v4.33.0 ([#1627](https://github.com/prometheus/client_java/issues/1627)) ([446a097](https://github.com/prometheus/client_java/commit/446a0973ad676a1716a469cf8aec6e1bb8fd733a))
* **deps:** update dependency commons-io:commons-io to v2.21.0 ([#1670](https://github.com/prometheus/client_java/issues/1670)) ([06d48a0](https://github.com/prometheus/client_java/commit/06d48a0598bea7cf6b9a467b8121b030fd2469e8))
* **deps:** update dependency io.dropwizard.metrics:metrics-core to v4.2.37 ([#1569](https://github.com/prometheus/client_java/issues/1569)) ([41b0365](https://github.com/prometheus/client_java/commit/41b0365077d1c60086da2530059cdcc8fc7b77e8))
* **deps:** update dependency io.dropwizard.metrics:metrics-core to v4.2.38 ([#1805](https://github.com/prometheus/client_java/issues/1805)) ([fcbb08e](https://github.com/prometheus/client_java/commit/fcbb08e6d433a4acfc0a418ba70dd7208126eb4a))
* **deps:** update dependency io.dropwizard.metrics5:metrics-core to v5.0.5 ([#1570](https://github.com/prometheus/client_java/issues/1570)) ([2abf8bb](https://github.com/prometheus/client_java/commit/2abf8bb523d6ffbed7238e2b0479e4edf1cc6c02))
* **deps:** update dependency io.dropwizard.metrics5:metrics-core to v5.0.6 ([#1806](https://github.com/prometheus/client_java/issues/1806)) ([0550eef](https://github.com/prometheus/client_java/commit/0550eef81789f7e54466e8d7d22200930791c11e))
* **deps:** update dependency io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom-alpha to v2.24.0-alpha ([#1863](https://github.com/prometheus/client_java/issues/1863)) ([04bc727](https://github.com/prometheus/client_java/commit/04bc727fcb2b9ba4da8eb7268c562f5385f5eda4))
* **deps:** update dependency io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom-alpha to v2.25.0-alpha ([#1871](https://github.com/prometheus/client_java/issues/1871)) ([7e472c7](https://github.com/prometheus/client_java/commit/7e472c7c9c9b4b49339cc95162b67c1b10cb3ba7))
* **deps:** update dependency io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom-alpha to v2.25.0-alpha ([#1872](https://github.com/prometheus/client_java/issues/1872)) ([246c956](https://github.com/prometheus/client_java/commit/246c956d3a54efecf12f5ded2098e489d1e73866))
* **deps:** update dependency io.prometheus:prometheus-metrics-bom to v1.5.0 ([#1877](https://github.com/prometheus/client_java/issues/1877)) ([043fc57](https://github.com/prometheus/client_java/commit/043fc5742752fdc2f67f0219418030a190c53bde))
* **deps:** update dependency org.apache.tomcat.embed:tomcat-embed-core to v11.0.11 ([#1554](https://github.com/prometheus/client_java/issues/1554)) ([3459435](https://github.com/prometheus/client_java/commit/3459435d628e391f402658d1ff8139d05d8e79b4))
* **deps:** update dependency org.apache.tomcat.embed:tomcat-embed-core to v11.0.12 ([#1615](https://github.com/prometheus/client_java/issues/1615)) ([41b1b48](https://github.com/prometheus/client_java/commit/41b1b4850b3f0dbf4c0b80467df5aab55f28b0db))
* **deps:** update dependency org.apache.tomcat.embed:tomcat-embed-core to v11.0.13 ([#1622](https://github.com/prometheus/client_java/issues/1622)) ([7948421](https://github.com/prometheus/client_java/commit/7948421fddeac5d73eab9521641fba26e6e5ad2d))
* **deps:** update dependency org.apache.tomcat.embed:tomcat-embed-core to v11.0.14 ([#1678](https://github.com/prometheus/client_java/issues/1678)) ([b53cf31](https://github.com/prometheus/client_java/commit/b53cf311f65d6c35210c467db181f344afa46d1d))
* **deps:** update dependency org.apache.tomcat.embed:tomcat-embed-core to v11.0.15 ([#1726](https://github.com/prometheus/client_java/issues/1726)) ([947e9c3](https://github.com/prometheus/client_java/commit/947e9c392e65bb604c16da1d943b3e567cb731d5))
* **deps:** update dependency org.apache.tomcat.embed:tomcat-embed-core to v11.0.18 ([#1820](https://github.com/prometheus/client_java/issues/1820)) ([a756947](https://github.com/prometheus/client_java/commit/a756947e29e4c00eb7fa4820daae5625f4f7b696))
* **deps:** update dependency org.eclipse.jetty:jetty-server to v12.1.2 ([#1616](https://github.com/prometheus/client_java/issues/1616)) ([da977df](https://github.com/prometheus/client_java/commit/da977df26775b4253fec2c940c19df0c864f1699))
* **deps:** update dependency org.springframework.boot:spring-boot-starter-parent to v3.5.6 ([#1574](https://github.com/prometheus/client_java/issues/1574)) ([eb91514](https://github.com/prometheus/client_java/commit/eb915140f94860ebe9147240b8299eeaaf60cdb9))
* **deps:** update dependency org.springframework.boot:spring-boot-starter-parent to v3.5.7 ([#1641](https://github.com/prometheus/client_java/issues/1641)) ([14883e4](https://github.com/prometheus/client_java/commit/14883e4534600cdd5ce60351fda555615fa0b78d))
* **deps:** update dependency org.springframework.boot:spring-boot-starter-parent to v3.5.8 ([#1696](https://github.com/prometheus/client_java/issues/1696)) ([490d2f6](https://github.com/prometheus/client_java/commit/490d2f6d96198b14a8444add200ffd6d215e69bd))
* **deps:** update dependency org.springframework.boot:spring-boot-starter-parent to v4 ([#1699](https://github.com/prometheus/client_java/issues/1699)) ([2a71bd3](https://github.com/prometheus/client_java/commit/2a71bd311da6e33ae2004a1198a452bb34a62533))
* **deps:** update dependency org.springframework.boot:spring-boot-starter-parent to v4.0.1 ([#1746](https://github.com/prometheus/client_java/issues/1746)) ([975e7c0](https://github.com/prometheus/client_java/commit/975e7c05ab49d549e5b2f4e5ea3dde2ec301a62a))
* **deps:** update dependency org.springframework.boot:spring-boot-starter-parent to v4.0.2 ([#1797](https://github.com/prometheus/client_java/issues/1797)) ([623467c](https://github.com/prometheus/client_java/commit/623467c4ed93eee6acceb00eb08ec9c726b1a815))
* **deps:** update dependency org.springframework.boot:spring-boot-starter-parent to v4.0.3 ([#1900](https://github.com/prometheus/client_java/issues/1900)) ([0d800d0](https://github.com/prometheus/client_java/commit/0d800d0a91578e48f34909472c183174fdf1d83e))
* **deps:** update jetty monorepo to v12.1.1 ([#1558](https://github.com/prometheus/client_java/issues/1558)) ([ab97f62](https://github.com/prometheus/client_java/commit/ab97f62ece8de42fff7b2b748e38eb25c0d8c2da))
* **deps:** update jetty monorepo to v12.1.3 ([#1634](https://github.com/prometheus/client_java/issues/1634)) ([0b7eb5f](https://github.com/prometheus/client_java/commit/0b7eb5f5d2c7024177b90ecc4907e90f73bac409))
* **deps:** update jetty monorepo to v12.1.4 ([#1680](https://github.com/prometheus/client_java/issues/1680)) ([f0e751d](https://github.com/prometheus/client_java/commit/f0e751dc76ad65b0ada0f22e6fee47ce7fe371bd))
* **deps:** update jetty monorepo to v12.1.5 ([#1722](https://github.com/prometheus/client_java/issues/1722)) ([dc805a7](https://github.com/prometheus/client_java/commit/dc805a749b474278dd6ab756b1f63a9813b1c87e))
* **deps:** update jetty monorepo to v12.1.6 ([#1837](https://github.com/prometheus/client_java/issues/1837)) ([ab808a0](https://github.com/prometheus/client_java/commit/ab808a0b21982b778f4aeb1dc7131d1c20c05766))
* **deps:** update jetty monorepo to v12.1.7 ([#1932](https://github.com/prometheus/client_java/issues/1932)) ([5bd3b79](https://github.com/prometheus/client_java/commit/5bd3b7932f454f3ed2cf55f26d6e1e1908d9ad16))
* **deps:** update junit-framework monorepo to v5.14.0 ([#1601](https://github.com/prometheus/client_java/issues/1601)) ([ab785c1](https://github.com/prometheus/client_java/commit/ab785c18c2e1f65ff66c63f698573b5f693ef5c2))
* **deps:** update junit-framework monorepo to v6 (major) ([#1602](https://github.com/prometheus/client_java/issues/1602)) ([0655cea](https://github.com/prometheus/client_java/commit/0655cea3e5a7c8b74cadac32fa340860c58a7318))
* **deps:** update junit-framework monorepo to v6.0.1 ([#1657](https://github.com/prometheus/client_java/issues/1657)) ([be0b1cf](https://github.com/prometheus/client_java/commit/be0b1cf861becab04d5d96b540cb01a44dcfd1d6))
* **deps:** update junit-framework monorepo to v6.0.2 ([#1764](https://github.com/prometheus/client_java/issues/1764)) ([63ed3b6](https://github.com/prometheus/client_java/commit/63ed3b627aea0a88709e10198b994348ae031655))
* **deps:** update junit-framework monorepo to v6.0.3 ([#1880](https://github.com/prometheus/client_java/issues/1880)) ([05ad751](https://github.com/prometheus/client_java/commit/05ad751a40053f11eae90b9e6cbd741814ca71a7))
* **deps:** update protobuf monorepo ([#1566](https://github.com/prometheus/client_java/issues/1566)) ([6121922](https://github.com/prometheus/client_java/commit/612192291df06c11bd0a32d66508dffed3ab23d6))
* **deps:** update protobuf monorepo ([#1681](https://github.com/prometheus/client_java/issues/1681)) ([bfbaa5f](https://github.com/prometheus/client_java/commit/bfbaa5f9864db19238f03ebef3ebcd9b452aba16))
* **deps:** update protobuf monorepo ([#1724](https://github.com/prometheus/client_java/issues/1724)) ([aed3439](https://github.com/prometheus/client_java/commit/aed3439ece75fcf793bb81c418b69ab8b26bf314))
* **deps:** update protobuf monorepo ([#1774](https://github.com/prometheus/client_java/issues/1774)) ([1effe1f](https://github.com/prometheus/client_java/commit/1effe1f434c123140c3ddecfe40ed919106faa1b))
* **deps:** update protobuf monorepo ([#1778](https://github.com/prometheus/client_java/issues/1778)) ([ea0c71e](https://github.com/prometheus/client_java/commit/ea0c71ebe85b27839fba8c1e2c78e91f5c0585e8))
* **deps:** update protobuf monorepo ([#1835](https://github.com/prometheus/client_java/issues/1835)) ([ba308cf](https://github.com/prometheus/client_java/commit/ba308cf393c89af8b2cd6773570ec7ba72acc42a))
* exclude standalone examples from `mise run format` ([#1931](https://github.com/prometheus/client_java/issues/1931)) ([537fb88](https://github.com/prometheus/client_java/commit/537fb88aae4048ab36041268f902afbbdce54a96))
* Handle empty datapoints in otel exporter ([#1898](https://github.com/prometheus/client_java/issues/1898)) ([59c8552](https://github.com/prometheus/client_java/commit/59c8552f3d67c06d82344383b45e07fea8ed88b9))
* inline set-version logic in build-release.sh ([#1884](https://github.com/prometheus/client_java/issues/1884)) ([c050435](https://github.com/prometheus/client_java/commit/c050435a4153046c72d158991c4c8e064dfb24ec))
* reduce lychee retries to avoid compounding GitHub 429s ([#1940](https://github.com/prometheus/client_java/issues/1940)) ([cc17d6e](https://github.com/prometheus/client_java/commit/cc17d6e4346c9d51e054010fddc75cf8935cbc7d))
* remove version manipulation from build-release.sh ([#1886](https://github.com/prometheus/client_java/issues/1886)) ([93e2b6d](https://github.com/prometheus/client_java/commit/93e2b6da48abc03ba7d96ffff5020ab73c1ee8c1))
* **renovate:** enable updates for opentelemetry-instrumentation-bom-alpha ([#1860](https://github.com/prometheus/client_java/issues/1860)) ([8adc88f](https://github.com/prometheus/client_java/commit/8adc88fb6faca1cf1043c63096e6464fefb1aa98))
* trigger Maven deploy on release-please published events ([#1966](https://github.com/prometheus/client_java/issues/1966)) ([643d0e7](https://github.com/prometheus/client_java/commit/643d0e70c274e2a55024611c73a53545a65e94a0))
* update build command to include 'clean' for benchmarks ([#1762](https://github.com/prometheus/client_java/issues/1762)) ([3a2b380](https://github.com/prometheus/client_java/commit/3a2b3801345d292058d9d7aba000a843f4f61be1))
* use /tree/ instead of /blob/ for directory URL ([#1944](https://github.com/prometheus/client_java/issues/1944)) ([b81332e](https://github.com/prometheus/client_java/commit/b81332e3a09e465f956f118a2403e64b83771ae5))
* use maven release type for release-please ([#1967](https://github.com/prometheus/client_java/issues/1967)) ([ff3bd2d](https://github.com/prometheus/client_java/commit/ff3bd2d329acdb6761044846559c353a526c0384))


### Documentation

* document DCO sign-off requirement for contributions ([#1937](https://github.com/prometheus/client_java/issues/1937)) ([0860e77](https://github.com/prometheus/client_java/commit/0860e7742b08ab019d129ea24c348191c3f9e0da))
* Update classicBuckets() to classicUpperBounds() ([#1644](https://github.com/prometheus/client_java/issues/1644)) ([c15ce23](https://github.com/prometheus/client_java/commit/c15ce23928794d2a7437a2878909fa63578423ca))

## [1.5.1](https://github.com/prometheus/client_java/compare/v1.5.0...v1.5.1) (2026-03-19)


### Bug Fixes

* **deps:** update dependency io.prometheus:prometheus-metrics-bom to v1.5.0 ([#1877](https://github.com/prometheus/client_java/issues/1877)) ([043fc57](https://github.com/prometheus/client_java/commit/043fc5742752fdc2f67f0219418030a190c53bde))
* **deps:** update dependency org.springframework.boot:spring-boot-starter-parent to v4.0.3 ([#1900](https://github.com/prometheus/client_java/issues/1900)) ([0d800d0](https://github.com/prometheus/client_java/commit/0d800d0a91578e48f34909472c183174fdf1d83e))
* **deps:** update jetty monorepo to v12.1.7 ([#1932](https://github.com/prometheus/client_java/issues/1932)) ([5bd3b79](https://github.com/prometheus/client_java/commit/5bd3b7932f454f3ed2cf55f26d6e1e1908d9ad16))
* **deps:** update junit-framework monorepo to v6.0.3 ([#1880](https://github.com/prometheus/client_java/issues/1880)) ([05ad751](https://github.com/prometheus/client_java/commit/05ad751a40053f11eae90b9e6cbd741814ca71a7))
* exclude standalone examples from `mise run format` ([#1931](https://github.com/prometheus/client_java/issues/1931)) ([537fb88](https://github.com/prometheus/client_java/commit/537fb88aae4048ab36041268f902afbbdce54a96))
* Handle empty datapoints in otel exporter ([#1898](https://github.com/prometheus/client_java/issues/1898)) ([59c8552](https://github.com/prometheus/client_java/commit/59c8552f3d67c06d82344383b45e07fea8ed88b9))
* inline set-version logic in build-release.sh ([#1884](https://github.com/prometheus/client_java/issues/1884)) ([c050435](https://github.com/prometheus/client_java/commit/c050435a4153046c72d158991c4c8e064dfb24ec))
* reduce lychee retries to avoid compounding GitHub 429s ([#1940](https://github.com/prometheus/client_java/issues/1940)) ([cc17d6e](https://github.com/prometheus/client_java/commit/cc17d6e4346c9d51e054010fddc75cf8935cbc7d))
* remove version manipulation from build-release.sh ([#1886](https://github.com/prometheus/client_java/issues/1886)) ([93e2b6d](https://github.com/prometheus/client_java/commit/93e2b6da48abc03ba7d96ffff5020ab73c1ee8c1))
* trigger Maven deploy on release-please published events ([#1966](https://github.com/prometheus/client_java/issues/1966)) ([643d0e7](https://github.com/prometheus/client_java/commit/643d0e70c274e2a55024611c73a53545a65e94a0))
* use /tree/ instead of /blob/ for directory URL ([#1944](https://github.com/prometheus/client_java/issues/1944)) ([b81332e](https://github.com/prometheus/client_java/commit/b81332e3a09e465f956f118a2403e64b83771ae5))
* use maven release type for release-please ([#1967](https://github.com/prometheus/client_java/issues/1967)) ([ff3bd2d](https://github.com/prometheus/client_java/commit/ff3bd2d329acdb6761044846559c353a526c0384))


### Documentation

* document DCO sign-off requirement for contributions ([#1937](https://github.com/prometheus/client_java/issues/1937)) ([0860e77](https://github.com/prometheus/client_java/commit/0860e7742b08ab019d129ea24c348191c3f9e0da))

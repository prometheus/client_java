# Changelog

## [1.6.0](https://github.com/prometheus/client_java/compare/v1.5.1...v1.6.0) (2026-04-25)


### Features

* Add logic for handling content negotation flag with OM2 ([#1986](https://github.com/prometheus/client_java/issues/1986)) ([0fa1ad7](https://github.com/prometheus/client_java/commit/0fa1ad7dcb71f7f02e19ee9604c07d9c48802f04))
* Add OM2 native histogram text output ([#2042](https://github.com/prometheus/client_java/issues/2042)) ([dec8e5b](https://github.com/prometheus/client_java/commit/dec8e5b15a1c48c54be6b81517f2cb334bc0ee60))
* add OTel preserve_names for scrape-time suffix handling ([#1956](https://github.com/prometheus/client_java/issues/1956)) ([f794288](https://github.com/prometheus/client_java/commit/f79428863e4f2012ab20b5b0aabc2ca1e6a8151c))
* compositeValues and exemplarCompliance flags for OM2 writer ([#1991](https://github.com/prometheus/client_java/issues/1991)) ([ff48ae8](https://github.com/prometheus/client_java/commit/ff48ae8118c4e071bf86ca7beabdd0e951b896dc))
* move suffix handling to scrape time ([#1955](https://github.com/prometheus/client_java/issues/1955)) ([5a5106c](https://github.com/prometheus/client_java/commit/5a5106c2ad46ad5e0c1c97d99b994e7626af18ee))
* OM2 writer outputs names as provided, no suffix appending ([#1957](https://github.com/prometheus/client_java/issues/1957)) ([5ce2b57](https://github.com/prometheus/client_java/commit/5ce2b575272a06b5115f40f3298d5c861cef8bbd))


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
* Relax metric name validation in Dropwizard5 ([#1985](https://github.com/prometheus/client_java/issues/1985)) ([deb782f](https://github.com/prometheus/client_java/commit/deb782f9fce60ffb1308a98b661c0a1ccb79a82b))
* stabilize flaky timer and thread count tests ([#1973](https://github.com/prometheus/client_java/issues/1973)) ([ce5867b](https://github.com/prometheus/client_java/commit/ce5867b3e25e10c68a6face275732b994a80ec98))
* Tighten OM2 summary and start timestamp output ([#2041](https://github.com/prometheus/client_java/issues/2041)) ([5699469](https://github.com/prometheus/client_java/commit/5699469d345b9d3aaf3d6c0e5e76de2959477269))
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

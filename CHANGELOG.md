## [1.24.0](https://github.com/trustyuri/trustyuri-java/compare/trustyuri-1.23.0...trustyuri-1.24.0) (2026-04-08)

### Features

* add ArtifactCode interface and its implementation ([a5d2931](https://github.com/trustyuri/trustyuri-java/commit/a5d29314f94f265056e4dba77f7aa11cb2de9080))
* **ArtifactCode:** enhance implementation with module and data hash handling ([35221b6](https://github.com/trustyuri/trustyuri-java/commit/35221b6818cea269a0b906f9328abffd846f1820))
* **ArtifactCode:** integrate ArtifactCode handling across RDF processing classes ([d09238e](https://github.com/trustyuri/trustyuri-java/commit/d09238ea0a0aa910f34d3902eab55ab8921425bf))
* **ArtifactCode:** replace getCode() with toString() for artifact code representation ([a989ade](https://github.com/trustyuri/trustyuri-java/commit/a989adebf6e80a849b8542f5cf33fc671335e72e))

### Dependency updates

* **core-deps:** remove unused commons-logging:commons-logging dependency ([2104f7b](https://github.com/trustyuri/trustyuri-java/commit/2104f7bd46b99de3ac62743e36f796fc8801c0e1))
* **core-deps:** set commons-io:commons-io dependency scope to test ([dab03ce](https://github.com/trustyuri/trustyuri-java/commit/dab03ce4dba26f17d8ae2ac026678543fe78f1ad))
* **core-deps:** update commons-io:commons-io to v2.21.0 ([6c28b67](https://github.com/trustyuri/trustyuri-java/commit/6c28b67a065627fd05901fb38f8cfc7e8113ba39))
* **core-deps:** update org.apache.commons:commons-lang3 dependency to v3.20.0 ([4acd8fa](https://github.com/trustyuri/trustyuri-java/commit/4acd8fad2d11c38adb4d8c07b55bd669d117183f))
* **core-deps:** update org.slf4j:* dependencies to v2.0.17 ([653f9e7](https://github.com/trustyuri/trustyuri-java/commit/653f9e7f09d69e60644abe01f2bcf8382aa977c1))
* **deps:** update org.apache.maven.plugins:maven-gpg-plugin to v3.2.8 ([8c2ce85](https://github.com/trustyuri/trustyuri-java/commit/8c2ce85ea377120c0a509920a3a7a90ef80039bd))
* **deps:** update org.apache.maven.plugins:maven-jar-plugin to v3.5.0 ([26394ef](https://github.com/trustyuri/trustyuri-java/commit/26394efb107a92b5a1381226fec727ba586eb07c))
* **deps:** update org.apache.maven.plugins:maven-javadoc-plugin to v3.12.0 ([f89b585](https://github.com/trustyuri/trustyuri-java/commit/f89b5851d9eb1cd5078f4261e3ec3b49ef71790d))

### Bug Fixes

* **RunBatch:** remove deprecated code with new methods calls and add logging ([dd1b689](https://github.com/trustyuri/trustyuri-java/commit/dd1b6899f87f686fb484045f8f558e67d9c5e927))

### Tests

* **TrustyUriUtils:** add unit tests for artifact code extraction and validation ([7ae4eda](https://github.com/trustyuri/trustyuri-java/commit/7ae4edae5a44a88a1de276fac357179aea54fe35))

### Build and continuous integration

* add autorelease workflow ([57acdad](https://github.com/trustyuri/trustyuri-java/commit/57acdadecb73be002bfc423f69f61e845d8d43b5))
* add GitHub Actions workflow for Maven testing ([4be260b](https://github.com/trustyuri/trustyuri-java/commit/4be260b5b7b4b676154bd656d6feb7e8858cae2d))
* **deps:** update actions/checkout action to v6.0.2 ([723a415](https://github.com/trustyuri/trustyuri-java/commit/723a415aa82c3a3d24df3b64dd86c90868461318))
* **deps:** update actions/setup-java action to v5.2.0 ([19b0305](https://github.com/trustyuri/trustyuri-java/commit/19b03054755627f55987ddf041e0c8c08cb5ee19))

### General maintenance

* add maven wrapper ([96a299f](https://github.com/trustyuri/trustyuri-java/commit/96a299f866f932122b8ea51247bae373d9b78ce7))
* add semantic release configuration and dependencies ([408dbf6](https://github.com/trustyuri/trustyuri-java/commit/408dbf64e9f590de81c557a2057dae050d02637f))
* **logging:** integrate SLF4J logging into CheckFile, CheckLargeRdf, CheckRdfGraph, and CheckSortedRdf classes ([aaac0c3](https://github.com/trustyuri/trustyuri-java/commit/aaac0c3b48559483c4abedeb7e0b3914dc7a9c9b))
* **readme:** update for improved clarity and consistency ([3b788d6](https://github.com/trustyuri/trustyuri-java/commit/3b788d674c34c394167b6f33bf328b3562271e41))
* **sem-release:** update configuration to customize tag format ([ad67751](https://github.com/trustyuri/trustyuri-java/commit/ad67751294c438257d1251df4a9e36a7e33b6b6f))
* update Maven configuration and add settings for central repository ([61a82dd](https://github.com/trustyuri/trustyuri-java/commit/61a82dd83d25954ebde98d17c6c308f074fd7797))
* update Maven to v3.9.14 ([a09cdeb](https://github.com/trustyuri/trustyuri-java/commit/a09cdeb2a97115dc4fe39e0de721a8ecb6c04895))
* update version to 1.24.0-SNAPSHOT and fix SCM tag format ([30ec65e](https://github.com/trustyuri/trustyuri-java/commit/30ec65e32025c2703c4a710d53a5a76be0907bcd))

### Refactoring

* **ArtifactCode:** update FileHasher and AbstractTrustyUriModule for improved artifact code handling ([8cdb615](https://github.com/trustyuri/trustyuri-java/commit/8cdb615cf2aee77165f8c8121f7c9296ce8a8aa7))
* improve code documentation and formatting in CheckFile and TransformRdf classes ([717efa7](https://github.com/trustyuri/trustyuri-java/commit/717efa7e885d51e9b0e02d80d8e09d91af956b34))
* replace deprecated vocabulary term `XMLSchema.STRING` with new one ([090484f](https://github.com/trustyuri/trustyuri-java/commit/090484f341a4ab0041bb142128846e4e75b2eb2e))
* **TrustyUriUtils:** update artifact code extraction using regex pattern matching ([c977576](https://github.com/trustyuri/trustyuri-java/commit/c977576e2b3d18226b7d8104916c31aa01a3dbe0))

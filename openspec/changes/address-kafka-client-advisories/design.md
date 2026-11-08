# Design

## Context

See `proposal.md` — Why for the failing scan.

`org.apache.kafka:kafka-clients` reaches the build transitively: `axon-kafka 4.12.1` pulls `cloudevents-kafka 4.0.1`,
which declares `kafka-clients`, and Spring Boot 3.5.16's BOM owns the resolved version (`3.9.2`). No module or catalog
entry declares the coordinate today, so the platform is the only site that can move it.

The pinned neighbours constrain the choice:

- Spring Kafka 3.3.16 (Boot 3.5's managed line) compiles against `kafka-clients` 3.8.1, so `4.4.0` is ahead of the
  combination upstream tests.
- The Spring Boot 4 migration — which would bring Spring Kafka 4 and its `kafka-clients` 4.x pairing — is deferred
  (ADR-0004).
- The deployed broker is `apache/kafka:3.9.0` (`kafka-image-tag`), and the integration suites boot that same image, so
  the probe exercises a 4.x client against a 3.9 broker.

## Goals / Non-Goals

**Goals:**

- Clear both advisories on the classpaths `snyk test` reads.
- Keep the coordinate catalog-owned so `dependencyUpdates` reports it.
- Record the unsupported-combination decision, its verification, and the condition that supersedes it.

**Non-Goals:**

- Patching the broker image: the newest Bitnami `kafka` chart (`32.4.3`) packages Kafka `4.0.0`, which is still inside
  the advisories' affected ranges, so no chart bump clears them; the deployment's exposure is also not assessed here.
- Migrating to Spring Boot 4 or Spring Kafka 4.
- Suppressing either advisory in `.snyk`.

## Decisions

### Force `kafka-clients` 4.4.0 through a platform constraint

`platform/build.gradle.kts` gains `api(libs.kafka.clients)` in its `constraints` block, and the catalog gains
`kafka-clients = "4.4.0"` with its library entry. The platform is imported by the builds that resolve the coordinate, so
one constraint covers the services, `showcase-test`, and the test classpaths the scan reads.

Alternatives considered:

- **Suppress both advisories in `.snyk` with an expiry.** Rejected: a fixed release exists and passes the suites run in
  the probe, and the suppression policy admits only advisories with no available fix.
- **Bump Spring Boot to 4.x.** Rejected: deferred by ADR-0004, and out of proportion to two advisories.
- **Declare the coordinate per module.** Rejected: the coordinate reaches the projects the scan flagged plus the shared
  test fixture, and a per-module declaration would leave the next consumer unprotected; the platform is the single seat
  for a constrained transitive.
- **Bump only the broker image.** Rejected as the remedy for the scan (it reads the client), and unavailable today (see
  Non-Goals).

### Target 4.4.0 rather than 4.2.2

`4.2.2` clears both advisories, but the `4.3` line re-opened the second one until `4.3.2`, so a later same-line bump
would re-surface it. `4.4.0` is the newest stable release, contains both fixes, and keeps the catalog entry meaningful.

### Record the decision as ADR-0031

The override deliberately leaves the Spring Kafka 3.3 / Boot 3.5 supported combination, which is a cross-cutting
supply-chain decision with an expiry condition, not a local build tweak. The ADR states the verification and owns the
supersession condition in its `Revisit when:` gate.

Alternative considered: leave the rationale in this design only. Rejected: a change's design is archived with the change
and is not read by a later dependency decision.

### Verify at the integration tier against the real broker image

Unit and component suites cannot see a binary incompatibility between Axon Kafka 4.12.1 / Spring Kafka 3.3.16 and
`kafka-clients` 4.4.0 — the classes link at compile time and fail at runtime. The probe therefore ran the three
services' integration suites, which boot `apache/kafka:3.9.0` through Testcontainers, and a control run on the unchanged
tree.

The integration tier alone leaves one path unexercised: the command-service's suites set
`axon.kafka.publisher.enabled=false`, so they never publish through the Axon Kafka publisher. The probe therefore also
ran the gateway e2e suite, which boots the four services and drives the full command → Kafka → projection → query
pipeline, and it passes with the same client (29 tests, 0 failures).

### Amended clause in the delta

The delta appends a clause to the requirement's description and one scenario; it does not replace an existing
obligation. One existing clause is amended: the "Dependency scan reports no vulnerable paths" scenario's coordinate list
gains `kafka-clients`.

- **Before**: the scenario's clause read "no sub-project reports one for Jackson 2, `zstd-jni`, `snappy-java`,
  `brotli4j`, `objenesis`, `guava`, `org.ow2.asm:asm`, or `io.netty`".
- **After**: the same list with `kafka-clients` after `snappy-java`.
- **Why**: the scan must name every constrained coordinate, and the newly added one is absent from the clause that
  enumerates them.

## Risks / Trade-offs

- **An unsupported Spring Kafka 3.3.16 / `kafka-clients` 4.4.0 pairing** → the three services' integration suites and
  the gateway e2e suite pass against the deployed broker version, and ADR-0031 records the pairing and its supersession
  condition. A future `kafka-clients` bump under this constraint is re-verified the same way.
- **A client line ahead of the broker's** → the integration suites and the e2e pipeline exercise a producer, a consumer,
  the admin client, and the Axon Kafka event store as a 4.x client against `apache/kafka:3.9.0`, and pass.
- **The broker image stays inside the advisories' affected ranges** → parked in `docs/ideas.md` with the condition that
  reopens it (a maintained chart or image tagging Kafka `4.2.2` or newer); this change does not assess the deployment's
  exposure.

## Claims table

| Claim                                                                             | Source / command                                                                                                                      | Result                                                                                              |
| --------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| The 2026-11-07 schedule run's `snyk` job failed on two `kafka-clients` advisories | `gh run view 42413633653 --log`                                                                                                       | two `SNYK-JAVA-ORGAPACHEKAFKA-…` findings at `org.apache.kafka:kafka-clients@3.9.2`                 |
| The run found vulnerable paths in 12 of 20 sub-projects                           | the same run log                                                                                                                      | `Tested 20 projects, 12 contained vulnerable paths.`                                                |
| Spring Boot 3.5.16's BOM owns `kafka-clients` 3.9.2                               | `curl -s https://repo1.maven.org/maven2/org/springframework/boot/spring-boot-dependencies/3.5.16/spring-boot-dependencies-3.5.16.pom` | `<kafka.version>3.9.2</kafka.version>`                                                              |
| Spring Kafka 3.3.16 compiles against `kafka-clients` 3.8.1                        | `curl -s https://repo1.maven.org/maven2/org/springframework/kafka/spring-kafka/3.3.16/spring-kafka-3.3.16.pom`                        | its `kafka-clients` dependency is `3.8.1`                                                           |
| Both advisories are fixed from 4.2.2 (4.3 line: 4.3.1 / 4.3.2)                    | the two Snyk advisory pages                                                                                                           | "Upgrade … to version 4.2.2, 4.3.1 or higher" / "4.2.2, 4.3.2 or higher"                            |
| `kafka-clients` arrives through `axon-kafka` and `cloudevents-kafka`              | scratch clone, `./gradlew :showcase-command-service:dependencyInsight --dependency kafka-clients --configuration runtimeClasspath`    | the chain is `axon-kafka:4.12.1` bringing `cloudevents-kafka:4.0.1`, which declares `kafka-clients` |
| The 3.9 line ends at 3.9.2 and the newest release is 4.4.0                        | `curl -s https://repo1.maven.org/maven2/org/apache/kafka/kafka-clients/maven-metadata.xml`                                            | the last `3.9.x` entries are `3.9.0`–`3.9.2`, `4.0.0` follows, and the list ends at `4.4.0`         |
| No build script or catalog entry declares `kafka-clients` before the change       | `git grep -n "kafka-clients" 0deca079 -- '*.kts' '*.toml' '*.properties'`                                                             | no match                                                                                            |
| The platform is imported by the builds that resolve the coordinate                | `git grep -ln 'project(":platform")' 0deca079 -- '*.kts'`                                                                             | matches across `build-logic`'s conventions and the module build scripts                             |
| Spring Boot 4.0.7 manages `kafka-clients` 4.1.2 with Spring Kafka 4.0.6           | `curl -s https://repo1.maven.org/maven2/org/springframework/boot/spring-boot-dependencies/4.0.7/spring-boot-dependencies-4.0.7.pom`   | `<kafka.version>4.1.2</kafka.version>`, `<spring-kafka.version>4.0.6</spring-kafka.version>`        |
| The constraint compiles the build                                                 | scratch clone of the base revision, `./gradlew compileJava`                                                                           | `BUILD SUCCESSFUL`                                                                                  |
| Unit and component suites pass with the constraint                                | scratch clone, `./gradlew :showcase-command-service:test …:componentTest` (all three services)                                        | `BUILD SUCCESSFUL`                                                                                  |
| Integration suites pass against `apache/kafka:3.9.0`                              | scratch clone, `./gradlew :showcase-command-service:integrationTest …` then the result XML counts                                     | 36 command-service, 20 projection-service, 11 gateway tests; 0 failures, 0 errors                   |
| The unchanged tree passes the same integration suites (control)                   | scratch clone, `git stash` then the same command                                                                                      | exit 0                                                                                              |
| The gateway e2e suite passes over the full pipeline with the new client           | scratch clone, `./gradlew :showcase-api-gateway:e2eTest` then the result XML counts                                                   | 29 tests (6 classes), 0 failures, 0 errors                                                          |
| The `e2eTest` runtime classpath resolves the constrained client                   | scratch clone, `./gradlew :showcase-api-gateway:dependencyInsight --dependency kafka-clients --configuration e2eTestRuntimeClasspath` | `org.apache.kafka:kafka-clients:4.4.0`                                                              |
| The command-service integration suites disable the Kafka publisher                | `git grep -n "axon.kafka.publisher.enabled=false" 0deca079 -- '*/src/integrationTest/**'`                                             | matches four command-service integration suites                                                     |
| The resolved runtime version is 4.4.0                                             | scratch clone, `./gradlew :showcase-command-service:dependencyInsight --dependency kafka-clients --configuration runtimeClasspath`    | `org.apache.kafka:kafka-clients:4.4.0`, selected "By constraint"                                    |
| The newest Bitnami `kafka` chart packages Kafka 4.0.0                             | `helm repo update bitnami && helm search repo bitnami/kafka --versions`                                                               | `32.4.3  4.0.0`                                                                                     |
| The deployed broker is below the fixed line                                       | `git show 0deca079:gradle/libs.versions.toml` at the base revision                                                                    | `kafka-image-tag = "3.9.0"`                                                                         |

### Dismissed scan candidates

The pre-write scan's remaining candidates are dismissed by class, each verified against the lines it covers:

- **`[count]`** — version ordinals and task numbers (`4.12`, `3.5`, `2.3`) are labels, not quantities.
- **`[absolute]`** — design goals, the alternatives' rationale, and task instructions; the facts they rest on are the
  rows above, and the goal's outcome is checked by the scan task.
- **`[path]`** — commands, file paths, and coordinates the artifacts instruct a reader to use or change, not claims
  about the repository's current state.
- **`[cross-ref]`** — a version span (`3.8/3.9`) and the `./gradlew` launcher, not document cross-references.

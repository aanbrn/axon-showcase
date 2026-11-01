# Design

## Context

See `proposal.md` — Why. The findings came from the scheduled credentialed run, not a local one
(`dependencySecurityCheck` needs the Snyk CLI and a token). The run's full log names every finding with its path, and
each fix statement below is Snyk's own ("Upgrade … to version … to fix", or "This issue was fixed in versions",
cross-checked against Central's metadata where the page states a threshold):

- **`org.ow2.asm:asm` 9.8 and 9.10.1** — five medium advisories (inefficient algorithmic complexity, excessive-size
  memory allocation ×2, uncontrolled recursion), each stated fixed at `9.11` or higher. Both versions arrive through
  JaCoCo: the modules' `code-coverage-conventions` pins `jacoco` to the catalog's `0.8.13` (ASM `9.8`), while the
  **root** project applies the `jacoco` plugin for `jacocoRootReport` with no `toolVersion`, so its `jacocoAnt` resolved
  Gradle 9.8.2's built-in `0.8.15` (ASM `9.10.1`) — the run reports both, in different projects.
- **`org.ow2.asm:asm` 9.11** — one medium infinite-loop advisory, stated fixed at `9.11.1`. It arrives through
  `com.github.spotbugs:spotbugs:4.10.5` on the `spotbugs` tool configuration. Central's metadata shows `4.10.5` is the
  newest SpotBugs release, so the fix is a constraint on the tool classpath rather than a bump.
- **`org.ow2.asm:asm` 9.7.1** — the same five advisories on the test classpaths: `spring-boot-starter-test` →
  `net.minidev:json-smart` → `net.minidev:accessors-smart` → `org.ow2.asm:asm@9.7.1`. The scheduled run folded this path
  into the JaCoCo finding ("and 3 other path(s)"), so only the local credentialed scan surfaced it once JaCoCo's ASM was
  patched.
- **`com.aayushatharva.brotli4j` 1.23.0** — six critical out-of-bounds writes (HTTP2Shell), one per native artifact plus
  the core jar, stated fixed at `1.24.0`. They arrive through `io.gatling.highcharts:gatling-charts-highcharts:3.16.0` →
  `gatling-http-client`, and Gatling `3.16.0` is the newest release, so the fix is a constraint on the `gatling`
  classpath (which already imports the platform).
- **`net.minidev:json-smart` 2.5.2** — one medium improper-neutralization, and Snyk's page says "There is no fixed
  version for `net.minidev:json-smart`" (it affects versions `[0,]`, so Central's `2.6.0` does not clear it either). It
  is reached through `spring-boot-starter-test`, which this build declares on test scopes alone.
- **`org.objenesis:objenesis` 3.3** — one low advisory, stated fixed at `3.6`. It arrives through `mockito-core`
  `5.24.0`, whose BOM (already imported by the platform) does not manage it.

The owning configuration for each was derived from the graph with `dependencyInsight`, not assumed: `jacocoAnt` (root
and modules), `spotbugs` (tool), `gatlingRuntimeClasspath` (load-tests), `testRuntimeClasspath` for objenesis, and the
suites' `componentTestRuntimeClasspath` / `integrationTestRuntimeClasspath` for json-smart, which the service's
`testRuntimeClasspath` does not carry.

## Goals / Non-Goals

**Goals:**

- Turn every finding into a constraint at a patched version, except the one with no fixed release, which gets a scoped,
  time-boxed suppression.
- Keep each coordinate single-sourced at the surface that owns it: `jacoco` in the catalog, `objenesis` and `asm` in the
  platform's constraint list, `brotli4j` through the platform's BOM import, and `asm` again on the `spotbugs` tool
  configuration, which no platform constraint reaches.

**Non-Goals:**

- A Gatling or SpotBugs upgrade: Central's metadata shows `3.16.0` and `4.10.5` are the newest releases, so neither
  carries the fix.
- A Spring Boot or Mockito upgrade: the first would ride the deferred Boot 4 migration (ADR-0004), and the second is not
  needed because the platform can constrain `objenesis` directly.
- Excluding `json-smart` from `spring-boot-starter-test`: the test suites' JSON path loads it as its parser, so an
  exclusion fails them at runtime, and the advisory affects every published version, so suppression is the only route
  that keeps the suites running.

## Decisions

**Bump `jacoco` to `0.8.16` (its `asm.version` is `9.11.1`) and pin the root project's `toolVersion` from the catalog.**
JaCoCo publishes the fix itself, so a constraint would only shadow a version the tool already ships. The root's
`toolVersion` omission is the drift that made one project report ASM `9.10.1` while every other reported `9.8`;
single-sourcing it from the catalog removes that class of drift rather than pinning a second default. _Alternative
rejected:_ constraining `org.ow2.asm:asm` on `jacocoAnt`, which would leave the root's JaCoCo version implicit.

**Constrain `org.ow2.asm:asm` on the `spotbugs` tool configuration.** The platform's constraints do not reach tool
configurations (the `checkstyle`/`guava` precedent in the same convention plugin), and SpotBugs `4.10.5` pins `9.11`
exactly. The constraint mirrors `constraints { checkstyle(libs.guava) }`. _Alternative rejected:_ importing the platform
into the `spotbugs` configuration, which is the shape `checkstyle` rejects.

**Constrain `org.ow2.asm:asm` in the platform too, for every classpath the platform reaches.** Patching JaCoCo's ASM
cleared the tool paths but left `accessors-smart`'s ASM `9.7.1` on the suites, which the local credentialed scan
reported after that first fix; the platform constraint covers every classpath importing it, while the `spotbugs` tool
configuration keeps its own constraint because tool configurations do not reach the platform. _Alternative rejected:_
extending the json-smart suppression to ASM, since a fix exists and the spec suppresses only advisories without one.

**Constrain brotli4j through its BOM and objenesis directly in the platform.** Both have a released fix, the platform is
the repository's single BOM, and `load-tests` already imports the platform into its `gatling` configuration. `objenesis`
is one coordinate, so a constraint suffices; `brotli4j` is a family whose core, `service`, and five native artifacts
must agree, so the platform imports `brotli4j-bom` at `1.24.0` instead. Constraining only the core jar was measured to
leave every native artifact at `1.23.0`, which would have kept five of the six critical findings. A platform import adds
constraints only, so it cannot pull brotli4j into a module that does not have it.

**Suppress `json-smart` rather than force a version.** The policy in `.snyk` exists for exactly this case — an advisory
with no fixed release — so the entry is pinned to `net.minidev:json-smart@2.5.2` and expires, re-surfacing the finding
for re-assessment. Moving to `2.6.0` would not clear the advisory and would change a test-framework runtime dependency
for no security gain.

## Risks / Trade-offs

- **ASM `9.11.1` against SpotBugs' `9.11` expectations** → patch-level within one minor line; the quality gates
  (`spotbugsMain`) are what confirm the tool still runs, not the compile.
- **Forcing brotli4j `1.24.0` under Gatling `3.16.0`** → the native artifacts must move with the core jar, which is why
  the fix is the family's BOM rather than one constraint; the resolution probe checks all six, and the `load-tests`
  compile checks the API.
- **A test-classpath finding reaches the scan but not a product** → it still fails the scheduled run, so it is fixed or
  suppressed like the rest rather than argued down.

## Migration Plan

None: constraints and one suppression, taking effect on the next build and the next credentialed scan.

## Claims table

| Claim                                                                                   | Source                                                                                                                                                                                                          |
| --------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 16 of 20 projects report vulnerable paths, none on 2026-10-25                           | run `41239187028`'s log ("Tested 20 projects, 16 contained vulnerable paths") against run `40196207458`'s ("Tested 20 projects, no vulnerable paths were found")                                                |
| The four coordinates account for every finding the run lists                            | the run log's per-project finding sections ("Issues to fix by upgrading" and "Issues with no direct upgrade or patch"), read whole, against this table's coordinate rows                                        |
| The ASM advisories' fix threshold is 9.11, and `9.11.1` for the 9.11 one                | the run's "This issue was fixed in versions" lines, and `https://security.snyk.io/vuln/SNYK-JAVA-ORGOW2ASM-21715439` ("Upgrade `org.ow2.asm:asm` to version 9.11 or higher")                                    |
| JaCoCo 0.8.16 ships ASM 9.11.1; 0.8.15 ships 9.10.1                                     | Central's `org.jacoco.build` POM `<asm.version>` at each release                                                                                                                                                |
| JaCoCo reaches the graph at 0.8.13 (modules) and 0.8.15 (root)                          | the run's per-project rows plus `./gradlew :dependencyInsight --configuration jacocoAnt --dependency org.ow2.asm:asm` (root: 9.10.1 via `org.jacoco.core:0.8.15`)                                               |
| SpotBugs 4.10.5 is the newest release and pins ASM 9.11                                 | Central's `maven-metadata.xml` for `com.github.spotbugs:spotbugs` and its 4.10.5 POM (`org.ow2.asm:asm:9.11`)                                                                                                   |
| brotli4j 1.24.0 is the fix, and Gatling 3.16.0 is the newest                            | the run's "This issue was fixed in versions: 1.24.0" lines and Central's metadata for both                                                                                                                      |
| json-smart has no fixed release at all                                                  | `https://security.snyk.io/vuln/SNYK-JAVA-NETMINIDEV-21715438` ("There is no fixed version for `net.minidev:json-smart`", affecting versions `[0,]`)                                                             |
| json-smart is reached through `spring-boot-starter-test`, every declaration test-scoped | the run's finding path (`spring-boot-starter-test@3.5.16 > net.minidev:json-smart@2.5.2`) and the build scripts' `libs.spring.boot.starter.test` declarations, each inside a test suite or `testFixtures` block |
| objenesis 3.6 is the fix, reached through `mockito-core:5.24.0`                         | the run's finding line and `:showcase-command-service:dependencyInsight --configuration testRuntimeClasspath --dependency org.objenesis:objenesis`                                                              |
| The four coordinates' owning configurations                                             | the `dependencyInsight` probes above and on `spotbugs`, `gatlingRuntimeClasspath`, `componentTestRuntimeClasspath`, `integrationTestRuntimeClasspath`                                                           |
| asm `9.7.1` reaches the test classpaths through json-smart's `accessors-smart`          | the local scan's finding path, and `:showcase-api-gateway:dependencyInsight --configuration componentTestRuntimeClasspath --dependency org.ow2.asm:asm` after the platform constraint (9.11.1)                  |
| The local credentialed scan clears every finding                                        | `./gradlew dependencySecurityCheck` on 2026-11-01: "Tested 20 projects, no vulnerable paths were found" (`BUILD SUCCESSFUL`)                                                                                    |
| The platform does not reach tool configurations                                         | `code-check-conventions.gradle.kts`'s existing `constraints { checkstyle(libs.guava) }` and the `gradle-build` pack's rule for it                                                                               |

### Dismissals

`scripts/claim-scan.py`'s candidates are dismissed by kind, one reason each:

- `absolute` — the quantifier words sit inside rows whose sources the table states, so each restates a sourced row
  rather than asserting an independent evidence claim.
- `count` — the scanned numerals are figures the table's own rows source, or version strings and dates the scanner reads
  as counts.
- `path` — a scanned path or command names either the repository surface the change edits or the source a row cites.
- `cross-ref` — the scanned version pair is the finding's own recorded versions.

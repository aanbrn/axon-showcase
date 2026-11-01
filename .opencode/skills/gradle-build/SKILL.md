---
name: gradle-build
description:
  The Gradle build for this repository — convention plugins, the version catalog, dependency and tool pins, the check
  gates, the build command surface, the dependency-update and security reports, and the build failures that read like
  application defects (a test run and its coverage rule are `jvm-testing`'s, and the deployment and local-stack commands
  are `deployment-operations`'s).
---

# Gradle build

It carries the build and dependency-management content the root `AGENTS.md` used to hold; the root's classification
table owns when it loads and states this pack's trigger.

Source: `AGENTS.md` `## Conventions` (Checkstyle, SpotBugs, LZ4 relocation, build-tool versions in the version catalog,
a convention plugin's verification, `-parameters`), `## Build & Test` (the Gradle command surface, the build-environment
constraint, the dependency-update suppression lists, calendar-versioned coordinates), and `## Gotchas` →
`### Build, Gradle & dependency management`.

The authoritative reasoning for each suppressed or held-back coordinate is the `showcase/quality/dependency-management`
spec's; this pack carries the operational rules a change needs, not a second copy of that reasoning.

## Build conventions

- **Checkstyle**: style gate wired into `check` via `code-check-conventions.gradle.kts`; ruleset at
  `config/checkstyle/checkstyle.xml`, generated sources excluded via `config/checkstyle/suppressions.xml`
- **SpotBugs**: finds bugs with findsecbugs and fbContrib plugins; uses `spotbugs-include.xml` and
  `spotbugs-exclude.xml` filters in `config/spotbugs/` if present (see `code-check-conventions.gradle.kts`)
- **LZ4 relocation**: root build forces `org.lz4:lz4-java` substitution (an unmaintained, CVE-affected line; see
  ADR-0020 and `build.gradle.kts`)
- **Build-tool versions live in the version catalog**: a `build-logic` convention plugin never hard-codes a tool version
  — it declares it in `gradle/libs.versions.toml` and reads it via `val libs = the<LibrariesForLibs>()` /
  `libs.versions.<name>.get()` (runtime `node`/`java`, plugin `toolVersion` `checkstyle`/`spotbugs`/`jacoco`, the
  ErrorProne/NullAway and protobuf artifacts, buildpack ids, image tags); see ADR-0015 for the rationale and the
  rejected alternatives. A version that is not a `group:name` dependency (a buildpack id, a builder tag, `node`) is a
  `[versions]`-only entry with no `[libraries]` module. Catalog ownership is single-sourcing, not update tracking: a
  bare `[versions]` entry is not resolved as a dependency, so `dependencyUpdates` ignores it — but `helmUpdates` reads
  the Helm CLI and chart pins, `buildpackUpdates` the Paketo builder and buildpack pins, `toolingUpdates` the CLI
  versions pinned in workflow files, and `buildLogicUpdates` the plugin pins the included `build-logic` build resolves
  (which the main build's own report cannot see), so only the entries none of them reads must be audited by hand. The
  buildpack/builder pin mechanics — the pair the pins must form, the run-the-image verification, and the CNB id versus
  Docker Hub repository — are the `deployment-operations` pack's. The tool-version rule reaches a plugin a build script
  applies directly too — the root's `jacoco` included: set its `toolVersion` from the catalog, because an omitted one is
  not unpinned but whatever the Gradle distribution bundles, older than the catalog's pin here, so one build ran two
  JaCoCo versions the scan reported separately. Grep each script that applies the plugin for a `toolVersion` assignment
  rather than trusting the plugin's version. captured: address-snyk-advisories-2026-11-01
- **A `build-logic` convention plugin's effect is verified through a consuming module, not unit-tested.** `build-logic`
  has no `gradleTestKit()`, so its `src/test` holds unit tests of plain helper objects (`Versions`, the `*Rules`
  classes) and a plugin's Gradle DSL — e.g. `spring-boot-conventions`' `springBoot { buildInfo { … } }` — cannot be
  exercised there. Assert the effect in a module that applies the plugin (the four JVM services' ITs assert the baked
  `BuildProperties`) rather than writing a test that cannot reach it. captured: expose-service-build-info
- **All JavaCompile tasks** add `-parameters` flag

## Commands

```bash
# Full build (all modules)
./gradlew build

# Build a single service (also produces Docker image via bootBuildImage)
./gradlew :showcase-command-service:bootBuildImage

# Check runs: compile → spotless/checkstyle/spotbugs/errorprone → test → componentTest → integrationTest,
# plus the chart packaging, the workflow lint, and the repository's verification tasks — the `check` block in
# `build.gradle.kts` enumerates them.
# (a Docker-free check is -PskipITs -Pcoverage.gate.enabled=false — see the coverage-gate gotcha; e2e is never part of
# check)
./gradlew :showcase-command-service:check

# Dependency security scan (Snyk; requires the Snyk CLI on PATH, not part of check)
./gradlew dependencySecurityCheck
# The scan passes --policy-path=.snyk (the root Snyk policy). The currently-suppressed findings, their expiry
# rationale live in `.snyk` (its header comment and each ignore's `reason`) — see those rather than restating them here.
# A Snyk plan's quota and reset schedule are the provider's to state, so the command reads them from the provider at the
# time it matters rather than pinning them. A version-pinned ignore goes dead silently once a constraint or bump moves
# the coordinate past its pinned version — unlike the web UI's `npm-audit-ignores.json`, which `npmAudit` fails on when
# an entry matches no finding, the `.snyk` policy has no staleness guard — so sweep the ignore and its rationale
# comment in the change that moves past it. Removing one rests on "no consumer remains", a claim over every module and
# configuration that resolves the coordinate, not the one classpath the change touched (a custom suite re-declares its
# edges) — run the credentialed `dependencySecurityCheck` in that change rather than leaving it to the weekly run.
# captured: address-scan-findings-2026-10-25

# Dependency update report (only catalog-owned coordinates; majors suppressed per
# config/dependency-updates/major-disabled.txt and same-major updates held back per
# config/dependency-updates/hold-back.txt)
./gradlew dependencyUpdates

# Build-logic plugin pin update report — the pins only the included build resolves (not part of check)
./gradlew buildLogicUpdates
```

- The `/dependency-updates` OpenCode command runs these reports and summarizes the available updates; the
  `/gradle-update` command updates the Gradle wrapper to the latest stable version when one is available.

Build-environment constraints can surface as spurious "current version" rows in the report: the build tooling itself
publishes module constraints that `checkBuildEnvironmentConstraints` reads and reports as the current version. For
example, `dependencyUpdates` reported `log4j-core [2.17.1 -> 2.26.1]` although `log4j-core` resolves to `2.26.1`
everywhere — `2.17.1` is the floor of the Log4Shell ban **Gradle itself** applies to every classpath it builds
(`Log4jBannedVersion` in the distribution's `gradle-logging` jar). Such a row is not a real update (upstream
`ben-manes/gradle-versions-plugin#755`), and ADR-0021 filters it in the report's own rules: the plugin consults
`rejectVersionIf` for this path, so a candidate no newer than the version the catalog declares is rejected **when the
plugin's reported current differs from that version**. Keep both halves of that condition — the plugin treats a
coordinate whose every candidate is rejected as unresolved, so a rule that also rejects an up-to-date coordinate's own
version moves every catalog-owned entry out of the report's up-to-date section. When documenting such an external
constraint, name the coordinate and the mechanism, not the constraint's version: that version is the _tool's_ guard, not
a version this repository pins, so the `AGENTS.md` docs-refresh de-versioning clause applies.

Major-blocking entries in `config/dependency-updates/major-disabled.txt` and held-back entries in
`config/dependency-updates/hold-back.txt` carry a pointer comment naming the coordinate and its rationale; the
authoritative reasoning for each suppressed or held-back coordinate lives in the
`showcase/quality/dependency-management` spec. Both files are line lists parsed on the first `=`, so a `group:module`
coordinate keeps its colon (see `AGENTS.md`'s `### Agent tooling, OpenCode & permissions`, the config-read-path bullet
for the `Properties` mis-parse this avoids).

For calendar-versioned coordinates (leading segment is a 4-digit year, e.g. Spring `YYYY.MINOR.MICRO` such as
`reactor-bom` on the `2025.0` train), the report treats a change in the `YYYY.TRAIN` pair (the first two version
segments) as a major update — matching Spring's release-train definition where `2025.0` and `2025.1` are distinct trains
— while a change only in the service-release (third) segment within the same train is a minor/patch update. Semver
coordinates keep the leading-integer major comparison.

## Build, Gradle & dependency management

- **A build property's name is shared across the module's tasks — grep the plugin's existing `gradleProperty(...)`
  consumers before forwarding a new `-P<name>`.** `load-tests/build.gradle.kts` forwards its whitelisted properties into
  the Gatling task's `systemProperties`, but the same module's `kneeFinder` task reads `-Pknee` as its **output-file
  path** — so the knee-rate property had to be `kneeRate`, since a reused `knee` would forward a path where the
  simulation parses an integer. A JavaExec task runs with the module's project directory as its working directory, so a
  relative `-Plog`/`-Precords` resolves under `load-tests/`, not the repository root — hand-run one with the absolute
  path the wrapper passes (`$ROOT/...`). captured: check-load-test-drift

- **The configuration cache is enabled (`org.gradle.configuration-cache=true`); a task action must not reference
  anything belonging to the Gradle script** — `project`, `gradle`, `rootProject`, `providers`, a script-level
  `val`/`fun`, the script's own `this` — because serializing the captured **script object** fails the store; compute
  what the action needs into a `val` local to the configuration lambda (a class/object reference is safe) — but a file
  tree the task scans is a declared task input (`@InputFiles`) resolved in the action, never a hoisted path list, since
  a warm cache replays the configuration-time list and a file added after the store goes unscanned, and replays
  configuration rather than running it, so a warning a plugin emits while configuring is absent from a reused run — a
  warm run's silence is not the plugin's fix (the deprecation bullet below owns the probe). captured:
  bump-dependencies-2026-11-01 It needed gradle-helm-plugin **3.2.0** or newer (its tasks captured a `Task`/`Project`
  through 3.1.2 — build-extensions-oss/gradle-helm-plugin#140, closed), that hoist at each task action that had it (the
  workflow-lint, dependency-security, frontend npm, load-tests, and compose tasks — whose `onlyIf` also read
  `gradle.startParameter` and a `rootProject.extra` handshake, now `composeScheduleState`), and
  `notCompatibleWithConfigurationCache` on `:load-tests:gatlingRecorder`, whose third-party task type holds a
  `Configuration`/`SourceSet` no hoist can fix (the plugin repository disables issues and discussions, so an upstream
  fix must arrive as a PR). Sweep such a change by **executing** every task a workflow invokes, not by realizing the
  graph: `./gradlew tasks --all`'s set never runs a task action, so a task no `check` graph requests —
  `upstreamReferences`, invoked only by its own workflow — escapes every sweep the build itself can run, and its store
  defect surfaces only in CI, on a schedule, where no gate sees it (#581 missed it for a week). Run each such task with
  the cache enabled and require a stored entry, then a reused one. A plugin task type is one failure shape (it cannot
  store); a third-party task whose `onlyIf` reads build state is another: it stores cleanly and then silently skips,
  because the cache resolves the predicate against the state captured at store time. Compare each such task's _outcome_
  (ran / skipped) with and without the cache on a clean checkout, not only that the graph stores; and neutralize such a
  gate with `setOnlyIf { true }`, since a bare `onlyIf { true }` only appends a spec that is ANDed with the plugin's and
  leaves it in force (the helm plugin's chart-dependency skip is the worked case — see the `deployment-operations`
  pack's Chart validation). captured: fix-upstream-references-config-cache captured:
  add-a-markdown-table-integrity-check

- **IF a Gradle run under `--warning-mode all` emits a gradle-helm-plugin deprecation or a `Configuration.visible`
  warning, THEN treat it as upstream and leave it: there is no repo-side fix.** gradle-helm-plugin 3.3.0 still calls the
  deprecated `Project.getProperties()` (a hard error in Gradle 10), from its
  `HelmPlugin.createRepositoriesFromProjectProperties` once per helm project, and only on a run whose
  configuration-cache entry is not reused (so probe with `--no-configuration-cache`;
  `-Dorg.gradle.deprecation.trace=true` names the emitting class); tracked upstream as
  build-extensions-oss/gradle-helm-plugin#145, so bump the plugin when a fixed release lands. The
  `Configuration.visible` deprecation (`setVisible(boolean)`, removed in Gradle 11) is emitted by several third-party
  plugins (helm, errorprone, SpotBugs, Spring Boot, protobuf) and is likewise plugin-side. Do not add a local workaround
  for either.

- **A task whose verdict derives from wall-clock time or external state must not declare an output Gradle can re-use — a
  cached UP-TO-DATE run serves a stale verdict.** `npmAudit` first declared `outputs.file(...)`, so a re-run was
  UP-TO-DATE and its `doLast` verdict — including the `LocalDate.now()` suppression-expiry check — was never re-derived:
  a cached pass could outlive a suppression's expiry. Drop the `outputs.file` declaration (matching the sibling
  `npmOutdated` task) so the verdict re-runs every invocation; declare `inputs` (the suppression file) without declaring
  an output the task does not produce as an artifact. Any verdict task whose result changes without its inputs changing
  — a date check, a live registry query, a remote state read — takes the same treatment. Verify by running the task
  twice with nothing changed: the second run must re-execute, not report UP-TO-DATE. captured:
  suppress-braces-npm-advisory

- **A fixable npm advisory in a transitive that a dev dependency pins exactly is forced with an `overrides` entry — not
  npm's suggested fix.** npm's offered fix is a _breaking_ downgrade of the pinning package (`eslint-plugin-boundaries`
  7.2.0 → 5.4.0, breaking the FSD boundary lint), because the plugin and `@boundaries/elements` then pinned
  `handlebars: "4.7.9"` exactly; `overrides` forced the patched `4.7.10` under them instead. A suppression
  (`npm-audit-ignores.json`) is for an advisory with no fix — the `dependency-security` spec owns that policy — and
  unlike one the override carries no expiry, so record its re-check where a sweep reads it: with an upstream report, a
  `retires when` clause beside its `owner/repo#NNN` reference in a file `upstreamReferences` scans (that report fires on
  the reference, not on the clause); without one, a parked entry in `docs/ideas.md`. Either way derive the retrigger
  from the resolved graph, since a pinning package may raise its range or drop the dependency outright (`effect` dropped
  `toml`, never bumped it), so state it as no resolution reaching a vulnerable range. Verify the override took effect
  with `npm ls <pkg>` (the patched version resolves). captured: bump-web-ui-npm-2026-10-22 captured:
  override-opencode-plugin-transitives

- **A build tool's own vulnerable transitive does not reach the platform BOM, so its fix is the narrowest surface that
  carries the patched set — the tool's own newer release first, otherwise a constraint on that tool configuration.** A
  tool release that ships the patched transitive is the single source (JaCoCo's publishes the patched ASM), so bump the
  catalog's tool version rather than pinning the transitive beside it; a tool already at its newest release that pins
  the vulnerable one (SpotBugs on ASM `9.11`) takes the constraint. The platform's `constraints` reach the
  compile/runtime classpaths, not Gradle's tool configurations: `errorprone(platform(project(":platform")))` works (its
  `annotationProcessor` classpath extends it), while `checkstyle(platform(project(":platform")))` drops the checkstyle
  tool and fails `checkstyleMain` with `ClassNotFoundException: CheckstyleAntTask` — constrain that one with
  `constraints { checkstyle(libs.guava) }`. A scan names the coordinate, not the configuration carrying it, so derive
  the owning configuration from the graph (`./gradlew :<module>:dependencies --configuration <config>`) before choosing
  the fix — and it names one artifact of a family, not the family, so fix the set the findings enumerate with the
  family's BOM: a constraint on the core jar alone left the five native artifacts the run listed at the vulnerable
  version, while the family's BOM manages every artifact it publishes. A plain `compileJava` never exercises the tool
  configuration — only the tool task does — so verify the fix by running the tool task (e.g.
  `./gradlew :<module>:checkstyleMain`), not by compiling, and verify a family by resolving every artifact of it on the
  configuration the findings came from. captured: fix-new-security-advisories captured:
  address-snyk-advisories-2026-11-01

- **The evidence a suppression rests on is the advisory page's own fixed-version line and affected range, not the
  absence of a newer release.** Its line states "There is no fixed version" where that is so, and the range shows
  whether the newest release clears it (`net.minidev:json-smart` affects `[0,]`, so its newest release does not, and a
  bump would move a test runtime for no security gain); a page that states a fixed version means a bump or a constraint
  is the route, and the `dependency-security` spec's no-fix-only policy governs the entry. captured:
  address-snyk-advisories-2026-11-01

- **A suppression covers the package it names, not a vulnerable transitive of that package.** That transitive is its own
  finding and takes its own fix: suppressing `json-smart` left `accessors-smart`'s ASM reported, and the ASM needed a
  constraint of its own. captured: address-snyk-advisories-2026-11-01

- **A Maven coordinate's newest release is read from the repository's `maven-metadata.xml`, never from a search API —
  the search index lags the repository, and its stale answer reads exactly like "no newer release exists".** That
  reading decides whether a fix exists, so read the `<versions>` list rather than `<latest>`/`<release>`, which can name
  a pre-release — the reason `BuildLogicPins` skips that element — and take the newest stable entry the coordinate's own
  metadata serves. captured: address-snyk-advisories-2026-11-01

- **Custom Gradle test suites (`componentTest`, `integrationTest`, `e2eTest`) do not inherit the project's
  `implementation`-only dependencies on their compile classpath** — the default `test` suite does, since its classpath
  extends `main`'s, while a custom suite receives them only at runtime through its `project()` dependency — so each
  suite re-declares what it needs to compile (client component suites duplicate axon/opensearch/resilience4j deps,
  `showcase-query-proto` must be listed explicitly, and a suite compiling against a `@Mapper`-annotated main class needs
  `libs.mapstruct`, or javac warns that the annotation's `ReportingPolicy` constant is unknown). A re-declared edge
  either carries its sibling edges' `exclude(...)` blocks or is dropped — an exclusion is per-edge, so a duplicated
  declaration silently restores the tree it removed, and a suite that already depends on the module exporting the
  dependency has no second declaration to maintain (a `componentTest` edge re-declared `libs.spring.data.opensearch`
  without the `opensearch-rest-high-level-client` exclusion `project(":showcase-projection-model")`'s `api` edge
  applies, putting a vulnerable server tree back on the test classpath; deleting the duplicate cleared it). captured:
  address-scan-findings-2026-10-25 A suite can be referenced in `shouldRunAfter(...)` only when bound as a `val` (e.g.
  `val integrationTest = suites.register<JvmTestSuite>("integrationTest")`). Source sets are siblings — `src/test/java`
  cannot see a type declared in `src/gatling/java`, while both see `main` — so a pure-logic type a unit test and a
  custom source set both need belongs in the module's `main` source set. Verify a suite's classpath by compiling its
  source set, which surfaces a missing dependency directly. captured: check-load-test-drift captured:
  fix-mapstruct-suite-warning

- **A dependency a module does not use can still be load-bearing — narrowing a `project(...)` dependency can break a
  downstream `compileJava`, and removing a starter drops the modules it transitively provided.** Before removing or
  narrowing a dependency, grep the consumers' sources for what they actually import and compile the affected modules; an
  unused direct dependency may be carrying the transitive one a consumer's main source needs. Worked case:
  `showcase-query-api` held `api(project(":showcase-command-api"))` only for `showcase.identifier.KSUID`; narrowing it
  to `api(project(":showcase-identifier-extension"))` (`narrow-query-api-dependency`) broke `:showcase-query-client`'s
  main compile, because `ShowcaseQueryClientProperties` imports `org.hibernate.validator.constraints.URL` and had been
  receiving it through `command-api`'s `api(libs.hibernate.validator)` — that consumer now declares it. The same holds
  for an external starter: removing `spring-boot-starter-webflux` from `showcase-query-client` silently removed the
  `jackson-datatype-jsr310` it had provided, and `JacksonSerializer.defaultSerializer()` failed at test-class init with
  `NoClassDefFoundError: JavaTimeModule` — name every module a removed starter was providing, declare it explicitly
  (`jackson-databind`, `jackson-datatype-jsr310`), and run the module's tests, not only `compileJava`. captured:
  grpc-query-transport

- **`./gradlew check -PskipITs` alone fails the coverage gate — the Docker-free check also passes
  `-Pcoverage.gate.enabled=false`.** `-PskipITs` excludes `integrationTest` from the jacoco exec data, so
  `jacocoTestCoverageVerification` verifies against a figure below the 0.80 baseline and fails; the gate is added to
  `check` only when `coverage.gate.enabled` is not `false` (`code-coverage-conventions.gradle.kts`). The PR CI gate is
  exactly `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` (see `AGENTS.md`'s `## Build & Test` for the
  Docker-free form) — run that for a local Docker-free check, not the bare `-PskipITs` form. That form disables the
  coverage gate exactly as the PR gate does, so it cannot catch a coverage regression: when a change adds production
  code to a gated module, run that module's `check` with the gate enabled before merging — Docker-free with `-PskipITs`
  for a module with no integration tests (`./gradlew :showcase-query-proto:check -PskipITs`), or the full
  `./gradlew check` — since the drop otherwise merges green and reddens the push-to-main full `check`
  (`grpc-query-transport` (#514) added `QueryMessageMapper.messageToResponse`/`payloadFromResponse` with no module test,
  `showcase-query-proto` fell to 0.60 against 0.80, and every full `check` since failed until a component test covered
  them). The same Docker-free form also drops the `integrationTest` and `e2eTest` classpaths (`check` never includes
  `e2eTest`), so a resolution-affecting change — a dependency, repository, or plugin declaration, or a BOM — compiles
  them explicitly (`./gradlew compileIntegrationTestJava compileE2eTestJava --refresh-dependencies`) and names the tiers
  its resolution claim covers, rather than resting that claim on the Docker-free check. captured:
  fix-main-ci-regressions captured: remove-unrecorded-redgate-repository

- **Bumping the Gradle wrapper needs two `wrapper` runs with `--distribution-type all`.** The first run only rewrites
  `distributionUrl`; the second, executing under the new version, regenerates `gradle-wrapper.jar`/`gradlew`/
  `gradlew.bat`. A single run leaves the jar and scripts at the old version, and a run without the flag flips the repo's
  `-all` distribution to `-bin` — both silently. `./gradlew --version` cannot see either (it reports the version from
  `distributionUrl`), so verify by diffing all four wrapper files (`gradle/wrapper/gradle-wrapper.properties`,
  `gradle/wrapper/gradle-wrapper.jar`, `gradlew`, `gradlew.bat`) and confirming `distributionUrl` still ends `-all.zip`;
  if only `gradle-wrapper.properties` changed, the bump is incomplete. Keep `/gradle-update`'s two-step invocation. The
  wrapper pins no `distributionSha256Sum`, and the bump has no rollback step. IF a wrapper run fails, or the four files
  disagree after the second run, THEN `/gradle-update` reports what it found and does not hand-edit the wrapper files;
  recovering them is the operator's step. captured: bump-gradle-and-web-ui-dependencies

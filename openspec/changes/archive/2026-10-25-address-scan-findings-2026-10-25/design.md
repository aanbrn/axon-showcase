# Design

## Context

The scheduled `Dependency Security` workflow runs two jobs, and both are red on `main` (`40125920555`, 2026-10-24).

### The `snyk` job

`showcase-projection-service` reports two High advisories in the same coordinate, with the same introduction path:

```text
✗ Inefficient Algorithmic Complexity [High] [SNYK-JAVA-COMTDUNNING-19659413] in com.tdunning:t-digest@3.3
✗ Improper Validation of Specified Quantity in Input [High] [SNYK-JAVA-COMTDUNNING-19778375] in com.tdunning:t-digest@3.3
    introduced by org.opensearch.client:spring-data-opensearch@2.0.8 >
    org.opensearch.client:opensearch-rest-high-level-client@3.9.0 >
    org.opensearch:opensearch@3.9.0 > com.tdunning:t-digest@3.3
  No upgrade or patch available
```

Neither advisory is new: both were `.snyk` ignores until 2026-10-16 (`git show 5c605afd^:.snyk`), each pinned to
`* > com.tdunning:t-digest@3.3` with the reason "Gatling charts transitive (load-tests only); no patched t-digest
release available". `bump-build-logic-pins-2026-10-16` removed both blocks because the Gatling bump moved
`gatling-charts` off `com.tdunning`, verified on `:load-tests:dependencies --configuration gatlingRuntimeClasspath` —
one consumer, not every consumer. That change's own task 3.2 counted the blocks by reading the ignore set but never
re-derived which modules resolved the coordinate.

The remaining consumer is the projection service's `componentTest` suite, and dependency resolution confirms the edge:

```text
$ ./gradlew :showcase-projection-service:dependencyInsight \
    --configuration componentTestCompileClasspath --dependency t-digest
com.tdunning:t-digest:3.3
\--- org.opensearch:opensearch:3.9.0
     \--- org.opensearch.client:opensearch-rest-high-level-client:3.9.0
          +--- project ':platform'
          \--- org.opensearch.client:spring-data-opensearch:2.0.8 (requested org.opensearch.client:opensearch-rest-high-level-client:3.6.0)
```

The coordinate resolves on the module's `componentTestCompileClasspath` and `componentTestRuntimeClasspath`; its
`runtimeClasspath` carries no `t-digest` (`:showcase-projection-service:dependencies --configuration runtimeClasspath`),
and the scan flags no other project for it. The two classpaths are the suite's:

```kotlin
val componentTest =
    suites.register<JvmTestSuite>("componentTest") {
        dependencies {
            implementation(project(":showcase-projection-model"))
            implementation(libs.axon.test)
            implementation(libs.hamcrest)
            implementation(libs.opensearch.client.java)
            implementation(libs.spring.boot.starter.test)
            implementation(libs.spring.data.opensearch)
        }
```

`libs.spring.data.opensearch` is added there without the `opensearch-rest-high-level-client` exclusion, and that
declaration arrived in `idempotent-projection-writes` (`65196058`, #670). It is also **redundant**: the suite already
declares `project(":showcase-projection-model")`, whose `api` edge declares the same dependency _with_ the exclusion, so
the second declaration supplies nothing the model does not and reintroduces the tree the model's exclusion removes. The
module's main (`spring-data-opensearch-starter`) and `integrationTest` (`spring-data-opensearch-testcontainers`) edges
carry the same exclusion, and ADR-0012 requires the legacy high-level client off every classpath that talks to the
cluster. The suite's sources import `org.opensearch.client.opensearch.*` and `org.opensearch.data.client.osc.*` (the
opensearch-java client and Spring Data's OSC template), never a legacy high-level-client type — which is why the
redundant declaration can go rather than gain an exclusion of its own.

`com.tdunning:t-digest` has no patched release: Maven Central's newest version is `3.3` (2021-06-01), and Snyk reports
"No upgrade or patch available" for both advisories.

### The `web-ui-audit` job

`npm audit` reports `found 0 vulnerabilities`, and the task then fails its own staleness rule:

```text
> Task :showcase-web-ui:npmAudit FAILED
    - suppression entry 'GHSA-vfj7-8cjw-p6xm' matches no finding in the audit report
```

The advisory was withdrawn upstream, not fixed locally (GitHub's advisory API):

```json
{
  "ghsa_id": "GHSA-vfj7-8cjw-p6xm",
  "cve_id": "CVE-2026-93687",
  "severity": "high",
  "summary": "Withdrawn Advisory: braces vulnerable to stack-exhaustion denial of service through deeply nested patterns",
  "withdrawn_at": "2026-10-23T12:53:02Z"
}
```

`braces@3.0.3` still resolves (`npm ls braces` → `eslint-plugin-boundaries@7.2.1` → `micromatch@4.0.8` →
`braces@3.0.3`), so the package did not move; only the advisory's status did. The suppression entry is now dead, and the
staleness rule the `dependency-security` spec requires is what surfaced it.

## Goals / Non-Goals

**Goals:**

- Get the coordinate off the only classpath that carries it, so the Snyk scan reports no vulnerable path without a new
  ignore.
- Remove the suppression entry the withdrawn advisory made stale.

**Non-Goals:**

- Restoring the two `.snyk` ignores (see D2).
- Changing the npm staleness rule or relaxing it.
- Bumping or suppressing `opensearch`, `spring-data-opensearch`, `opensearch-java`, or `braces` (no release moves either
  finding).

## Decisions

**D1: Remove the suite's redundant `spring-data-opensearch` declaration, rather than add a second exclusion or suppress
`t-digest`.** The defect is that #670 re-declared a dependency the suite already receives through
`project(":showcase-projection-model")`'s `api` edge, without the exclusion that edge carries; the fix is to delete the
duplicate, so one declaration (with one exclusion) serves the suite. Adding the exclusion to the duplicate — the form
this change first took — leaves two declaration sites for one dependency, and the next edit to either can silently drop
the exclusion; an independent consultation showed the declaration was unnecessary (the removal left the suite's resolved
classpath unchanged). Either form removes the whole `org.opensearch:opensearch` server tree from a test classpath that
does not use it, which also removes both advisories and every other server-jar transitive the scan would otherwise have
to assess; removing the declaration is the smaller build surface. _Alternatives considered:_ a second exclusion on the
duplicate (the larger maintenance surface, above); a version constraint (impossible — the coordinate's newest release is
the vulnerable one); a repo-wide substitution to a maintained fork (none exists for `t-digest`; ADR-0020's `lz4`
substitution had one); keeping the dependency and suppressing (D2).

**D2: Do not restore the `.snyk` ignores.** A suppression is for an advisory with no way to remove it. Here the
vulnerable coordinate has a consumer that does not need it, so the fix is available and the ignore would re-accept a
High finding the build can stop shipping — the same reasoning `bump-build-logic-pins-2026-10-16` applied to the
load-tests consumer, only with the right consumer set. The removed ignores' stated reason was also wrong ("load-tests
only"), so restoring them would restore a false claim. If a future consumer reintroduces the coordinate, the scan
surfaces it as an unsuppressed finding and a suppression (or an exclusion) is decided then.

**D3: Remove the npm entry; do not weaken the staleness rule.** The `dependency-security` spec requires `npmAudit` to
fail on a suppression whose advisory id matches no finding, so a suppression cannot sit silently after a fix or a
withdrawal. Removing the entry keeps the list truthful (empty), and the mechanism stays armed for the next unfixable
advisory. An empty `suppressions` array is a valid list for `NpmAuditRules`.

**D4: No spec delta.** The `showcase/quality/dependency-security` spec states the outcomes both fixes restore — the scan
reports no vulnerable paths, and only fixless advisories are suppressed — and neither fix changes a scenario. The
dependency wiring is a build mechanism whose alternatives yield the same outcome, which `AGENTS.md`'s extraction rules
keep out of the capability spec. `skip_specs: true` records the determination.

## Risks / Trade-offs

- [The suite now receives `spring-data-opensearch` only through `showcase-projection-model`'s `api` edge] → the
  declaration that went was redundant, so the resolved `componentTest` classpath is unchanged (Gradle reported the
  compile `UP-TO-DATE` after the removal), and a future narrowing of that `api` edge fails the suite's compile rather
  than silently changing its runtime.
- [`spring-data-opensearch` may auto-configure a legacy template and need the client at runtime] → the module's
  production context boots without it today (same exclusion), so a component-test context cannot need it more; the suite
  run proves it.
- [Removing the npm entry leaves the list empty] → `npmAudit` still enforces the high-severity verdict and the staleness
  and expiry rules; an empty list suppresses nothing, which is the intended state.

## Verification

- `./gradlew :showcase-projection-service:dependencyInsight --configuration componentTestCompileClasspath --dependency t-digest`
  reports no match after the removal, while `--dependency spring-data-opensearch` still resolves it through
  `project(":showcase-projection-model")`.
- `./gradlew :showcase-projection-service:componentTest` runs green without the legacy client.
- `./gradlew :showcase-projection-service:check -PskipITs -Pcoverage.gate.enabled=false` and
  `./gradlew compileIntegrationTestJava compileE2eTestJava --refresh-dependencies` compile the tiers the Docker-free
  check drops (their compile classpaths; no IT/e2e runtime classpath is resolved).
- `./gradlew :showcase-web-ui:npmAudit` passes with the empty list.
- `./gradlew dependencySecurityCheck` (credentialed) reports no vulnerable path, and the scheduled workflow's next run
  is green.

## Claims table

Seeded with `python3 scripts/claim-scan.py`; every candidate it printed is a row below or a dismissal beside the table.

| Claim                                                                                                                                                                                                                                                                                                                                                               | Source                                                                                                                                                                                                        |
| ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Run `40125920555` failed both jobs (`snyk`, `web-ui-audit`)                                                                                                                                                                                                                                                                                                         | `gh run view 40125920555 --json jobs`                                                                                                                                                                         |
| The `snyk` job reports the two advisories, their High severity, the introduction path, and "No upgrade or patch available"                                                                                                                                                                                                                                          | the run's log: `gh run view 40125920555 --log`                                                                                                                                                                |
| The same run's `web-ui-audit` job reports `found 0 vulnerabilities` and fails on `suppression entry 'GHSA-vfj7-8cjw-p6xm' matches no finding`                                                                                                                                                                                                                       | the same log                                                                                                                                                                                                  |
| Both advisories were `.snyk` ignores until 2026-10-16, pinned to `* > com.tdunning:t-digest@3.3`, reason "Gatling charts transitive (load-tests only); no patched t-digest release available"                                                                                                                                                                       | `git show 5c605afd^:.snyk`                                                                                                                                                                                    |
| The removal verified one consumer (`gatlingRuntimeClasspath`) and counted the ignore set                                                                                                                                                                                                                                                                            | `openspec/changes/archive/2026-10-16-bump-build-logic-pins-2026-10-16/tasks.md`, tasks 1.3 and 3.2                                                                                                            |
| `t-digest` resolves on the projection service's two `componentTest` classpaths through `spring-data-opensearch` → `opensearch-rest-high-level-client` → `org.opensearch:opensearch`, and not on `runtimeClasspath`                                                                                                                                                  | `:showcase-projection-service:dependencyInsight --configuration <cfg> --dependency t-digest` and `dependencies --configuration runtimeClasspath`, run before the declaration was removed                      |
| The `componentTest` edge lacks the exclusion, the module's other three edges carry it (main starter, `integrationTest` testcontainers, `showcase-projection-model` api), and the suite's own `project(":showcase-projection-model")` dependency already supplies `spring-data-opensearch` through that excluding `api` edge, so its direct declaration is redundant | `git show HEAD:showcase-projection-service/build.gradle.kts` and `git show HEAD:showcase-projection-model/build.gradle.kts`                                                                                   |
| The declaration arrived in `65196058` (#670)                                                                                                                                                                                                                                                                                                                        | `git log -L 68,80:showcase-projection-service/build.gradle.kts`                                                                                                                                               |
| The suite imports only OSC (`org.opensearch.data.client.osc.*`) and opensearch-java (`org.opensearch.client.opensearch.*`) types                                                                                                                                                                                                                                    | `grep -rn '^import org.opensearch' showcase-projection-service/src/componentTest`                                                                                                                             |
| `com.tdunning:t-digest`'s newest release is `3.3` (2021-06-01)                                                                                                                                                                                                                                                                                                      | Maven Central: `curl -s 'https://search.maven.org/solrsearch/select?q=g:com.tdunning+AND+a:t-digest&core=gav&rows=20&wt=json'`                                                                                |
| The removal keeps the coordinate off both `componentTest` classpaths, and `spring-data-opensearch` still resolves through `project(":showcase-projection-model")`                                                                                                                                                                                                   | the same `dependencyInsight` command after the edit (no `t-digest` match; `spring-data-opensearch` shown under the model)                                                                                     |
| ADR-0012 requires the legacy high-level client off every classpath that talks to the cluster                                                                                                                                                                                                                                                                        | `docs/adr/0012-prefer-typed-opensearch-java-client.md`                                                                                                                                                        |
| Every edge that pulls the starter carries the exclusion: `showcase-projection-service` main, `showcase-query-service` main and `componentTest`, `showcase-query-client` `componentTest`, plus `showcase-projection-model`'s `api` and the two `testcontainers` edges                                                                                                | the four `build.gradle.kts` files: `grep -n 'spring.data.opensearch' -A4 <each>`                                                                                                                              |
| `componentTest` passes without the legacy client (`--rerun`, so it executed rather than reporting `UP-TO-DATE`)                                                                                                                                                                                                                                                     | `./gradlew :showcase-projection-service:componentTest --rerun` — BUILD SUCCESSFUL as of that run                                                                                                              |
| The module's gates pass                                                                                                                                                                                                                                                                                                                                             | `./gradlew :showcase-projection-service:check -PskipITs -Pcoverage.gate.enabled=false` — BUILD SUCCESSFUL as of that run                                                                                      |
| The `integrationTest`/`e2eTest` classpaths still compile                                                                                                                                                                                                                                                                                                            | `./gradlew compileIntegrationTestJava compileE2eTestJava --refresh-dependencies` — BUILD SUCCESSFUL as of that run                                                                                            |
| The credentialed scan reports no vulnerable path across its 20 projects                                                                                                                                                                                                                                                                                             | `./gradlew dependencySecurityCheck` — `Tested 20 projects, no vulnerable paths were found.` as of that run                                                                                                    |
| `GHSA-vfj7-8cjw-p6xm` was withdrawn on 2026-10-23T12:53:02Z                                                                                                                                                                                                                                                                                                         | `gh api /advisories/GHSA-vfj7-8cjw-p6xm` (an external surface, re-run at review)                                                                                                                              |
| `braces@3.0.3` still resolves, as a dev-only transitive of `eslint-plugin-boundaries@7.2.1` → `micromatch@4.0.8`                                                                                                                                                                                                                                                    | `npm ls braces` in `showcase-web-ui`                                                                                                                                                                          |
| `micromatch/braces#72` closed unmerged on 2026-10-05, and `micromatch/braces#70` is open and locked                                                                                                                                                                                                                                                                 | `gh pr view 72 --repo micromatch/braces --json state,closedAt,mergedAt` (CLOSED, `mergedAt: null`) and `gh api repos/micromatch/braces/issues/70 --jq '{state,locked}'` — external surfaces, re-run at review |
| The empty suppression list passes the audit                                                                                                                                                                                                                                                                                                                         | `./gradlew :showcase-web-ui:npmAudit` — "clean (0 suppression(s) applied)" as of that run                                                                                                                     |
| The `NpmAuditRules` cases pass with an empty list                                                                                                                                                                                                                                                                                                                   | `./gradlew :build-logic:test` as of that run                                                                                                                                                                  |

**Dismissals, by candidate kind:**

- `[path]` — each candidate names a repository path, a task number, or a Gradle/`gh` command that exists; existence was
  verified by reading or running it, and the fact a sentence states about it is a row above.
- `[cross-ref]` — `#670` resolves to `65196058` (`git show`), `Build/tests` is a proposal section heading, and
  `AGENTS.md` / `showcase/quality/dependency-security` name live artifacts.
- `[absolute]` — each qualifier (`only`, `every`, `none`, `never`) is scoped to its sentence and carried by a row above;
  where one quantifies over a set, the Context names its members.

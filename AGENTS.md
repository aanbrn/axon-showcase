# AGENTS.md

## Line Length

- Wrap code and text at 120 characters. Markdown (`docs/`, `AGENTS.md`, `README.md`, `openspec/specs/`, and active
  `openspec/changes/*/`; archived changes are excluded) is formatted automatically by the root Spotless `markdown`
  format (Prettier at `printWidth: 120` with `proseWrap: "always"`), gated in `check` via `spotlessCheck` — no manual
  wrapping needed for those files; Java/Kotlin are gated by Spotless too. See the `Formatting` convention for the
  per-file-type mechanics.

## Project Overview

**axon-showcase** — a CQRS/Event Sourcing reference app using the Axon Framework. Java 21, Spring Boot 3.5.16, Gradle
9.7.1 (Kotlin DSL), monorepo with 19 modules (18 JVM + `showcase-web-ui`).

This repo uses **spec-driven development**: behavior is captured as OpenSpec specs in `openspec/specs/showcase/`
(organized by architectural role: `gateway`, `write-side`, `read-side`, `clients`, `extensions`, `deployment`,
`quality`). Code changes go through the `opsx-*` OpenCode commands / `openspec-*` skills (propose → apply → archive).
Follow these workflows for new work, and treat the captured specs as the behavioral source of truth.

## OpenSpec Workflow Agreement

**Never archive a change automatically after apply.** Stop after implementation, report, and let the user review the
changes made and decide when (or whether) to archive.

**Never push to the remote automatically.** Commit locally when asked, but only `git push` when the user explicitly
requests it (e.g., "push" or "commit and push").

**Create the change's branch at propose.** As soon as a change is proposed, put its artifacts on their own branch (named
after the change). The change dir and all subsequent work live on that branch; rejecting a proposal is a branch delete,
never a `main` cleanup.

**Fork branches from `main` only.** Every new branch — a change branch or a standalone fix — is created from
`origin/main` (fetch first), never from another work branch. Branching from a work branch silently carries its commits
into the new PR (a fix PR ended up shipping a change's commit history); recover by rebasing `--onto origin/main` and
force-pushing, then verify the PR's changed-file set is the intended one.

**Leave implementation uncommitted until the user has reviewed it.** After applying a change, do not commit the
implementation before the user has done their review pass — keep the working-tree diff visible (`git status`/`git diff`)
so they can see exactly which files changed. Commit only after the user approves the implementation (or explicitly asks
to commit); the planning artifacts may be committed separately.

**Auto-review the change before asking for a manual review.** After finishing a change's **proposal** (planning
artifacts) and again after finishing its **implementation**, run a quick review of the work (the `review-quick`
subagent) against the change's planning artifacts — for the proposal, the proposal/design/tasks/spec-delta coherence and
repo fit; for the implementation, the tasks and delta spec — and repeat it until it reports no new observations. Fix
everything the quick review finds, re-run it, and stop only when it comes back clean — only then ask the user for a
manual review pass.

**Interrogate the premise before designing a change that moves, copies, or removes existing configuration.** Establish
_why the current state exists_ and whether it is deliberate before designing _how_ to change it — a change that
relocates configuration already in place can be the best-executed version of the wrong idea. The
`remove-redis-client-label` detour designed a chart-default for the `*-client` pod labels (hardcode them in the
templates) and ran a full propose→apply→verify cycle, including a live `helmInstallToLocal`, before the user's question
("the label name depends on the release name — does this still make sense?") revealed the labels are intentionally
local-target values that belong in `values-local.yaml`. The design weighed implementation alternatives but never
questioned the premise. Verify the current state's rationale against the repo — grep for consumers, read the values and
their comments, check `git log` for the introducing change — and record it in the design's Context; treat "this looks
redundant" as a hypothesis to verify, not a justification to remove.

**Capture lessons after every change's implementation and after every merge into `main`.** Once a change's
implementation quick review is clean — and again after the PR is merged (including docs changes, standalone fixes, and
dependency bumps that never went through the OpenSpec workflow) — run the `lesson-capture` subagent (giving it the diff,
review findings, the change dir when one exists, and a short note on what went wrong or was learned) to propose
AGENTS.md additions — gotchas and conventions worth recording. Apply the proposals the main agent judges durable, then
ship them as a docs PR (per the docs-refresh convention) alongside or after the change. Process mistakes that leave no
diff trace (e.g. a git command that discarded work) are the most valuable thing to capture — this is what makes the
capture systematic instead of memory-dependent. For a docs-only merge that fixes stale facts or removes duplication, the
fix is the lesson — do not re-capture it as a new gotcha; capture only what the merge left unaddressed. Do not skip the
subagent on your own judgment that "there's nothing new" — the merge itself is the trigger, and the subagent is the
arbiter (the archive merge after `remove-redis-client-label` was skipped on exactly such an assumption, and the user had
to push back before the forgotten-archive and premise-interrogation lessons were captured). When the user asks "is there
anything else to capture?", treat it as a prompt to run the subagent again over the events — not as a request to justify
the previous pass. An initial "nothing to capture" verdict is a hypothesis, not a conclusion: the session that produced
it had process mistakes that were themselves the lesson (e.g. the archive was forgotten and the premise-interrogation
gap went uncaptured until the user pushed twice). A docs-fix merge has nothing further to capture only if the subagent
actually reviewed it and said so.

**Sync the main spec only at archive.** Apply edits to code and the change dir's _delta_ spec — never the main spec
under `openspec/specs/`. The main spec is updated exclusively when the change is archived (delta → main), so the source
of truth never describes behavior the code hasn't yet been verified against.

**A delta spec cannot rename a main-spec requirement header.** A `MODIFIED` requirement in a change's delta spec is
matched to the main spec by its `### Requirement:` header, so the header must be verbatim-identical to the one it
modifies — only the description/body can change. Retitling a requirement while rewording it (e.g. renaming "Vendored
agent skills are available to agents" while narrowing it) fails `openspec validate --changes` with "Archive would refuse
this delta: MODIFIED failed for header ... not found" and must be reverted. To genuinely retitle a requirement,
delete-and-add it instead of renaming the MODIFIED header.

**A `MODIFIED` requirement block replaces the whole requirement — the delta must carry every existing scenario the main
spec still has, not just the new ones.** `openspec validate --changes` fails with "MODIFIED ... omits scenario(s) the
current spec still has" when a delta drops an existing scenario (the first `helm-install-builds-webui-image` delta wrote
only the new web UI scenario, omitting "The deployed UI can call the gateway" and "The UI origin is configurable"). The
safe recipe: copy the current spec's full requirement block (description + all scenarios) into the delta, then edit it —
never hand-write a MODIFIED block from memory.

**A spec rename/move (`git mv`) does not update the spec's internal `#` title, and nothing validates the title against
the capability path.** The first line of `openspec/specs/.../spec.md` must be edited separately to match the new path —
`openspec validate` never checks it, so a stale header is silent drift that passes CI. The 2026-08-14 role-group
restructure git-mv'd most spec files but left their `#` headers at the old `showcase/<capability>` paths (e.g.
`# showcase/helm-chart Specification` still under `showcase/deployment/helm-chart/`); the `rest-api` rename fixed one
such leftover, and several still remain. On any spec move or capability rename, fix the first line in the same change —
the title does not follow the file.

**A delta cannot carry a `## Purpose` for an existing capability — refresh the main spec's Purpose in the archive commit
and record it as a task.** `openspec archive` (and the `openspec-sync-specs` workflow) treats the main spec's Purpose as
authoritative and leaves it alone; a delta `## Purpose` only seeds a capability whose spec does not exist yet and is
otherwise ignored. A change that alters a capability's scope — e.g. adding `zstd-jni` to the constrained transitives in
`showcase/quality/dependency-security` — therefore leaves the Purpose stale, and `openspec validate` still passes
because the Purpose is not validated against the change. The repo rule forbids editing the main spec before archive, so
record an explicit task (as `address-new-snyk-findings` did in task 3.2) and apply the Purpose edit in the archive
commit; do not assume the sync workflow covers it.

**Run CI before archiving; one PR per change.** Push the implementation branch and open a PR with the code and the
active change dir. After the `build` check is green and the user approves, archive the change (move the change dir and
sync the main spec) as an additional commit in the _same_ PR, then merge once. The archive — the declaration that a
change is done — always follows CI, never precedes it. (Docs refresh that reflects a completed change —
`AGENTS.md`/`README.md`/`docs/ideas.md` updates and captured lessons — ships as its own separate docs PR; docs that ARE
the change ship with the change's PR, per the docs-refresh convention.)

**Merging PRs: the `--admin` flag is for admin users only.** The `main-require-pr-on-merge` ruleset requires an
approving review (`required_approving_review_count: 1`), but the repo owner (`aanbrn`) is a bypass actor on that ruleset
(`bypass_mode: always`). When the active GitHub user **is** the repo owner/admin, merge directly with
`gh pr merge --squash --delete-branch --admin` once CI is green — do not first attempt a plain merge (it will be
rejected by the ruleset) and do not wait on a Copilot review (it never approves). When the active user is **not** an
admin (e.g. a team member), `--admin` is meaningless and the normal review-required flow applies: request a reviewer and
wait for approval before merging.

## Prerequisites

- Java 21+
- Docker & Docker Compose (for local dev / integration tests)
- Gradle wrapper included (use `./gradlew`)
- Helm 4.x + Kubernetes cluster (for deployment)
- Snyk CLI (for `./gradlew dependencySecurityCheck`)
- actionlint (for `./gradlew workflowLint`, part of `check`; see https://github.com/rhysd/actionlint — brew,
  `go install`, or a release binary)
- `pack` CLI (for the web UI `dockerBuildImage` image build; see
  https://buildpacks.io/docs/for-platform-operators/how-to/integrate-ci/pack/, e.g. `brew install buildpacks/tap/pack`
  on macOS)
- Python 3 (for `./scripts/setup-idea.sh`'s inspection-profile upsert; macOS ships it via Command Line Tools)

## Build & Test

```bash
# Full build (all modules)
./gradlew build

# Build a single service (also produces Docker image via bootBuildImage)
./gradlew :showcase-command-service:bootBuildImage

# Run tests for a single module
./gradlew :showcase-command-service:test

# Run component tests (faster, no containers)
./gradlew :showcase-command-service:componentTest

# Run integration tests (requires Docker, spins up Testcontainers)
./gradlew :showcase-command-service:integrationTest

# Run end-to-end tests (separate opt-in task; boots services + infra via Testcontainers, builds service images)
./gradlew :showcase-api-gateway:e2eTest

# Run web UI end-to-end tests (separate opt-in task; builds service images + boots infra via docker compose,
# serves the built UI via vite preview, drives it with Playwright, then tears the stack down)
./gradlew :showcase-web-ui:e2eTest

# Check runs: compile → spotless/checkstyle/spotbugs/errorprone → test → componentTest → integrationTest,
# plus workflowLint (actionlint) and verifyInfraImageVersions
# (add -PskipITs to drop integration for a Docker-free check; e2e is never part of check)
./gradlew :showcase-command-service:check

# Load tests (Gatling)
./gradlew :load-tests:test

# Dependency security scan (Snyk; requires the Snyk CLI on PATH, not part of check)
./gradlew dependencySecurityCheck
# The scan passes --policy-path=.snyk (the root Snyk policy). Suppressed findings are tracked
# there with a short-term expires (2026-11-28 for the Spring cluster, 2026-12-11 for t-digest) so they
# re-surface if not resolved in time: the Spring Framework 6.2.x / Spring Security 6.5.x cluster is
# fixed only by the deferred Spring Boot 4 migration (ADR-0004), and com.tdunning:t-digest:3.3 (a
# load-tests-only gatling-charts transitive) has no patched release. The checkstyle tool (13.11.0) no
# longer carries any vulnerable transitives, so no tooling findings remain to suppress. See the
# /dependency-security-check command
# for the version-pinned ignore format and the Snyk rate-limit gotcha: the free org allows 200 Open
# Source tests/billing period, counted only for manifests with identified vulnerabilities — so
# the policy-suppressed task consumes no quota (a passing scan works even once the limit is
# exhausted by unfiltered runs), and only raw `snyk test` runs that find issues hit the cap.

# Dependency update report (only catalog-owned coordinates; majors suppressed for groups in
# config/dependency-updates/major-disabled.properties)
./gradlew dependencyUpdates

# Frontend (showcase-web-ui): install, lint, format-check, tests (verification; the production bundle
# is built by `build`/`assemble`, like the JVM modules' bootJar)
./gradlew :showcase-web-ui:check
```

The `/dependency-updates` OpenCode command runs this task and summarizes the available updates; the `/gradle-update`
command updates the Gradle wrapper to the latest stable version when one is available, and the `/opsx-tool-update`
command regenerates the OpenSpec command/skill instruction files after a new `openspec` CLI release.

Build-environment constraints can surface as spurious "current version" rows in the report: build tooling such as
SpotBugs publishes module constraints that `checkBuildEnvironmentConstraints` reads and reports as the current version.
For example, a `log4j-core [2.17.1 -> 2.26.1]` row appears even though `log4j-core` resolves to `2.26.1` everywhere —
`2.17.1` is the floor of an external Log4Shell guard published by `spotbugs-annotations`. These rows are a known
`gradle-versions-plugin` limitation, not real updates (see upstream ben-manes/gradle-versions-plugin#755); do not chase
them (see ADR-0007).

Major-blocking entries in `config/dependency-updates/major-disabled.properties` carry a pointer comment naming the
coordinate and its rationale; the authoritative reasoning for each suppressed coordinate lives in the
`showcase/quality/dependency-management` spec.

The Helm update check has its own suppression file, `config/helm-updates/major-disabled.properties`: major bumps of the
bitnami infra charts (postgres, kafka, opensearch) are suppressed there because a major chart ships a new preconfigured
`image.tag` that diverges from the test-surface `*-image-tag` pins (docker-compose/Testcontainers) — a coordinated
migration, not an automatic update. The observability charts (kps, tempo) carry no `*-image-tag`, so their major bumps
surface as actionable; verify them with a live install + smoke test (pods ready, Prometheus targets up, Grafana
datasources wired), since `verifyInfraImageVersions` covers only the bitnami infra charts.

For calendar-versioned coordinates (leading segment is a 4-digit year, e.g. Spring `YYYY.MINOR.MICRO` such as
`reactor-bom 2025.0.7`), the report treats a change in the `YYYY.TRAIN` pair (the first two version segments) as a major
update — matching Spring's release-train definition where `2025.0` and `2025.1` are distinct trains — while a change
only in the service-release (third) segment within the same train is a minor/patch update. Semver coordinates keep the
leading-integer major comparison.

**Test suite order matters:** `test` → `componentTest` → `integrationTest` → `e2eTest`. `check` runs the first three by
default (`-PskipITs` drops integration for Docker-free runs); `e2eTest` is a separate opt-in task. There are two e2e
suites: the gateway's (`showcase-api-gateway`, builds all four service images) and the web UI's (Playwright against the
compose stack, serving the built UI via `vite preview`).

**Test tiers** — a test's tier is decided by its collaborators (what is real vs. faked), not by how long it takes to
run:

- **Unit** (`src/test/java`, suffix `Tests`): the subject under test is isolated — its collaborators are mocks/fakes, no
  Spring context. Verifies single-class logic (e.g. `KsuidIdentifierFactoryTests`).
- **Component** (`src/componentTest/java`, suffix `CT`): the subject is composed with real, in-process collaborators —
  real serializers, Axon `AggregateTestFixture`/`SagaTestFixture`, or a Spring context with WireMock — but external
  infrastructure is never started. Verifies a component behaves correctly against its real neighbors (e.g.
  `QueryMessageRequestMapperCT`, `ShowcaseAggregateCT`, `ShowcaseQueryClientCT`).
- **Integration** (`src/integrationTest/java`, suffix `IT`): real external infrastructure via Testcontainers
  (PostgreSQL, Kafka, OpenSearch). Verifies services against the real things they talk to.
- **End-to-end** (`src/e2eTest/java`, suffix `E2E`): a real deployed system is booted and exercised against all-real
  collaborators, transport-independent — HTTP for the gateway/query-service, the distributed command bus (JGroups) for
  the command-service. The gateway e2e boots the full four-service pipeline and verifies cross-service propagation over
  the full command → Kafka → projection → query pipeline (e.g. `ShowcaseApiGatewayE2E`). The web UI e2e
  (`showcase-web-ui/e2e`, Playwright) boots the same pipeline via docker compose and drives the browser against it:
  create → appears, start → STARTED, saga auto-start reflected over SSE, live events appended to the timeline, and a
  duplicate title surfacing the gateway validation error.

`disable-axoniq-console-message=true` is set both in integration tests and in each service's main application source
(e.g., `ShowcaseApiApplication.java`).

**DB scripts** — before running the command-service standalone (outside Docker), ensure the PostgreSQL event store is
initialized:

```bash
./db.sh init   # creates user `showcase` and database `showcase-events` if absent
./db.sh reset  # drops the database and recreates it
```

## Continuous Integration

`.github/workflows/ci.yml` runs a single `build` job on every pull request and every push to `main`:

- **Pull requests** run the Docker-free fast gate: `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` plus
  `openspec validate --all` — the coverage gate is disabled because the 0.80 baseline is calibrated on integration-test
  coverage, which PRs skip by design.
- **Pushes to `main`** run the full gate: `./gradlew check` (with integration tests and the coverage gate) plus
  `openspec validate --all`.

The `check` task also runs `workflowLint`, which lints the GitHub Actions workflows with actionlint (installed on the
runner via the official download script; see the Prerequisites).

The job uses `gradle/actions/setup-gradle` to restore the Gradle User Home (dependencies, wrapper, and local build
cache) across runs — it never caches workspace `build/` directories, since stale `jacoco` exec data would corrupt the
coverage gate. The `.github/workflows/ci.yml` and `.github/workflows/e2e.yml` workflows additionally extend
`gradle-home-cache-includes` with `nodejs` (the node-gradle plugin's Node download in `~/.gradle/nodejs`) and add an
`actions/cache` step for the npm package cache (`~/.npm`, keyed on `showcase-web-ui/package-lock.json`), so the web UI
build does not re-download the Node runtime or the dependency tree on every run. The `main-required-checks` branch
ruleset requires the `build` check for every merge into `main`, with no bypass actors.

**A build-file or dependency change costs a one-time full rebuild (~9 min vs ~1 min warm).** `setup-gradle` partitions
its caches by a hash of the build/dependency configuration (log keys like `gradle-home-v2|Linux-X64|build[<hash>]` and
`gradle-build-cache-v2-<hash>`), and Gradle build-cache entries are keyed on task inputs — so changing
`libs.versions.toml` or a `build.gradle.kts` invalidates the compile/test/static-analysis entries for every module. PR
runs are `cache-read-only: true` (they restore from `main` but never write), so a PR cannot re-warm the cache itself and
waits for the next push-to-main run; a docs or tiny PR that branches off the updated `main` and builds before that
re-warm lands pays the full rebuild once (the docs-only #138 raced #137's 10-minute re-warm and took ~9 min instead of
~1). It self-heals as soon as `main` re-warms — nothing to fix.

`.github/workflows/e2e.yml` runs the heavy end-to-end suites (`:showcase-api-gateway:e2eTest`, which builds all four
service images and boots the full pipeline, and `:showcase-web-ui:e2eTest`, which drives the browser against the same
pipeline with Playwright) on a nightly schedule and via `workflow_dispatch`. It installs the `pack` CLI explicitly
(`buildpacks/github-actions/setup-pack`, pinned to the same version as local development — the GitHub runner image does
not guarantee it), and uses `actions/cache@v6` for the npm cache. It is observational — never a merge gate, no secrets,
and it shares the same `gradle/actions/setup-gradle` caching rules as `.github/workflows/ci.yml`.

`.github/workflows/snyk.yml` runs the credentialed dependency security scan (`./gradlew dependencySecurityCheck`, all
sub-projects with the root `.snyk` policy) on a weekly schedule and via `workflow_dispatch`, authenticated with the
`SNYK_TOKEN` secret. It is observational — never a merge gate.

`.github/workflows/dependency-updates.yml` runs the Gradle dependency update report (`./gradlew dependencyUpdates`) on a
weekly schedule and via `workflow_dispatch`, opening or updating the "Dependency updates" issue with only the actionable
sections of `build/dependencyUpdates/report.txt` (stable catalog updates + the Gradle wrapper status) using the
`GITHUB_TOKEN` (`issues: write`). When there are actionable updates it posts a comment mentioning the repository owner
(so they are notified); runs with no updates update the issue silently. It is observational — never a merge gate.

`.github/workflows/helm-updates.yml` runs the Helm update check (`./gradlew helmUpdates`) on a weekly schedule and via
`workflow_dispatch`, opening or updating the "Helm updates" issue with the actionable coordinates from
`build/helm-updates/report.txt` (the Helm CLI and pinned chart versions that have a newer version), using the
`GITHUB_TOKEN` (`issues: write`). When there are updates it posts a comment mentioning the repository owner (so they are
notified); runs with no updates update the issue silently. It is observational — never a merge gate.

`.github/dependabot.yml` keeps the GitHub Actions versions current (weekly `github-actions` updates), so an action whose
major bump targets a newer Node runtime (e.g. the Node 20 → Node 24 migration) surfaces as a reviewable PR instead of a
silent CI deprecation warning. The `opencode` workflow's `anomalyco/opencode/github@latest` and the Snyk workflow's
`snyk/actions/setup@master` are deliberate floating refs that Dependabot does not manage.

## Architecture

CQRS with 4 services + an API gateway:

- **showcase-command-service** — write side, publishes events to Kafka, uses PostgreSQL event store
- **showcase-projection-service** — consumes Kafka events, writes projections to OpenSearch
- **showcase-query-service** — read side, queries OpenSearch
- **showcase-api-gateway** — REST entry point (`/showcases`), routes to command/query services; also exposes the live
  event stream over SSE (`/events`) and applies CORS for the web UI origin
- **showcase-web-ui** — standalone browser UI (React + Vite, Feature-Sliced Design) that browses and drives showcases
  through the gateway and renders the live event timeline; deployed as its own nginx container image (see Docker Images)
  with the API base URL configured via `SHOWCASE_API_BASE_URL`

Key modules (libraries, not services):

- `showcase-command-api` / `showcase-query-api` — API interfaces; their testFixtures are used by clients and services
- `showcase-command-client` / `showcase-query-client` — reactive clients for remote services
- `showcase-query-proto` — Protobuf definitions for query side
- `showcase-projection-model` — shared query model definitions
- `showcase-test` — shared test utilities
- `showcase-identifier-extension` — KSUID identifier support
- `showcase-mapstruct-extension` — MapStruct extensions
- `showcase-resilience4j-extension` — Resilience4j integration
- `platform` — Java platform BOM for dependency version management
- `build-logic` — Gradle convention plugins
- `helm` — Gradle module for Helm releases; contains sub-module `helm:chart`
- `load-tests` — Gatling-based load tests

## Conventions

- **Lombok**: use Lombok where possible (e.g., `@RequiredArgsConstructor`, `@Data`, `@Builder`, `@Value`) instead of
  writing boilerplate manually; `addNullAnnotations = jspecify`; copyable annotations include `@Qualifier` and `@Value`
- **MapStruct**: default component model is `spring` (`-Amapstruct.defaultComponentModel=spring`)
- **ErrorProne**: NullAway on `showcase.*` packages in production code; disabled in `TestJava` tasks
- **Checkstyle**: style gate wired into `check` via `code-check-conventions.gradle.kts`; ruleset at
  `config/checkstyle/checkstyle.xml`, generated sources excluded via `config/checkstyle/suppressions.xml`
- **SpotBugs**: finds bugs with findsecbugs and fbContrib plugins; uses `spotbugs-include.xml` and
  `spotbugs-exclude.xml` filters in `config/spotbugs/` if present (see `code-check-conventions.gradle.kts`)
- **LZ4 relocation**: root build forces `org.lz4:lz4-java` substitution (see `build.gradle.kts`)
- **Build-tool versions live in the version catalog**: a `build-logic` convention plugin never hard-codes a tool version
  — it declares it in `gradle/libs.versions.toml` and reads it via `val libs = the<LibrariesForLibs>()` /
  `libs.versions.<name>.get()` (runtime `node`/`java`, plugin `toolVersion` `checkstyle`/`spotbugs`/`jacoco`, generator
  artifacts, buildpack ids, image tags). A version that is not a `group:name` dependency (a buildpack id, a
  builder/run-image tag, `node`) is a `[versions]`-only entry with no `[libraries]` module. Catalog ownership is
  single-sourcing, not update tracking: bare `[versions]` entries are not resolved as dependencies, so
  `dependencyUpdates` and `helmUpdates` both ignore them — they go stale silently and must be audited by hand.
- **All JavaCompile tasks** add `-parameters` flag
- **Test display names**: every test class and every `@Test`/`@ParameterizedTest` method (plus `@Nested` groups) carries
  a static-sentence `@DisplayName` (e.g., `@DisplayName("Showcase aggregate component tests")`,
  `@DisplayName("Finishing a showcase with a valid command succeeds")`). Do not use `{0}`-style placeholders — named
  `argumentSet("...", ...)` invocations already render their own detail
- **Spring bean mocks in tests**: use `@MockitoBean` (from `org.springframework.test.context.bean.override.mockito`),
  not the deprecated-for-removal `@MockBean` (`org.springframework.boot.test.mock.mockito`), which has been deprecated
  since Spring Boot 3.4
- **Test tier placement**: a test's tier is decided by its collaborators (see Test tiers). Verify the application's bean
  wiring (`@SpringBootApplication` config) at the **integration** tier via a real context boot — do not write component
  tests that mock the app's own collaborators. Component tests compose real in-process collaborators (e.g. a real
  mapper) with only external infrastructure faked
- **Nested test groups for resilience features**: a `@Nested` class that groups Resilience4j scenarios is named
  `<Feature>Behavior` (e.g., `TimeLimiterBehavior`, `RetryBehavior`, `CircuitBreakerBehavior`), both for uniformity and
  to avoid shadowing the library's `CircuitBreaker` type
- **BlockHound jvmArgs**: only suites whose tests call `BlockHound.install()` need
  `-XX:+AllowRedefinitionToAddDeleteMethods` and `-XX:+EnableDynamicAgentLoading` (e.g. the query-client `componentTest`
  and the gateway `e2eTest` suites); leave them off suites that don't (e.g. a `componentTest` with only an
  `ApplicationContextRunner` test)
- **Asserting log output**: use `OutputCaptureExtension` (`CapturedOutput`) when the code under test runs **in the test
  JVM** (e.g. `ShowcaseProjectorIT`'s projector logging, `ShowcaseApiControllerCT`'s gateway fallback logging). It
  cannot capture a separate process's output — to assert a **containerized** service's logs (the code-under-test runs in
  a different JVM), collect them via `withLogConsumer` into a `static StringBuilder` and poll it, as the command-client
  e2e did before the suite was consolidated (see `69f2811`)
- **`@DirtiesContext`**: add it only where a full-context boot leaks global JVM state — JGroups (ports and system
  properties) and JCache (a JVM-global cache manager). Contexts that are safely cacheable don't need it: service slices,
  and `@Nested` classes with distinct `@ActiveProfiles` (which already get separate cached contexts). Keep it on the
  gateway/command-service full-context ITs (and the gateway e2e test, which pulls in JGroups); drop it elsewhere
- **Code coverage**: modules opt in via `code-coverage-conventions`. Coverage is measured per module with
  `jacocoTestReport` (unit + component + integration exec data) and aggregated with the root `jacocoRootReport`. The
  `jacocoTestCoverageVerification` gate is wired into `check` at the baseline in
  `config/jacoco/coverage-baseline.properties` and requires Docker (integration tests). A module can extend the
  generated-class excludes via `coverage.generatedClassExcludes`
- **Architecture Decision Records**: record cross-cutting architecture decisions as numbered ADRs under `docs/adr/`
  (Nygard format — Status/Context/Decision/Consequences). OpenSpec captures behavior and change plans; ADRs capture the
  _why_ behind structural choices. Capture a decision as an ADR when it is made, not after the fact
- **Docs refresh on change**: on every change, verify whether `AGENTS.md` and `README.md` need to be refreshed to
  reflect the new state (commands, config, conventions, gotchas) and update them before reporting the change done; also
  remove the change's idea from `docs/ideas.md` — an idea is removed once implemented (captured by a change) or once
  explored and decided against (the durable lesson is captured in `AGENTS.md`/an ADR instead); only open ideas remain
  (see the file's header). Docs that ARE the change (new agent/command/skill documentation, README rows describing a new
  capability) ship with the change's PR; docs that refresh facts about a completed change ship as a separate docs PR.
- **"OpenCode" is capitalized in prose; lowercase `opencode` is only the CLI command, `.opencode/` paths, the
  `opencode.json`/`opencode.jsonc` config filenames, `.github/workflows/opencode.yml`, and the `anomalyco/opencode` repo
  path.** Keep the distinction when editing docs — the lowercase form names a command or path, not the product; the
  README already follows this.
- **README design intent**: the README is a human-facing showcase and onboarding guide, not a reference dump. Preserve
  its intended shape on every edit: section order (intro → Project Structure → Cool Story → Architecture → Technologies
  → Development Workflow → Getting Started → Development Practices → Deployment and Operations → License/Author);
  "Getting Started" is a step-by-step path (tools → sources → build → run → play); prefer Gradle tasks over raw
  `docker compose`/`helm install` commands (they need env vars the Gradle tasks set automatically); use one CLI for API
  examples (curl) — do not add parallel httpie examples; the development narrative is a prompting exercise (the agent
  implements, the human approves); observability is Kubernetes-deployment-only via Helm (custom Axon Showcase Grafana
  dashboard, not in the local compose stack); slash commands render as a table; a worked scenario shows the interactive
  loop
- **Surface human-visible capabilities in the README on every change**: while working on a change, actively look for
  behavior a person can _see or experience_ — a cool story moment, a watcher's flow, a demo-able feature, an access
  path, a dashboard — and make sure it is mentioned in the README before the change is reported done (the docs-refresh
  and README-design-intent conventions cover _how_ it is presented; this is the _what_ to look for). Prefer
  experience-oriented framing ("watch the saga auto-start it") over plumbing descriptions. If a feature is deliberately
  not surfaced, note the omission rather than leaving it silent. Examples that were nearly missed: how to reach the
  deployed system (the `setup-hosts.sh` hostnames) and the observability access path (the Grafana port-forward).
- **Confirm a diagram's semantic mapping with the user before iterating its geometry.** A diagram is a rendering of a
  fixed mapping — which span starts where and ends where; once the mapping is agreed, alignment is mechanical. The
  README OpenSpec-flow diagram consumed many revision cycles (quick + thorough reviews, multiple layouts) because the
  mapping was adjusted through the review loop instead of confirmed up front. State the intended mapping (each span →
  its end node) in the change's report and get it confirmed before re-rendering; keep review effort proportional to a
  presentational artifact instead of iterating its geometry through the review agents.
- **No comments** in source code (per project convention). The sole exception is the `// SPDX-License-Identifier: MIT`
  header, enforced by Spotless on every Java file and by `eslint-plugin-header` (`@tony.ganchev/eslint-plugin-header` in
  the flat `showcase-web-ui/eslint.config.js`, since the original plugin is unmaintained and does not support ESLint
  9/10) on every `showcase-web-ui` source file (the project is MIT licensed; see the LICENSE file)
- **Javadoc**: classes, methods, and fields carry a Javadoc comment describing their purpose (see
  `ShowcaseApiErrorResolver`, `ShowcaseApiController`); wrap at 120 characters. The `showcase-web-ui` uses JSDoc the
  same way: exported components, hooks, and helpers carry a `/** ... */` comment describing their purpose (e.g.
  `ShowcasesPage`, `contextualTime`, `waitForReadModel`); wrap at 120 characters
- **Frontend (`showcase-web-ui`)**: organized per Feature-Sliced Design (`app`/`pages`/`widgets`/`features`/`entities`/
  `shared`, importing only downward, `@/` alias → `src/`). Server state via TanStack Query, client state via a Redux
  Toolkit slice, forms via React Hook Form + Zod. Format with Prettier (`format:check` gated in `check`); lint with
  ESLint 10 via the flat `showcase-web-ui/eslint.config.js`
- **Avoid redundancy**: don't write redundant code — e.g. redundant `throws` clauses on test methods, explicit type
  arguments that diamond inference or target typing resolve, or repeated boilerplate that Lombok covers. Use the
  simplest construct that compiles and stays readable
- **Formatting**: format Java sources, Gradle Kotlin DSL (`*.gradle.kts`), and build-logic Kotlin
  (`build-logic/src/**/*.kt`) files with `./gradlew spotlessApply` (Spotless: palantir-java-format for Java, ktfmt for
  `.gradle.kts` and build-logic `.kt`, both fixed 120 columns) — the canonical format step, enforced by `spotlessCheck`
  in `check` with no IDE required. After each edit, run `spotlessApply` (via the `codefmt` skill's Spotless path) before
  reporting the change done; the IntelliJ formatter is no longer canonical, and import order is owned by the formatter.
  - The 120-character wrapping convention still applies manually to content the formatter does not touch (YAML, and so
    on); markdown is formatted by the root Spotless `markdown` format (Prettier at `printWidth: 120` — a preference, not
    a hard limit: backtick-dense lines can still exceed 120, the accepted trade-off of automating markdown wrapping).
    Verify with `awk 'length > 120'` over edited files. Formatters cannot reflow string literals (e.g. an error message
    in Kotlin/Gradle), so wrap an over-long string with concatenation (`"part1 " + "part2"`) — the formatter preserves
    it. Write markdown as natural prose and let `spotlessApply` (Prettier) wrap it — do not hand-wrap lines at 120; the
    formatter owns the wrapping and reflows on every run. A bare `$` in prose (outside inline code) is parsed as inline
    math and blocks that reflow — the paragraph silently keeps its original ragged wrapping while `spotlessCheck` still
    passes; escape it as `\$` (which renders as `$`).
  - For assertion lambdas inside `argumentSet(...)` parameterized sources, prefer a block lambda body (`(x) -> { ... }`)
    so the formatter indents the statements normally instead of deep-aligning one long expression. The resulting
    "Statement lambda can be replaced with expression lambda" inspection is suppressed with
    `@SuppressWarnings("CodeBlock2Expr")` on the source method (the correct token — not `StatementLambdaInspection`).
- **IDE inspections (optional)**: the build gates are the canonical verification — after each edit, run
  `./gradlew spotlessApply` and the touched module's quality gates (`compileJava`/`check`); no IDE is required. If the
  IDE is available, you may additionally run its inspections on the touched files (through the Steroid MCP
  `steroid_execute_code` / `runInspectionsDirectly`) and fix warnings, but this is not required and never a gate. Prefer
  assertions like `assertThat(x).isNotNull()` over `Objects.requireNonNull(x)` when guarding nullable values in tests,
  since the IDE recognizes them for dataflow.
- **Vision subagent for screenshot review**: the main agent runs on the cheap `opencode-go/deepseek-v4.1-flash`
  (text-only); a `vision` subagent (`.opencode/agent/vision.md`) is pinned to `opencode-go/deepseek-v4-flash-vision-exp`
  to read screenshots. When a visual review is needed (e.g. styling of the web UI), delegate to the `vision` subagent —
  it inherits the Playwright MCP, captures the screenshot into its own context, reads it, and returns a description,
  while the main session stays on the cheap model. This auto-routes vision work without manual model switching.
- **Diagrammer subagent for ASCII diagrams**: the main agent (cheap `opencode-go/deepseek-v4.1-flash`) is weak at ASCII
  diagram geometry — drawing or fixing a diagram (a README flow diagram, alignment, bracket spans) repeatedly cost extra
  effort and review cycles. A `diagrammer` subagent (`.opencode/agent/diagrammer.md`) is pinned to
  `opencode-go/deepseek-v4-pro` to draw and fix ASCII diagrams. When a diagram needs creating, aligning, or correcting,
  delegate to it via the `/diagram` command: it establishes the semantic mapping (which span ends where) before
  rendering, aligns by character width, and preserves deliberate asymmetry. The main agent stays on the cheap model.
- **Experience-analyzer subagent for retrospectives and improvements**: the `experience-analyzer` subagent
  (`.opencode/agent/experience-analyzer.md`) aggregates recent experience across many changes — above the per-change
  `review-quick`/`lesson-capture` agents. Trigger it with the `/retrospective` OpenCode command (or run it manually):
  the command gathers the digest with `./scripts/experience-analysis.sh [since]` (merged PRs, git log, archived changes,
  AGENTS.md gotchas, docs/ideas.md), then the subagent returns a retrospective (shipped PRs by theme, lessons,
  went-well/went-wrong) and improvement suggestions classified as `system` (→ docs/ideas.md or a proposal) or `process`
  (→ AGENTS.md), which the main agent verifies and applies. Retrospectives land in `docs/retrospectives/<date>.md` as a
  docs change.
- **Thorough-review subagent for deep passes**: the `review-thorough` subagent (`.opencode/agent/review-thorough.md`)
  does a deep review of a change against its proposal, delta specs, design, tasks, and the implementation diff — drift,
  correctness, architecture, and conventions. It is intentionally not auto-scheduled (the expensive pass); invoke it
  with the `/review-thorough` OpenCode command (or ask the main agent to run it manually). Findings come back grouped by
  severity with file/line references; the main agent applies fixes.
- **A subagent is only invocable through a trigger, not its documentation**: documenting an `.opencode/agent/*.md`
  subagent in AGENTS.md does not make it reachable — ship a `.opencode/commands/*.md` command (e.g. the `/retrospective`
  trigger for `experience-analyzer`) alongside the agent definition. The experience-analyzer agent existed as
  documentation first and was only usable once the user pointed out it had no trigger and the command was added.
- **An OpenCode model-pin bump is a multi-file sweep — grep for the old model id, and keep the vision pin out of
  scope.** The cheap flash model is pinned in six places: `.opencode/opencode.json` (`model` and `small_model` — two
  keys), the flash-pinned subagent frontmatter (`.opencode/agent/review-quick.md`, `lesson-capture.md`,
  `experience-analyzer.md`), the `.github/workflows/opencode.yml` `model` input, and the `AGENTS.md` agent gotchas that
  name the model id (docs that ARE the change — update them in the same change). When bumping, grep for the old id
  across `.opencode/`, `.github/workflows/`, `AGENTS.md`, and `README.md`, and exclude the vision agent's `-vision-exp`
  pin: the vision model is a separate experimental line that may not have a counterpart in the new family (the v4.1 bump
  left it on `deepseek-v4-flash-vision-exp`). A naive sweep that flags the vision pin as stale would wrongly "fix" a
  deliberate asymmetry. Note the config `model` key is a default for **new** sessions, not a live override: OpenCode
  persists the last-used model in `~/.local/state/opencode/model.json` (its `recent` list), so a restarted TUI that
  restores a session keeps that session's model and still shows the old one until you switch manually or start a new
  session — a correct config pin does not by itself make the running agent use the new model.
- **Vendored agent skills**: the three `axon4to5-*` skills under `.opencode/skills/` are vendored from the
  `AxonIQ/agent-skills` repository, plugin `axoniq-migration` version 0.2.2 (Apache-2.0), copied verbatim from
  `plugins/axoniq-migration/skills/`. To refresh, re-copy the skill directories from that upstream tree at the desired
  plugin version and update the recorded version here and in the `showcase/quality/agent-skills` spec — a deliberate,
  reviewed change, not silent drift.
- **Tooling-setup skill and command**: `.opencode/skills/setup-agent-tools/` and
  `.opencode/commands/setup-agent-tools.md` (project-local, **not** one of the vendored `axon4to5-*` skills) let a
  contributor ask the agent to wire the per-user MCP servers — the GitHub MCP (core) and, for IntelliJ IDEA users only,
  the Steroid MCP — into their global `~/.config/opencode/opencode.jsonc` via `opencode mcp add <name> -- <command…>`.
  Playwright is project-configured.

## Docker Images

Each boot service builds a Docker image:

- `aanbrn/axon-showcase-command-service:${project.version}`
- `aanbrn/axon-showcase-api-gateway:${project.version}`
- `aanbrn/axon-showcase-query-service:${project.version}`
- `aanbrn/axon-showcase-projection-service:${project.version}`
- `aanbrn/axon-showcase-web-ui:${project.version}` (static nginx serving the built frontend)

Image names are set in each service's `bootBuildImage` task configuration. To build for a non-default platform (e.g.,
ARM64 host), pass `-PimagePlatform=linux/amd64` (or `--imagePlatform=linux/amd64`), which Gradle maps to the
`bootBuildImage`/`dockerBuildImage` task's `imagePlatform` `@Option`.

The web UI image is built differently: `frontend-conventions` registers a generic `dockerBuildImage` task (typed as
`PackBuildImageTask`) that runs the `pack` CLI with the **version-pinned** Paketo NGINX + Procfile buildpacks
(`paketo-buildpacks/nginx@1.2.0`, `paketo-buildpacks/procfile@5.14.0`; the versions are catalog-owned as `paketo-nginx`
and `paketo-procfile`) over `build/dist` (the `pack` CLI is a build prerequisite like Helm/Snyk). The pins are explicit
because the builder is floating (`...builder-jammy-base:latest`) and an unversioned buildpack reference becomes
ambiguous — `pack` fails with "multiple versions … must specify an explicit version" — once the builder bundles two
versions of a buildpack (the intermittent `e2e`/`helmInstallToLocal` failure); neither `dependencyUpdates` nor
`helmUpdates` tracks buildpack/builder versions, so keep the pins current. The image serves the bundle via nginx on
`8080` and exposes nginx `stub_status` metrics on `9090` (`BP_NGINX_STUB_STATUS_PORT`); in the Helm deployment, a gated
`nginx-prometheus-exporter` sidecar (`webUi.metricsExporter`, on by default when observability metrics export and the
web UI ServiceMonitor are enabled) converts stub_status to Prometheus `/metrics` on port `9113`, which the Service
`http-metrics` port and ServiceMonitor scrape. A `PackBuildImageTask` convention defaults the image name to
`${project.name}:${project.version}`, which the web UI module overrides with the deployable
`aanbrn/axon-showcase-web-ui:${project.version}` in `showcase-web-ui/build.gradle.kts`. The UI's API base URL is
configured at runtime via the `SHOWCASE_API_BASE_URL` env var — **no baked default** (the browser needs the
externally-visible gateway URL, which only the deployment knows; compose sets `http://localhost:8080`, the Helm chart
uses `webUi.apiBaseUrl` with an empty default) — which a `start.sh` renders into `/workspace/config.js` at container
start (failing fast if the env var is unset/empty) — no ConfigMap or volume mount. The `dockerBuildImage` run prints two
informational warnings from the toolchain, not defects: "Exporting to docker daemon (building without --publish) and
daemon uses containerd storage" (pack exports to the local daemon's containerd store, losing the fast publish path) and
"deprecated usage of stack" (an upstream Paketo buildpack still declares the deprecated `stacks` key instead of
`targets`). Neither is actionable in the build — ignore them.

## Kubernetes Deployment

```bash
# Deploy to local cluster (must be ordered)
helm install kps prometheus-community/kube-prometheus-stack --version 90.0.0 \
  --namespace monitoring --create-namespace --wait
helm install tempo grafana/tempo --version 1.24.4 --namespace monitoring --create-namespace --wait
helm install axon-showcase-db-events bitnami/postgresql --version 16.7.27 \
  --namespace axon-showcase --create-namespace --wait
helm install axon-showcase-kafka bitnami/kafka --version 31.5.0 \
  --namespace axon-showcase --create-namespace --wait
helm install axon-showcase-os-views bitnami/opensearch --version 2.0.10 \
  --namespace axon-showcase --create-namespace --wait
helm install axon-showcase ./helm/chart --namespace axon-showcase --create-namespace --wait

# Or use Gradle Helm plugin (builds images, then installs all releases to the local target)
./gradlew helmInstallToLocal
```

Per-release install/uninstall tasks follow `helmInstall<Release>To<Target>` / `helmUninstall<Release>From<Target>`, e.g.
`helmInstallKpsToLocal` and `helmUninstallKpsFromLocal` (to install/verify a single chart without building images — the
app-release install task additionally depends on the four `bootBuildImage` tasks and the web UI `dockerBuildImage`).
Uninstalling leaves `createNamespace` namespaces (`monitoring`, `axon-showcase`) behind; remove them with
`kubectl delete namespace` afterwards.

**A chart Deployment addition must extend the app release's `installDependsOn` in `build.gradle.kts`.** The list must
enumerate an image-build task for every image the chart's Deployments reference — `ship-web-ui-as-deployable` added the
web UI Deployment without adding its image build, so `helmInstallToLocal` deployed a web UI pod with an image that was
never built. Note the web UI image is built by `:showcase-web-ui:dockerBuildImage` (a pack-based task, not a
`bootBuildImage`); verify the graph with `./gradlew helmInstallToLocal --dry-run` and confirm every chart Deployment's
image has a build task in it.

**Helm release order**: kps → tempo → db-events/kafka/os-views → axon-showcase. Uninstall in reverse.

**Helm release namespaces**: declared in `build.gradle.kts` — the observability releases (kps, tempo) deploy into the
`monitoring` namespace, and the application and infrastructure releases (db-events, kafka, os-views, axon-showcase)
deploy into a dedicated `axon-showcase` namespace (created on install). The local deployment does not depend on the kube
context's current namespace or a `helm.namespace` gradle property.

**Helm release target kube contexts**: each release target declares the kube context it deploys to in
`build.gradle.kts`. The `local` target resolves its context per-machine from the `helm.local.kubeContext` Gradle
property (set in `~/.gradle/gradle.properties` or via `-P`), falling back to the developer's current kube context when
unset — so macOS (colima) and Linux (kind/minikube) contributors each deploy to their own local cluster without a
hard-coded context name in the repo. A remote target (e.g. a future staging) would declare a shared, fixed context in
the build. Do not hard-code a machine-specific local context name (like `colima`) in the versioned build.

**Chart validation**: the chart is linted as part of packaging (`helmPackageMainChart` → `helmLintMainChart`). Lint runs
strict (warnings are errors) and lints the Bitnami `common` subchart, rendering two extra value sets:

```bash
./gradlew :helm:chart:helmLintMainChartFull :helm:chart:helmLintMainChartMinimal
```

Value files live in `helm/chart/src/test/helm/` (`helm/chart/src/test/helm/helm-lint-full.yaml` enables all optional
features, `helm/chart/src/test/helm/helm-lint-minimal.yaml` disables the default-on ones).

Custom values can be placed in `helm/values/<release-name>/values-local.yaml`.

**The `*-client` pod labels in `helm/values/axon-showcase/values-local.yaml` are a bitnami-netpol × local-target
artifact — keep them in the local values, never in the chart.** The labels (`axon-showcase-kafka-client`,
`axon-showcase-db-events-client`, `axon-showcase-os-views-client`) exist only because the local target sets
`allowExternal: false` on the bitnami infra charts (their netpols admit pods carrying the `<release>-client` label), and
their values derive from the local target's release names (declared in `build.gradle.kts`). Moving them into the
reusable app chart would couple it to (a) the bitnami netpol convention, (b) the local release names, and (c) a
netpol-strictness decision the chart cannot observe — a deployment that leaves the bitnami netpols open would carry
labels nothing consumes. A `*-client` label with no matching infra release is dead weight (the removed
`axon-showcase-redis-client` was a leftover from an earlier design — nothing in the stack or code referenced redis).
This was explored as a chart-default change and reverted; the labels belong co-located with the netpol restrictions that
require them.

The local values expose the API gateway and web UI via ingress at the hostnames `axon-showcase-api` and
`axon-showcase-ui` respectively. To reach them by hostname (instead of a `Host:`-header curl workaround), run
`./setup-hosts.sh setup`, which detects the local cluster's ingress-controller LoadBalancer address generically (against
the current kube context, so it works on colima + Traefik, kind/minikube + ingress-nginx, etc.) and manages the
`/etc/hosts` entries (`./setup-hosts.sh remove` to clean up; re-run `setup` if the address changes on cluster restart).

## Local Development

```bash
# Infrastructure
docker compose up -d

# Run services individually (each on separate port)
./gradlew :showcase-api-gateway:bootRun        # :8080
./gradlew :showcase-command-service:bootRun     # :8081
./gradlew :showcase-query-service:bootRun       # :8083
./gradlew :showcase-projection-service:bootRun  # :8082

# Web UI (Vite dev server, proxies /showcases and /events to :8080)
./gradlew :showcase-web-ui:viteDev              # :5173
```

Docker Compose (`docker-compose.yml`) starts all infrastructure **and** the Java services (using pre-built Docker images
`aanbrn/axon-showcase-*:${PROJECT_VERSION}`). Build the images first (`./gradlew bootBuildImage` for the JVM services,
`./gradlew :showcase-web-ui:dockerBuildImage` for the UI); `PROJECT_VERSION` resolves the image tags and must equal the
version the images were built with — the Gradle compose tasks set it automatically, a raw `docker compose up -d` needs
it set explicitly. To run services from source instead, use `bootRun` as shown above. The compose stack also runs the
deployed web UI at `http://localhost:8084` (its image is the nginx-built
`aanbrn/axon-showcase-web-ui:${PROJECT_VERSION}`); the Vite dev server (`viteDev`) remains the hot-reload alternative
for UI development.

**Ports:** the HTTP ports (`server.port` in each service's `application.yml`) are the API Gateway `8080`, Command
Service `8081`, Query Service `8083`, Projection Service `8082`. In `docker-compose.yml`, the published `8000`–`8003`
mappings are **JVM debug ports** (`BPL_DEBUG_PORT`), not the services' HTTP ports — only the API Gateway publishes its
HTTP port (`8080`); the other services' HTTP ports are reachable only via the Docker network or `bootRun`. The web UI is
published on `8084` (its container nginx port is `8080`; `stub_status` metrics on `9090`).

The `docker-conventions` plugin adds root-level `compose*` Gradle tasks that wrap Docker Compose and set
`PROJECT_VERSION` + image versions automatically (also `composeBuildAndUp`, `composeBuildAndRestart`):
`./gradlew composeUp`, `./gradlew composeDown`. A compose task runs only when it is explicitly requested on the command
line (standalone `./gradlew composeUp` — a leading `:` from the IDE, e.g. `:composeUp`, is tolerated) or when a
scheduled task needs it as a dependency or finalizer — the web UI `e2eTest` boots the stack via `composeBuildAndUp` and
tears it down via `composeDown`. Broad builds that do not schedule a compose task never start/stop containers as a side
effect. `composeBuildAndUp` starts the stack with `docker compose up -d` (no `--wait` — a one-shot `kafka-init`
container would trip `--wait`'s health check), so the web UI e2e suite polls the gateway's health endpoint itself to
avoid racing gateway startup.

**Infra image versions are single-sourced** in `gradle/libs.versions.toml`: `*-image-tag` coordinates
(`postgres-image-tag`, `kafka-image-tag`, `opensearch-image-tag`) for the official Docker Hub images used by
docker-compose and the Testcontainers IT/e2e suites (`postgres`, `apache/kafka`, `opensearchproject/opensearch`;
postgres omits a trailing `.0`), and pinned `bitnami-*` chart versions (`bitnami-postgresql`, `bitnami-kafka`,
`bitnami-opensearch`) for the Helm deployment. Each chart ships its own preconfigured `image.tag`, which the Helm charts
deploy as-is — no `image.tag` override in build logic or values files. To bump an infra component, update its
`*-image-tag` and/or its `bitnami-*` chart version together. The `verifyInfraImageVersions` task (part of `check`)
derives its checks from the actual `helm.releases` container, resolves each pinned chart's preconfigured `image.tag` via
the Helm CLI (`helm show values bitnami/<chart> --version <pinned>`, using the plugin-managed client; the task adds and
updates the bitnami chart repository itself), fails the build if its app version drifts from the `*-image-tag` after
truncating the chart app version to the official tag's numeric segment count (so `17.6` matches a chart app version
`17.6.0` at minor granularity, while `3.9.0` requires an exact chart app version match), rejects any official tag with
fewer than two numeric segments as a floating reference (e.g. `17`, which Docker Hub re-points to the latest 17.x), and
fails if any infra values file (`helm/values/*/values*.yaml`) pins `image.tag` — so the repo cannot reintroduce a
separate override. The task is build-cacheable on its inputs (the pinned coordinates and values files): since a pinned
chart version's preconfigured `image.tag` is immutable, unchanged inputs restore the verification from the Gradle build
cache and skip the Helm resolution entirely. Deriving the checks from the configured releases means renaming an infra
release retargets its check and removing one drops it. External `image.tag` overrides at deploy time (e.g. `--set` in a
release pipeline) are outside this in-repo gate.

**Every Helm chart coordinate in the version catalog is a concrete version** — never a floating major-line pin such as
`77.x.x`. This covers the observability charts (`prometheus-community-stack`, `grafana-tempo`) and the `common` subchart
dependency as well as the `bitnami-*` infra charts, so the Helm deployment is reproducible at a reviewable version. Bump
a chart by updating its concrete coordinate (e.g. `77.14.0` → `77.15.0`), and verify the bump with a live install +
smoke test (`helmInstall<Release>ToLocal`: pods ready, Prometheus targets up, Grafana datasources wired) —
`verifyInfraImageVersions` gates only the bitnami image-tag drift, not that the bumped chart deploys correctly.

**Kafka 3.9.0 Testcontainers note**: Kafka 3.9.0 has a validation bug (KAFKA-18281) that rejects Testcontainers' default
listener config (`0.0.0.0` binds). The `KafkaContainer` usages in the IT/e2e suites override `KAFKA_LISTENERS` to
`PLAINTEXT://:9092,BROKER://:9093,CONTROLLER://:9094` (empty hosts make the listeners implicit) so 3.9.0 starts; keep
that override when bumping the Kafka image tag.

## Key Environment Variables

- `SHOWCASE_CORS_ALLOWED_ORIGINS` — comma-separated browser origins allowed cross-origin access to the gateway. The
  container image defaults to empty (fail-closed — deployments must allow their UI origin explicitly); docker-compose
  sets it to the local dev + preview origins, and the Helm chart exposes it as `apiGateway.cors.allowedOrigins`.
- `DB_PASSWORD=showcase` — PostgreSQL password for command-service
- `BPL_DEBUG_ENABLED=true` / `BPL_DEBUG_PORT=8000-8003` — JVM debug (JDWP) agent ports (`8000`–`8003` are the published
  debug ports in `docker-compose.yml`; see Local Development)
- `THC_PATH=/actuator/health` — health check path
- `JAVA_OPTS=-XX:MaxDirectMemorySize=128m -XX:MaxGCPauseMillis=20` — container JVM tuning

## Gotchas

- **`git stash pop` can leave conflict markers after a rebase.** The "leave implementation uncommitted until reviewed"
  workflow stashes the change on every rebase; if a docs file (e.g. `docs/ideas.md`) advances on `main` between the
  stash and the rebase, popping the stash after the rebase can leave `<<<<<<<` conflict markers in the working tree (the
  stash carries the pre-rebase copy). This surfaced when the ideas-dates fix (PR #64) merged mid-rebase. Resolve by
  restoring the docs file to `origin/main` (the change branch carries no docs changes) rather than resolving the markers
  by hand.
- **`git reset --hard` on a branch with uncommitted work discards tracked-file edits.** A change branch holds the
  implementation uncommitted (per the workflow); a `git reset --hard origin/main` to "rebase" the branch reverts every
  tracked-file modification (`build.gradle.kts`, workflows, docs) while leaving untracked files (the change dir, new
  sources) intact — silently losing the implementation's edits. This bit the actionlint change when rebasing onto a main
  that had advanced. Never `reset --hard` a branch carrying uncommitted work: with no local commits,
  `git reset --soft`/`--mixed` to `origin/main` keeps the working tree; with local commits, `git rebase` (or stash →
  rebase → stash pop, per the stash-pop gotcha above) is the way. Verify `git status` after to confirm the diff
  survived. The same class of mistake occurs outside a rebase: `git checkout -- <file>` (or `git restore <file>`)
  reverts just that file to `HEAD`, discarding its uncommitted edits — a README change was lost this way to a
  `git checkout -- README.md` run inside an unrelated verification step (recovered only from a backup). While a work
  branch holds uncommitted edits, inspect committed content with `git diff`/`git show HEAD:<file>` rather than
  `checkout --`/`restore`/`reset`.
- **The actionlint download script takes positional arguments (`version dir`), not `--dir`, and the target dir must
  already exist.** When installing actionlint in CI with `bash <(curl .../scripts/download-actionlint.bash)`, pass
  `latest "$RUNNER_TEMP/actionlint"` and `mkdir -p` the dir first — a `--dir` flag is rejected as an invalid version
  (the script exits 1 with its usage).
- **`gh pr list` does not support a `--since` flag** — filter merged PRs by window with the search qualifier
  `gh pr list --state merged --search "merged:>=<YYYY-MM-DD>"` (an unknown flag like `--since` is rejected outright, and
  there is no date variant of `--merged`). This bit the `/retrospective` gather script
  (`scripts/experience-analysis.sh`), whose first version used `--since`; the proposal/tasks that described
  `--merged --since <window>` were corrected to the search form during implementation. Any future "what shipped since X"
  automation must use the `--search "merged:>=..."` form.
- **`gh pr create --body` with Markdown can fail under zsh with `no matches found`** — an inline body containing
  `**bold**` (or other shell metacharacters/newlines) is subject to zsh's `nomatch` glob error
  (`zsh: no matches found: **...`). Write the body to a file and use `--body-file <file>` instead; it also sidesteps
  quoting and embedded-newline problems. Prefer `--body-file` for any multi-line PR or issue body.
- **Relative `date` arithmetic is not portable across macOS and Linux**: BSD `date` (macOS) uses `-v-7d`, GNU `date`
  (Linux, incl. CI) rejects it and needs `--date='7 days ago'`. A cross-platform script computing a relative date must
  probe first (`date -v-7d >/dev/null 2>&1 && … || date --date='…'`), as `scripts/experience-analysis.sh` does for its
  7-days-ago default — don't hard-code one platform's form.

- **Deployment investigations must include the Helm chart and values files.** When diagnosing a deployment issue, the
  chart (`helm/chart/src/main/helm/`) and its values (`helm/values/*/values-*.yaml`) are always in scope alongside the
  live cluster — the deployed resources are generated output of the chart, so a wrong live resource usually means a
  wrong source (or a stale apply), not a standalone "cluster problem." The chart also introduces k8s-specific concerns
  (namespaces, NetworkPolicies, KUBE_PING discovery) that do not exist under `bootRun`/`docker-compose`, so a bug can be
  invisible locally and only surface once deployed. Recent deployment bugs were both chart bugs, not code bugs: the
  `kubernetes` EndpointSlice `lookup` namespace in the network policies (`fix-kube-ping-api-egress`) and the missing
  release-namespace declarations (`declare-axon-showcase-namespace`). Prefer rendering the chart locally with
  `helm template` to inspect what the source produces before (or alongside) inspecting live resources.
- **Checking CI status**: don't poll a PR build with an idle `sleep` loop — use `gh run watch <run-id> --exit-status`
  (or `gh pr checks <pr> --watch`), which blocks until the check finishes and exits non-zero on failure. When the run id
  isn't known, fetch it once via the GitHub MCP `pull_request_read` / `get_check_runs` (or `gh run list`), then
  `gh run watch` it — one blocking call, no manual polling. A docs/build change's `build` check typically completes in
  about a minute; check once shortly after pushing, then confirm green before archiving/merging. Idle sleep loops only
  waste time and add no information.
- **A PR whose base advanced after its CI ran fails to merge with "Required status check 'build' is expected" and
  `mergeStateStatus` BEHIND.** When two PRs merge close together (the norm here: a code PR followed by its docs PR), the
  second PR's branch is behind the new `main`; the `main-required-checks` ruleset demands the `build` check on the
  latest base, so the merge is blocked even though the branch's own CI is green. The error message does not say "update
  your branch" — BEHIND is the tell. Fix: `gh pr update-branch` (or `gh pr merge --update-branch`), which re-runs CI
  against the updated base; merge only once that check is green.
- **Exec tasks (`docker`, `pack`, `snyk`) fail in IDEA on macOS**: an IDEA launched from Finder/Dock (or a stale Gradle
  daemon) gives the Gradle daemon a minimal PATH (`/usr/bin:/bin:/usr/sbin:/sbin`) without `/opt/homebrew/bin`, so
  bare-name execs ("command 'docker' not found") fail even though the tools are installed. Root cause: Gradle applies
  the client's environment to the daemon (`System.getenv("PATH")` is then the full shell PATH), but the JVM caches PATH
  for native process spawning at daemon start and ignores later changes — so execs that resolve a bare command name via
  the JVM's cached PATH fail intermittently depending on which client spawned the daemon. The build resolves this by
  resolving the tool to its absolute path from `System.getenv("PATH")` (which is the real shell PATH) and passing that
  in the `commandLine` (`dockerCli()` in `docker-conventions`, `packCli()` in `PackBuildImageTask`, `snykExecutable()`
  in `dependency-security-conventions`), bypassing the JVM's cached PATH entirely. Do not revert to bare command names;
  do not prepend tool dirs to PATH (the daemon JVM won't honor it). Launching IDEA from a terminal still helps avoid
  stale minimal-PATH daemons in the first place.
- IntelliJ's built-in formatter (its `Default` code style) disagrees with the Spotless format (palantir for Java, ktfmt
  for `.gradle.kts`), so the auto-reformat triggers (**Actions on Save → Reformat code / Optimize imports**, **Auto
  Import → Optimize imports on the fly**) only cause drift if the **palantir-java-format**/**ktfmt** plugins are not
  active. The repo's IntelliJ config is **not versioned** — `.idea/` is git-ignored entirely. Run
  `./scripts/setup-idea.sh` (locates the IDE, runs `installPlugins` for both plugins, and writes the project config from
  the committed templates in `config/idea/`) so a fresh clone gets a formatter-matched IDE after one run. The ktfmt
  config uses the plugin's **Custom** style configured to reproduce ktfmt's kotlinlang style at 120 columns with
  unused-import removal, because the plugin's `Kotlinlang` mode hard-codes ktfmt's 100-column default and ignores the
  line-length option (see README → Local Development → IntelliJ IDEA Setup).
- **palantir-java-format does not manage imports**: since 2.47.0 the plugin only takes over **Reformat Code**, and
  `Optimize Imports` is always run by IDEA's native optimizer, governed by `.editorconfig` (the import layout
  `ij_java_imports_layout = $*,|,*` and `ij_java_use_single_class_imports=true` with the two on-demand counts at `999`).
  This is what stops IDEA collapsing to wildcard imports; without it, `spotlessApply` cannot auto-expand a wildcard
  (palantir never touches imports), so a wildcard must be expanded by hand or via Optimize Imports. The build gate is
  Spotless `forbidWildcardImports()` (fails on any `import x.*;`). A change to the code-style scheme requires an IDEA
  restart to take effect.
- E2E tests for `showcase-api-gateway` depend on Docker images of all other services being built (`bootBuildImage`). Run
  those first.
- The `io.github.build-extensions-oss.helm` / `io.github.build-extensions-oss.helm-releases` gradle-helm-plugin tasks
  are not configuration-cache compatible — do not enable `org.gradle.configuration-cache=true` (verify with
  `--configuration-cache` before adding it).
- The gradle-helm-plugin 3.1.2 calls the deprecated `Project.getProperties()` (a `--warning-mode all` deprecation that
  becomes a hard error in Gradle 10). Tracked upstream as build-extensions-oss/gradle-helm-plugin#145; bump the plugin
  when a fix is released.
- `helmInstallToLocal` tags `"*"` select all releases; deployment order defined by `mustInstallAfter`/
  `mustUninstallAfter` in `build.gradle.kts`.
- NullAway is strict on `showcase.*` packages — ensure proper `@Nullable`/`@NonNull` annotations from `jspecify`.
- Jackson 3 artifacts (`tools.jackson.core:*`) are present on the query-service and projection-service runtime
  classpaths transitively via `co.elastic.clients:elasticsearch-java`, constrained by the platform's `jackson3-bom`
  (kept current on minor versions). This is dependency hygiene, not the deferred Jackson 3 backend migration — Jackson 2
  remains the serialization backend in application code (see ADR-0003).
- Spring Data Elasticsearch's `DateFormat.strict_date_optional_time_nanos` maps to a **microsecond** Java pattern
  (`SSSSSS`, not 9 digits) despite its name — see upstream spring-data-elasticsearch#3334. `ShowcaseEntity` uses a
  custom `NANOS_DATE_PATTERN` (`yyyy-MM-dd['T'HH:mm:ss.SSSSSSSSSXXX]`) with `format = {}` instead; do not "simplify" it
  back to the built-in enum. The truncation is invisible on macOS (microsecond clocks) and surfaces only on nanosecond
  clocks (Linux CI).
- Custom Gradle test suites (`componentTest`, `integrationTest`, `e2eTest`) do not inherit the project's
  `implementation`-only dependencies — each suite re-declares what it needs (client component suites duplicate
  axon/opensearch/wiremock/resilience4j deps, and `showcase-query-proto` must be listed explicitly). A suite can be
  referenced in `shouldRunAfter(...)` only when bound as a `val` (e.g.
  `val integrationTest = suites.register<JvmTestSuite>("integrationTest")`).
- `@Nested` test classes are incompatible with Spring Boot slice tests (`@WebFluxTest`/`@WebMvcTest`): nested classes
  load the full application context instead of the slice and fail on infrastructure beans (e.g. the gateway's JGroups
  `DistributedCommandBusProperties`). Keep slice-test classes flat (see `ShowcaseApiControllerCT`).
- Testcontainers 2.0.5 moved `PostgreSQLContainer` from `org.testcontainers.containers` (now a deprecated shim) to the
  non-generic `org.testcontainers.postgresql.PostgreSQLContainer` — use the new import without the `<?>`/`<>` type
  arguments.
- **Programmatic file restructuring can silently drop a whole section — verify every expected heading survives.**
  Reordering the README with a Python boundary script dropped the entire "Getting Started" section: the move used
  `lines[ends['Development Workflow']:]`, which starts at the _next_ section and skips the block sitting between the
  two, and only a follow-up grep for `## Getting Started` caught the loss. After any script-driven move/rewrite of a
  markdown file, grep for each expected heading (or diff the heading list before/after) before reporting done; for a
  docs reorder, prefer the edit tool over a hand-rolled reordering script.
- **Align ASCII/Unicode diagram comments by character width, not byte length.** In the README's project-structure tree,
  `awk`/`length()` counts UTF-8 box-drawing characters (`│`, `├`, `─`) as multiple bytes, so byte columns ≠ visual
  columns and the `#` comments end up misaligned. Measure with a decoded string (`len(line[:idx]) + 1` in Python) and
  align every comment to the longest entry (the tree's target column 42 is set by `showcase-resilience4j-extension/`).
- **A diagram's geometry can encode semantics — do not normalize a deliberate asymmetry as a rendering defect.** The
  README's OpenSpec-flow diagram has two brackets with different right edges on purpose: `human approves` spans
  Propose→Merge, while `delta spec → main spec` ends at Archive (where the delta folds into main, one node before
  Merge). A cleanup pass that aligned the body pipes to the full width flattened that distinction and had to be reverted
  by the user. When a diagram (or any doc) has been hand-edited, treat an asymmetry as intentional until you verify what
  each element is meant to start and end at — ask rather than "fixing" it, and never regenerate over a human edit
  without diffing against it.
- **Verify documented infrastructure/deployment numbers against the config files, not memory.** The README rewrite
  claimed "the API gateway's two replicas" (only `commandService.replicaCount` is 2 in
  `helm/values/axon-showcase/values-local.yaml`; the gateway defaults to 1), "36 panels" (36 is the raw top-level count
  — 5 are empty row separators, 31 are real panels), and "four services and a gateway" (double-counting a table that
  lists four components). The quick review against repo files caught all three. Before writing a replica count,
  panel/section count, or diagram count into a doc, read the source (`helm/values/*/values-*.yaml`, the dashboard JSON,
  the component table) and cite the real number.
- **Javadoc is a claim about the code — verify direction and subject against the member's own docs and a usage site, and
  scope a Javadoc-consistency sweep by the convention, not just the review's findings list.** The
  `fix-javadoc-consistency` change introduced a `@param elasticsearchConverter` reading "OpenSearch results to entities"
  for a converter that maps entities → OpenSearch (the field Javadoc and its `mapObject(ShowcaseEntity…)` call sites say
  so); the implementation quick review caught it. For converters/mappers either direction reads plausibly, so read the
  field's Javadoc and one call site before writing the `@param`/`@return`. The same change also had to add a field
  Javadoc the review never enumerated (`ShowcaseProjector.METER_NAME_PREFIX`, caught by the user): a consistency sweep
  must audit every class/method/field in the touched classes, not only the review's findings list.
- **Doc claims must match their source and their strength — quote verbatim or paraphrase explicitly, and reserve
  "enforced" for a real gate.** The self-learning README section described `AGENTS.md` rules in quotes;
  `/review-thorough` caught a reworded rule rendered as a verbatim quote, an "enforced" that no gate backs, and an
  overstated process claim. When a doc quotes a rule/spec/comment, copy the exact text (or drop the quote marks and
  describe it), and check the mechanism before using words like "enforced", "always", "never", or "every".
- **When documenting agent tooling (MCP servers, skills, subagents), read the artifact's own definition — not the config
  entry or the `docs/ideas.md` note that mentions it.** The README "Tooling MCP Servers" section described `codefmt` as
  running IDE _inspections_ (it runs the formatter), said the Playwright MCP "drives the web-UI e2e" (the test framework
  runs the e2e; the MCP is only the agent's browser), and listed `runInspectionsDirectly` as an MCP tool (it is an
  IntelliJ helper called inside `steroid_execute_code`) — every claim sourced from the config entry or the parked idea
  note rather than the tool itself. A referencing entry summarizes; it does not specify. Read
  `.opencode/skills/*/SKILL.md`, `.opencode/agent/*.md`, and the server's exposed tool list before describing what each
  does, and treat an idea note's prose as a lead, not a spec.
- **A CLI's `--help` is not a capability list — absence of a flag is not evidence the capability is missing.** The
  `setup-agent-tools` design originally asserted `opencode mcp add` was interactive "with no `--command` flag for a
  local server, so it cannot be driven by the agent"; in fact `opencode mcp add <name> -- <command…>` is
  non-interactive, writes the global config, and preserves JSONC comments — the `-- <command>` form is simply not shown
  in `opencode mcp add --help` (which shows only the MCP-server flags `--url`, `--env`, `--header`). A quick review
  caught the false premise. Before designing around a limitation ("this can't be automated"), verify it by trying the
  command or reading its source/docs — do not infer impossibility from a help screen.
- **A change merged without its archive is incomplete — do not merge the implementation PR and defer the archive.** The
  "one PR per change" rule puts the archive commit in the _same_ PR before merge; a change whose PR merged but whose
  change dir was never archived is easy to forget (the `remove-redis-client-label` change was merged and sat unarchived
  until the user pointed it out). When the merge completes, verify the change dir is archived; if archiving post-merge,
  put it on its own branch and PR — never commit it to local `main` and `git push origin main`, which the branch ruleset
  rejects and forces a branch-then-reset dance.
- **A "the only X" claim in `AGENTS.md` goes stale the moment a change adds a second X — fix it in that change.** The
  web UI's Playwright e2e suite was added while `AGENTS.md` still said "the only e2e suite is `showcase-api-gateway`'s",
  and the stale claim survived until a later cleanup pass (PR #116). When a change adds a second instance of anything
  the docs call unique (a second e2e suite, image, or workflow), grep `AGENTS.md` for `only`/`sole`/`never` claims about
  the first and update them; when editing a section, re-verify such claims against the repo instead of trusting the
  prose.
- **The Snyk CLI pin is outside every update-check workflow — check it manually.** `dependencyUpdates` /
  `dependency-updates.yml` cover Gradle catalog coordinates and `helmUpdates` / `helm-updates.yml` cover the Helm CLI
  and pinned charts, but the `snyk-version` input in `.github/workflows/snyk.yml` has no check (Dependabot manages
  action version refs, and `snyk/actions/setup@master` is a floating ref it does not bump), so it goes stale silently.
  Confirm it against `gh api repos/snyk/cli/releases/latest` when bumping or auditing tooling currency. A bump also
  cannot be verified locally: `workflowLint` (actionlint) proves only that the YAML lints, not that the version tag is
  installable — the credentialed weekly run (or a local `dependencySecurityCheck` with `SNYK_TOKEN`) is the first real
  execution.

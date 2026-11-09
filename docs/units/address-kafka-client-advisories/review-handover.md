# Review hand-over — address-kafka-client-advisories

## Round 1 — proposal review

- **Unit**: `address-kafka-client-advisories` (change name and branch).
- **Base revision**: `bfe30a08ce3924cbf1f0d343d135ea196004e819` (`git merge-base HEAD origin/main`).
- **Current revision**: `bfe30a08` — the unit's work is uncommitted and staged in the working tree.
- **Working tree and branch**: `/Users/afanasyev/Development/axon-showcase-address-kafka-client-advisories` on branch
  `address-kafka-client-advisories`.
- **Supplied artifacts**:
  - `openspec/changes/address-kafka-client-advisories/proposal.md`
  - `openspec/changes/address-kafka-client-advisories/design.md` (includes the claims table and the dismissed scan
    candidates)
  - `openspec/changes/address-kafka-client-advisories/tasks.md`
  - `openspec/changes/address-kafka-client-advisories/specs/showcase/quality/dependency-security/spec.md`
  - `docs/units/address-kafka-client-advisories/unit.md`
- **Scope**: the proposal stage — coherence of proposal, design, tasks, and delta spec with one another and with the
  repository, task coverage, and the claims table.
- **Prior findings**: none; this is the unit's first round.
- **Completed checks**:
  - `openspec validate address-kafka-client-advisories --type change` — valid.
  - `openspec validate --all` — 27 items passed, 0 failed.
  - `python3 scripts/claim-scan.py` — candidates resolved into the claims table or the dismissed classes beside it.
  - `./gradlew spotlessApply` — clean.
- **Known limitations**:
  - The credentialed Snyk scan has not run for this change: `SNYK_TOKEN` is unset locally, and the 2026-11-07 run logged
    that the Snyk org reached its monthly private-test limit. The scan is a delivery-time task against the pushed
    branch.
  - The compatibility probe ran in a scratch clone under `$TMPDIR/opencode/kafka-probe`, not in the unit's tree; the
    unit's own build has not been run with the constraint yet (it is task 1.3 onward).
  - The gateway e2e suite was not run; the integration suites of all three Kafka-consuming services were.
- **Subject packs the caller classifies as applicable**: `gradle-build` (the change decides a Gradle dependency
  question) and `jvm-testing` (it interprets and plans test runs).

## Round 2 — proposal review, after the round-1 fixes

- **Unit**: `address-kafka-client-advisories` (change name and branch).
- **Base revision**: `21b100bf` (`git merge-base HEAD origin/main`); the round-1 `stale-base` fix fast-forwarded the
  tree.
- **Current revision**: `21b100bf` — the unit's work is uncommitted and staged.
- **Working tree and branch**: `/Users/afanasyev/Development/axon-showcase-address-kafka-client-advisories` on
  `address-kafka-client-advisories`.
- **Supplied artifacts**: the same five as round 1 (`proposal.md`, `design.md`, `tasks.md`, the delta spec, `unit.md`),
  plus the round-1 finding instances at `docs/units/address-kafka-client-advisories/findings/round-1.md`.
- **Scope**: the proposal stage again, with the round-1 instances supplied below.
- **Prior findings and how each was addressed**:
  - `[missing-task] tasks.md:12-26` — the plan never compiles the `e2eTest` classpath. Added task 2.5
    (`compileIntegrationTestJava compileE2eTestJava --refresh-dependencies`) and named the e2e tier's compile-only
    coverage.
  - `[claim-unreproducible] tasks.md:21-22` — the Docker-free `check` does not run the spec-delta validation. Removed
    that clause; added task 2.4 running `openspec validate address-kafka-client-advisories --type change` and
    `openspec validate --all`.
  - `[claim-missing] design.md:97-116` — added rows for the `axon-kafka → cloudevents-kafka → kafka-clients` arrival
    path and for the 3.9 line ending at 3.9.2.
  - `[stale-base] HEAD` — fast-forwarded to `21b100bf`; the two-dot diff now deletes nothing the unit did not touch.
  - `[formatting] tasks.md:15,18` — the two command tasks now use fenced `bash` blocks with one command per line and no
    multi-space inline spans.
- **Completed checks**: `openspec validate address-kafka-client-advisories --type change` (valid),
  `openspec validate --all` (27 passed), `./gradlew spotlessApply` (clean),
  `python3 scripts/commit-hygiene.py --unique-rounds` and `--session-continuity` (both pass).
- **Known limitations**: unchanged from round 1 — no credentialed Snyk scan yet, the probe ran in a scratch clone, and
  the gateway e2e suite has not been run.
- **Subject packs**: `gradle-build`, `jvm-testing`.

## Round 3 — proposal review, after the round-2 fixes

- **Unit**: `address-kafka-client-advisories` (change name and branch).
- **Base revision**: `21b100bf` (`git merge-base HEAD origin/main`).
- **Current revision**: `21b100bf` — the unit's work is uncommitted and staged.
- **Working tree and branch**: `/Users/afanasyev/Development/axon-showcase-address-kafka-client-advisories` on
  `address-kafka-client-advisories`.
- **Supplied artifacts**: the five of round 1, the round-2 instance file `findings/round-2.md`, and the amended
  `review-quick.md` canonical class list.
- **Scope**: the proposal stage, with the round-2 instances supplied below.
- **Prior findings and how each was addressed**:
  - `[contradiction] tasks.md:85` — the follow-up now points at task 2.6, where the dispatch lives.
  - `[contradiction] design.md:106` — the arrival-path row now reads `axon-kafka:4.12.1` bringing
    `cloudevents-kafka:4.0.1`, which declares `kafka-clients`.
  - `[record-shape] findings/round-1.md:9` — split into one instance per line.
  - The `record-shape` class was added to `.opencode/agent/review-quick.md`'s canonical list (task 4.7), and
    `commit-hygiene --class-vocabulary` accepts the ledger.
- **Completed checks**: `openspec validate address-kafka-client-advisories --type change` and `--all` (27 passed),
  `./gradlew spotlessApply`, `commit-hygiene --unique-rounds --expect …:2`, `--session-continuity`,
  `--class-vocabulary`, `--finding-instances`, and `--unit-layout` — all pass.
- **Known limitations**: no credentialed Snyk scan yet; the probe ran in a scratch clone; the gateway e2e suite has not
  been run.
- **Subject packs**: `gradle-build`, `jvm-testing`.

## Round 4 — proposal review, after the round-3 fix

- **Unit**: `address-kafka-client-advisories` (change name and branch).
- **Base revision**: `21b100bf` (`git merge-base HEAD origin/main`).
- **Current revision**: `21b100bf` — the unit's work is uncommitted and staged.
- **Working tree and branch**: `/Users/afanasyev/Development/axon-showcase-address-kafka-client-advisories` on
  `address-kafka-client-advisories`.
- **Supplied artifacts**: the five of round 1, `findings/round-3.md`, and the amended `proposal.md`.
- **Scope**: the proposal stage, with the round-3 instance supplied below.
- **Prior findings and how each was addressed**:
  - `[impact-omission] proposal.md:11-48` — `## What Changes` now names the `.opencode/agent/review-quick.md` edit (the
    `record-shape` class, with its cause) and `## Impact` carries it; the edit is task 4.7, lands at implementation, and
    was reverted out of the planning tree, so the planning stage no longer writes a live rule file.
- **Completed checks**: `openspec validate address-kafka-client-advisories --type change` and `--all` (27 passed),
  `./gradlew spotlessApply`, `commit-hygiene --unique-rounds --expect …:3`, `--session-continuity`,
  `--finding-instances`, `--unit-layout` — all pass.
- **Known limitations**:
  - `commit-hygiene --class-vocabulary` reports round 2's `record-shape` as absent from the canonical list until task
    4.7 adds it at implementation; `check` is not run during the planning stage.
  - No credentialed Snyk scan yet; the probe ran in a scratch clone; the gateway e2e suite has not been run.
- **Subject packs**: `gradle-build`, `jvm-testing`.

## `kafka-client-override` — technical advisor consultation

- **Authorization**: the owner's instruction "Отревьювь с советником до согласия" (review with the advisor before
  agreeing), given at the proposal-review boundary of `address-kafka-client-advisories`.
- **Owner objective**: clear the two new high-severity findings in `Dependency Security` without breaking the pinned
  stack, and keep the repository's supported-baseline discipline intact.
- **Material constraints**: Spring Boot 4 is deferred by ADR-0004; Spring Kafka 3.3.16 (Boot 3.5.16's managed line)
  compiles against `kafka-clients` 3.8.1; Axon Kafka 4.12.1 targets the 3.x client line; the deployed broker image is
  `apache/kafka:3.9.0`; the `.snyk` policy admits a suppression only for an advisory with no available fix.
- **Question**: is forcing `org.apache.kafka:kafka-clients` 4.4.0 through a `platform` constraint the right direction
  now — versus a `.snyk` suppression with an expiry, versus waiting for the Spring Boot 4 migration — and is the
  proposal's scope proportionate to that decision?
- **Next action considered**: the owner approves the proposal, then the constraint is implemented and verified (three
  services' integration suites against `apache/kafka:3.9.0`, then a dispatched `Dependency Security` run against the
  branch).
- **Change identity and stage**: `address-kafka-client-advisories`, proposal-review boundary (planning artifacts
  written, no implementation edit).
- **Diff base and artifacts**: base `21b100bf47a67746b63884b5238c7d3648e47fbb` (`git merge-base HEAD origin/main`); the
  unit's diff is new files only — `openspec/changes/address-kafka-client-advisories/{proposal,design,tasks}.md`, its
  delta spec, and `docs/units/address-kafka-client-advisories/`.
- **Evidence the advisor should weigh** (supplied as excerpts because the advisor cannot run commands):
  - The 2026-11-07 run `42413633653` logged `Tested 20 projects, 12 contained vulnerable paths.` and two findings at
    `org.apache.kafka:kafka-clients@3.9.2`: `SNYK-JAVA-ORGAPACHEKAFKA-22150240` (CVE-2026-111073) and
    `SNYK-JAVA-ORGAPACHEKAFKA-22150295` (CVE-2026-111115), both high.
  - Snyk's advisory pages state the fixes: "4.2.2, 4.3.1 or higher" and "4.2.2, 4.3.2 or higher"; the affected ranges
    are `[3.7.0,4.2.2) ∪ [4.3.0,4.3.1)` and `[2.4.0,4.2.2) ∪ [4.3.0,4.3.2)`.
  - `spring-boot-dependencies:3.5.16` declares `<kafka.version>3.9.2</kafka.version>` and
    `<spring-kafka.version>3.3.16</spring-kafka.version>`; `spring-kafka:3.3.16`'s pom declares `kafka-clients` 3.8.1.
  - `org/apache/kafka/kafka-clients/maven-metadata.xml` ends at `4.4.0`; the last `3.9.x` entries are `3.9.0`–`3.9.2`.
  - A scratch clone of the base revision with `api(libs.kafka.clients)` in the platform's constraints resolved
    `kafka-clients:4.4.0` ("By constraint"), compiled the build, passed the three services' unit and component suites,
    and passed their integration suites against `apache/kafka:3.9.0` — 36 command-service, 20 projection-service, and 11
    gateway tests, 0 failures; an unchanged-tree control run also passed.
  - `helm search repo bitnami/kafka --versions` reports `32.4.3 / 4.0.0` as the newest chart, so the broker cannot be
    patched through a maintained chart today.
  - `spring-boot-dependencies:4.0.7` declares `<kafka.version>4.1.2</kafka.version>`, which falls short of the `4.4.0`
    condition ADR-0031 records.
  - `docs/adr/0004-*` defers the Spring Boot 4 migration for cross-cutting API changes, not for this dependency alone.
  - `.snyk` currently suppresses Spring Framework 6.2.x findings whose fix requires the deferred Spring Boot 4
    migration, each with a reason and an expiry.
- **Review findings and dispositions**: the quick review converged in four rounds (5, 3, 1, 0 findings); the round-2
  finding that fitted no canonical class added `record-shape` to `review-quick.md`, planned as task 4.7 at the
  implementation stage because the planning stage may not edit a live rule file.
- **Known limitations**: the credentialed Snyk scan has not run (no `SNYK_TOKEN` locally; the 2026-11-07 run logged the
  Snyk org's monthly private-test limit); the gateway e2e suite was not run; the `record-shape` class is absent from the
  canonical list until task 4.7.
- **Subject packs the caller classifies as applicable**: `gradle-build`, `jvm-testing`.
- **Report audience and language**: owner-facing, Russian.
- **Fresh or follow-up**: fresh consultation for this decision.

## Round 5 — proposal review, after the e2e-driven revision

- **Unit**: `address-kafka-client-advisories` (change name and branch).
- **Base revision**: `21b100bf` (`git merge-base HEAD origin/main`).
- **Current revision**: `21b100bf` — the unit's work is uncommitted and staged.
- **Working tree and branch**: `/Users/afanasyev/Development/axon-showcase-address-kafka-client-advisories` on
  `address-kafka-client-advisories`.
- **Supplied artifacts**: `proposal.md`, `design.md`, `tasks.md`, the delta spec, `unit.md`, and `findings/round-4.md`.
- **Scope**: the proposal stage; the artifacts changed after a technical-advisor consultation the owner authorized.
- **Prior findings**: round 4 reported none, so there is no instance to re-check. The changes since then came from the
  consultation: the gateway e2e suite is now run and reported (29 tests, 0 failures), the command-service suites'
  disabled Kafka publisher is recorded as the reason the e2e is required, the broker claim is narrowed to "runs a
  release inside the affected ranges; exposure not assessed", and the Snyk-pending status is explicit in task 2.7.
- **Completed checks**: `openspec validate address-kafka-client-advisories --type change` and `--all`,
  `./gradlew spotlessApply`, `commit-hygiene --unique-rounds --expect …:4`, `--session-continuity`,
  `--finding-instances`, `--unit-layout`, `--review-ready` — all pass.
- **Known limitations**:
  - `commit-hygiene --class-vocabulary` reports round 2's `record-shape` absent from the canonical list until task 4.7
    adds it at implementation.
  - The credentialed Snyk scan has not run; the e2e and integration runs were in a scratch clone at the base revision
    with the constraint applied.
- **Subject packs**: `gradle-build`, `jvm-testing`.

## Round 6 — proposal review, after the round-5 fixes

- **Unit**: `address-kafka-client-advisories` (change name and branch).
- **Base revision**: `21b100bf` (`git merge-base HEAD origin/main`).
- **Current revision**: `21b100bf` — the unit's work is uncommitted and staged.
- **Working tree and branch**: `/Users/afanasyev/Development/axon-showcase-address-kafka-client-advisories` on
  `address-kafka-client-advisories`.
- **Supplied artifacts**: `proposal.md`, `design.md`, `tasks.md`, the delta spec, `unit.md`, and `findings/round-5.md`.
- **Scope**: the proposal stage, with the round-5 instances supplied below.
- **Prior findings and how each was addressed**:
  - `[contradiction] tasks.md:61-63` — task 3.2 now writes the narrowed claim ("runs a release inside both advisories'
    affected ranges … the deployment's exposure is not assessed here"), matching the other sites.
  - `[incomplete-enumeration] proposal.md:7-9` — the `## Why` probe parenthetical now names the gateway e2e suite.
  - `[over-claim] proposal.md:56` — now states the scan as expected, confirmed by task 2.7, and records that the
    clearance is unconfirmed until then.
- **Completed checks**: `openspec validate address-kafka-client-advisories --type change` and `--all`,
  `./gradlew spotlessApply`, `commit-hygiene --unique-rounds --expect …:5`, `--session-continuity`,
  `--finding-instances`, `--unit-layout` — all pass.
- **Known limitations**: `--class-vocabulary` red on round 2's `record-shape` until task 4.7; no credentialed Snyk scan
  yet; the e2e and integration runs were in a scratch clone.
- **Subject packs**: `gradle-build`, `jvm-testing`.

## Round 7 — proposal review, after the base refresh

- **Unit**: `address-kafka-client-advisories` (change name and branch).
- **Base revision**: `2b4c9a00` (`git merge-base HEAD origin/main`); the tree was fast-forwarded from `21b100bf`, whose
  commit touched `README.md`, `.opencode/skills/setup-idea/SKILL.md`, and another unit's records — none of this unit's
  artifacts.
- **Current revision**: `2b4c9a00` — the unit's work is uncommitted and staged.
- **Working tree and branch**: `/Users/afanasyev/Development/axon-showcase-address-kafka-client-advisories` on
  `address-kafka-client-advisories`.
- **Supplied artifacts**: `proposal.md`, `design.md`, `tasks.md`, the delta spec, `unit.md`, and `findings/round-5.md`.
- **Scope**: the proposal stage. The only change since round 6 is the claims table's base-revision anchors, re-derived
  from `21b100bf` to `2b4c9a00`; the re-run commands reproduce the same results (no `kafka-clients` in build files, 16
  platform imports, `kafka-image-tag = "3.9.0"`, four publisher-disabled integration suites).
- **Prior findings**: round 6 reported none; no instance to re-check.
- **Completed checks**: `openspec validate --type change` and `--all`, `./gradlew spotlessApply`,
  `commit-hygiene --unique-rounds --expect …:6`, `--session-continuity`, `--finding-instances`, `--unit-layout`.
- **Known limitations**: `--class-vocabulary` red on round 2's `record-shape` until task 4.7; no credentialed Snyk scan
  yet; the e2e and integration runs were in a scratch clone.
- **Subject packs**: `gradle-build`, `jvm-testing`.

## Round 8 — implementation review

- **Unit**: `address-kafka-client-advisories` (change name and branch).
- **Base revision**: `2b4c9a00` (`git merge-base HEAD origin/main`).
- **Current revision**: `2b4c9a00` — the unit's work is uncommitted and staged.
- **Working tree and branch**: `/Users/afanasyev/Development/axon-showcase-address-kafka-client-advisories` on
  `address-kafka-client-advisories`.
- **Supplied artifacts**: `tasks.md`, the delta spec, and the staged implementation diff —
  - `gradle/libs.versions.toml` (+`kafka-clients = "4.4.0"`, + library entry),
  - `platform/build.gradle.kts` (+`api(libs.kafka.clients)` in `constraints`),
  - `docs/adr/0031-run-kafka-clients-4x-ahead-of-boot-4.md` (new),
  - `docs/ideas.md` (a `## 2026-11-09` broker entry),
  - `.opencode/agent/review-quick.md` (the `record-shape` class),
  - `docs/units/address-kafka-client-advisories/process-defects.md` (two records),
  - plus the unit's records and the change directory.
- **Scope**: the implementation stage — every implemented behavior present in the delta spec, tasks marked done
  correspond to the work, and the diff follows the repository's conventions.
- **Prior findings**: round 7 reported none; no instance to re-check.
- **Completed checks**:
  - `./gradlew compileJava` — succeeds; `dependencyInsight` on the command-service runtime classpath reports
    `org.apache.kafka:kafka-clients:4.4.0` selected "By constraint".
  - `./gradlew :showcase-command-service:test …` (all three services' unit and component suites) — succeeds.
  - `./gradlew :showcase-command-service:integrationTest … :showcase-api-gateway:integrationTest :showcase-api-gateway:e2eTest --rerun`
    — executed on this tree: 36 command-service, 20 projection-service, 11 gateway integration tests and 29 gateway e2e
    tests, 0 failures.
  - `./gradlew compileIntegrationTestJava compileE2eTestJava --refresh-dependencies` — succeeds.
  - `./gradlew :showcase-command-service:check -PskipITs -Pcoverage.gate.enabled=false spotlessCheck` — succeeds.
  - `openspec validate address-kafka-client-advisories --type change` and `openspec validate --all` — valid, 27 passed.
  - `commit-hygiene --class-vocabulary`, `--unique-rounds --expect …:7`, `--session-continuity`, `--finding-instances`,
    `--unit-layout` — pass.
- **Known limitations**:
  - The credentialed Snyk scan has not run (`SNYK_TOKEN` unset locally); task 2.7 dispatches it against the pushed
    branch, and until it is green the advisories' clearance is unconfirmed.
  - Task 3.4 (the capability `## Purpose` refresh) is applied in the archive commit.
- **Subject packs**: `gradle-build`, `jvm-testing`.

## Round 9 — implementation review, after the round-8 fixes

- **Unit**: `address-kafka-client-advisories` (change name and branch).
- **Base revision**: `2b4c9a00` (`git merge-base HEAD origin/main`); current revision `2b4c9a00`, work uncommitted and
  staged.
- **Working tree and branch**: `/Users/afanasyev/Development/axon-showcase-address-kafka-client-advisories` on
  `address-kafka-client-advisories`.
- **Supplied artifacts**: the round-8 set (`tasks.md`, the delta spec, the staged implementation diff, the unit records)
  and `findings/round-8.md`.
- **Scope**: the implementation stage, with the round-8 instances supplied below.
- **Prior findings and how each was addressed**:
  - `[missing-task] tasks.md:79-80` — task 4.3 is ticked; its verification (rounds 6 and 7 both `- none`) holds.
  - `[wording] docs/adr/0031-…md:9-10` — the second `Revisit when:` arm now names a fireable internal trigger (a
    `kafka-clients` version the constraint selects failing the integration or e2e suites) with its checkable signal,
    replacing the arm a Gradle constraint cannot produce.
- **Completed checks**: the implementation checks of round 8 still hold (`compileJava`, the unit/component suites, the
  `--rerun` integration and e2e runs at 36/20/11 and 29, the tier compiles, the Docker-free `check`, `spotlessCheck`,
  `openspec validate`), plus `commit-hygiene --unique-rounds --expect …:8`, `--session-continuity`,
  `--finding-instances`, `--unit-layout`.
- **Known limitations**: the credentialed Snyk scan has not run (task 2.7); the capability `## Purpose` refresh is task
  3.4 in the archive commit.
- **Subject packs**: `gradle-build`, `jvm-testing`.

## `kafka-client-override-follow-up` — technical advisor follow-up

- **Authorization**: the owner's instruction "Уточни у советника" (clarify with the advisor), given at the
  implementation-review boundary of `address-kafka-client-advisories`.
- **Relation to the first consultation**: a follow-up on the same decision (`kafka-client-override`), continuing the
  advisor session the first invocation returned.
- **Owner objective**: ship the two-advisory fix once it is safe to, without an unplanned deep-review pass if one is not
  warranted.
- **Question**: should the unit run the deep review before the owner's manual pass and delivery — the routing tool
  returned `RECOMMENDED` with one signal, `repeated-class` (two rows repeat a prior class) — and is the implementation,
  with the credentialed Snyk scan still pending, sound enough to proceed to the manual pass and delivery?
- **Stage**: implementation review; the implementation is complete, uncommitted, and staged.
- **Artifacts and diff base**: base `2b4c9a00` (`git merge-base HEAD origin/main`); the staged diff is 25 files,
  +850/−4. Material changes: `gradle/libs.versions.toml` (`kafka-clients = "4.4.0"` plus its library entry);
  `platform/build.gradle.kts` (`api(libs.kafka.clients)` in `constraints`);
  `docs/adr/0031-run-kafka-clients-4x-ahead-of-boot-4.md`; `docs/ideas.md` (a `## 2026-11-09` broker entry);
  `.opencode/agent/review-quick.md` (the `record-shape` class);
  `docs/units/address-kafka-client-advisories/process-defects.md`; the change directory and the unit's records.
- **Verification measured on this tree**: `compileJava` succeeds and `dependencyInsight` reports `kafka-clients:4.4.0`
  selected "By constraint"; the three services' unit and component suites pass; the integration and e2e suites were
  re-executed with `--rerun` (36 command-service, 20 projection-service, 11 gateway integration tests; 29 gateway e2e
  tests; 0 failures); `compileIntegrationTestJava compileE2eTestJava --refresh-dependencies` succeeds; the Docker-free
  `check` and `spotlessCheck` pass; `openspec validate --type change` and `--all` pass (27 items).
- **Review state and dispositions**: nine quick-review rounds (5, 3, 1, 0, 3, 0, 0, 2, 0 findings), rounds 6-7 and 9
  clean; `scripts/review-routing.py --unit address-kafka-client-advisories --change address-kafka-client-advisories`
  returned `RECOMMENDED` with `[repeated-class] 2 rows repeat a prior class` (round 5 repeated `contradiction`, round 8
  repeated `missing-task`); `commit-hygiene` checks (`--expect-clean`, `--session-continuity`, `--finding-instances`,
  `--unit-layout`, `--class-vocabulary`, `--review-ready`) pass.
- **Known limitations**: the credentialed Snyk scan has not run (`SNYK_TOKEN` unset locally; the 2026-11-07 run logged
  the Snyk org's monthly private-test limit), and it is task 2.7 against the pushed branch; the capability `## Purpose`
  refresh is task 3.4 in the archive commit; the broker image remains inside the advisories' affected ranges (parked,
  exposure not assessed).
- **Subject packs the caller classifies as applicable**: `gradle-build`, `jvm-testing`.
- **Report audience and language**: owner-facing, Russian.
- **Fresh or follow-up**: follow-up, continuing the `kafka-client-override` consultation session.

## `deep-review-pass-1` — thorough review, initial pass

- **Change identity**: `address-kafka-client-advisories` (branch of the same name), change directory
  `openspec/changes/address-kafka-client-advisories/`.
- **Base and revision**: base `2b4c9a00` (`git merge-base HEAD origin/main`); the work is uncommitted and staged (25
  files, +850/−4).
- **Planning artifacts**: `proposal.md`, `design.md` (with its claims table), `tasks.md`, and the delta spec
  `specs/showcase/quality/dependency-security/spec.md`.
- **What the diff does**:
  - `gradle/libs.versions.toml` — `kafka-clients = "4.4.0"` and the `org.apache.kafka:kafka-clients` library entry.
  - `platform/build.gradle.kts` — `api(libs.kafka.clients)` in the `constraints` block.
  - `docs/adr/0031-run-kafka-clients-4x-ahead-of-boot-4.md` — the decision, alternatives, verification, and a
    `Revisit when:` disjunction.
  - `docs/ideas.md` — a `## 2026-11-09` entry for the broker-side exposure, exposure explicitly unassessed.
  - `.opencode/agent/review-quick.md` — the `record-shape` class added to the canonical list.
  - `docs/units/address-kafka-client-advisories/process-defects.md` — two records.
  - The change directory and the unit's records.
- **Prior findings and their statuses**: nine quick-review rounds (5, 3, 1, 0, 3, 0, 0, 2, 0 findings); every finding
  was fixed and verified in the next round. Dispositions: the missing e2e/`e2eTest`-classpath verification, a false
  `check`-runs-validation claim, two missing claims rows, a stale base, multi-space inline spans, two contradictions (a
  stale task reference and an inverted dependency arrow), a record-shape defect (which added the `record-shape` class),
  an omitted `review-quick.md` edit from the proposal, a withdrawn broker claim left in task 3.2, an omitted e2e run
  from the `## Why` parenthetical, an over-claimed "scan goes green", an unticked task 4.3, and an unfireable
  `Revisit when:` arm — all fixed.
- **Current pass**: pass 1 of the budget, initial review, selected by the routing decision `RECOMMENDED` and the owner's
  "Запускай".
- **Known limitations**: the credentialed Snyk scan has not run (`SNYK_TOKEN` unset locally; the 2026-11-07 run logged
  the Snyk org's monthly private-test limit) and is task 2.7 against a pushed branch; the capability `## Purpose`
  refresh is task 3.4 in the archive commit; the deployed broker image remains inside the advisories' affected ranges,
  parked with the exposure unassessed.
- **Subject packs the caller classifies as applicable**: `gradle-build`, `jvm-testing`.

## `deep-review-pass-2` — thorough review, fix verification

- **Change identity**: `address-kafka-client-advisories`; change directory
  `openspec/changes/address-kafka-client-advisories/`.
- **Base and revision**: base `2b4c9a00`; the work is uncommitted and staged.
- **Pass**: 2 of the default budget, a verification of the three pass-1 findings; the owner approved applying all three
  ("Применяй все три").
- **Pass-1 findings and how each was addressed**:
  - `coherence` — `proposal.md:19` now reads "the condition that supersedes it (Spring Boot 4 / Spring Kafka 4 managing
    `kafka-clients` at `4.4.0` or newer)", matching `design.md:63`; the consultation hand-over's evidence line now reads
    "which falls short of the `4.4.0` condition ADR-0031 records".
  - `missing-task` — task 4.4 is ticked; its verification holds at round 9 (`findings/round-9.md` is `- none`).
  - `over-claim` — `docs/ideas.md` now says "The client-side fix ships as …".
- **Planning artifacts**: `proposal.md`, `design.md`, `tasks.md`, the delta spec, plus ADR-0031, the ideas entry,
  `review-quick.md`, and the unit records.
- **Known limitations**: the credentialed Snyk scan has not run (task 2.7); the capability `## Purpose` refresh is task
  3.4 in the archive commit; the broker image remains inside the affected ranges (parked, unassessed).
- **Subject packs the caller classifies as applicable**: `gradle-build`, `jvm-testing`.

## Round 10 — implementation review, after the base refresh

- **Unit**: `address-kafka-client-advisories` (change name and branch).
- **Base revision**: `0deca079` (`git merge-base HEAD origin/main`); the tree fast-forwarded from `2b4c9a00`, whose
  advance touched `build.gradle.kts`, `build-logic`'s chart tasks, and two subject packs — none of this unit's
  artifacts. `docs/ideas.md` conflicted (the base added a `## 2026-11-09` helm entry in the same section), and both
  entries are kept.
- **Current revision**: `0deca079` — the unit's work is uncommitted and staged.
- **Working tree and branch**: `/Users/afanasyev/Development/axon-showcase-address-kafka-client-advisories` on
  `address-kafka-client-advisories`.
- **Supplied artifacts**: `proposal.md`, `design.md`, `tasks.md`, the delta spec, `unit.md`, `findings/round-9.md`, and
  the staged implementation diff.
- **Scope**: the implementation stage; the change since round 9 is the base refresh, the hand-resolved `docs/ideas.md`
  merge, and the claims table's re-anchored base SHAs (`2b4c9a00` → `0deca079`).
- **Prior findings**: round 9 reported none; no instance to re-check.
- **Completed checks**: after the refresh —
  `./gradlew :showcase-command-service:check -PskipITs -Pcoverage.gate.enabled=false spotlessCheck compileJava`
  (succeeds), `openspec validate --all` (27 passed), the four re-anchored claim commands reproduce (no `kafka-clients`
  in build files, 16 platform imports, four publisher-disabled suites, `kafka-image-tag = "3.9.0"`), and
  `commit-hygiene --unique-rounds --expect …:9`, `--session-continuity`, `--finding-instances`, `--unit-layout`.
- **Known limitations**: the credentialed Snyk scan has not run (task 2.7); the capability `## Purpose` refresh is task
  3.4 in the archive commit; the broker image remains inside the affected ranges (parked, unassessed).
- **Subject packs**: `gradle-build`, `jvm-testing`.

## Round 11 — implementation review, after the round-10 fix

- **Unit**: `address-kafka-client-advisories`; base `0deca079`; working tree
  `/Users/afanasyev/Development/axon-showcase-address-kafka-client-advisories`, work uncommitted and staged.
- **Supplied artifacts**: `proposal.md`, `design.md`, `tasks.md`, the delta spec, `unit.md`, `findings/round-10.md`, and
  the staged implementation diff.
- **Scope**: the implementation stage, with the round-10 instance supplied below.
- **Prior finding and how it was addressed**: `[single-sourcing]` — the three restatements of ADR-0031's `Revisit when:`
  gate now point at the ADR's gate instead (the proposal and design name it, and task 3.1 describes writing the gate
  without restating its clauses).
- **Completed checks**: after the round-10 fixes, `openspec validate --type change`, `spotlessApply`, the re-anchored
  claim commands, and `commit-hygiene --unique-rounds --expect …:10`, `--session-continuity`, `--finding-instances`,
  `--unit-layout`.
- **Known limitations**: the credentialed Snyk scan has not run (task 2.7); the capability `## Purpose` refresh is task
  3.4 in the archive commit; the broker image remains inside the affected ranges (parked, unassessed).
- **Subject packs**: `gradle-build`, `jvm-testing`.

## Round 12 — implementation review, after a wording correction

- **Unit**: `address-kafka-client-advisories`; base `0deca079`; working tree
  `/Users/afanasyev/Development/axon-showcase-address-kafka-client-advisories`, work uncommitted and staged.
- **Supplied artifacts**: `proposal.md`, `design.md`, `tasks.md`, the delta spec, `unit.md`, `findings/round-11.md`, and
  the staged implementation diff.
- **Scope**: the implementation stage. The change since round 11 is one wording correction the round-11 reviewer set
  aside as a style nit: `design.md`'s ADR-0031 paragraph read "The ADR states the verification and the condition that
  supersedes it, stated in the ADR's `Revisit when:` gate"; it now reads "…and owns the supersession condition in its
  `Revisit when:` gate."
- **Prior findings**: round 11 reported none; the correction was made after it, so this round covers the edit.
- **Completed checks**: `openspec validate --type change`, `./gradlew spotlessApply`, and
  `commit-hygiene --unique-rounds --expect …:11`, `--session-continuity`, `--finding-instances`, `--unit-layout`.
- **Known limitations**: the credentialed Snyk scan has not run (task 2.7); the capability `## Purpose` refresh is task
  3.4 in the archive commit; the broker image remains inside the affected ranges (parked, unassessed).
- **Subject packs**: `gradle-build`, `jvm-testing`.

## `kafka-client-override-reassessment` — technical advisor, second follow-up

- **Authorization**: the owner's instruction "Отревьювь с советником до согласия" (review with the advisor before
  agreeing), given at the implementation-review boundary of `address-kafka-client-advisories`.
- **Relation**: a follow-up on the same decision (`kafka-client-override`), continuing the advisor session.
- **Owner objective**: decide whether the implementation is ready for the owner's manual approval and delivery, without
  spending a disproportionate review effort on a tree whose implementation has not changed.
- **Question**: the routing tool returns `RECOMMENDED` again on the refreshed tree (base `0deca079`), with the same
  signal as before (`[repeated-class] 2 rows repeat a prior class`). Should the unit run a second deep review on that
  tree, or does the completed two-pass deep review plus the quick loop cover it — and is the implementation ready for
  the owner's manual approval, with the credentialed Snyk scan still pending?
- **Stage**: implementation review; the implementation is complete, uncommitted, and staged.
- **Base, revision and artifacts**: base `0deca079` (`git merge-base HEAD origin/main`); the staged diff is 26 files.
  `openspec/changes/address-kafka-client-advisories/{proposal,design,tasks}.md` and the delta spec; the implementation —
  `gradle/libs.versions.toml` (`kafka-clients = "4.4.0"`), `platform/build.gradle.kts` (`api(libs.kafka.clients)` in
  `constraints`), `docs/adr/0031-run-kafka-clients-4x-ahead-of-boot-4.md`, `docs/ideas.md` (a `## 2026-11-09` broker
  entry), `.opencode/agent/review-quick.md` (`record-shape`), and the unit's records.
- **Deep review outcome**: pass 1 (full) reported three findings — `coherence` (the supersession condition restated as
  "Spring Boot 4 / Spring Kafka 4", while ADR-0031 states the pinned Spring Boot line managing `kafka-clients` at
  `4.4.0` or newer), `missing-task` (task 4.4 unticked), `over-claim` (`docs/ideas.md` said "shipped") — all fixed at
  the owner's direction; pass 2 (targeted verification) returned `nothing remains` with all three `FIXED_AND_VERIFIED`.
  The default two-pass budget is spent and no finding remains open.
- **Deltas since pass 2**: a base fast-forward to `0deca079` (`build.gradle.kts`, `build-logic`'s chart tasks, two
  subject packs — none of this change's files); a hand-resolved `docs/ideas.md` merge that kept both same-day entries;
  the claims table's re-anchored base SHAs; one wording fix (a redundant clause) and three restatements of ADR-0031's
  gate replaced with pointers.
- **Verification after the refresh**: `compileJava`,
  `./gradlew :showcase-command-service:check -PskipITs -Pcoverage.gate.enabled=false spotlessCheck`, and
  `openspec validate --all` (27 passed) pass; the integration and e2e suites were executed on this tree before the
  refresh (36 command-service, 20 projection-service, 11 gateway integration tests; 29 gateway e2e tests; 0 failures),
  and the base's advance touched no service source.
- **Review state**: twelve quick-review rounds (5, 3, 1, 0, 3, 0, 0, 2, 0, 1, 0, 0 findings); rounds 9, 11, and 12 are
  clean; `commit-hygiene --expect-clean`, `--session-continuity`, `--finding-instances`, `--unit-layout`,
  `--class-vocabulary`, and `--review-ready` pass.
- **Known limitations**: the credentialed Snyk scan has not run (`SNYK_TOKEN` unset locally; the 2026-11-07 run logged
  the Snyk org's monthly private-test limit) and is task 2.7 against a pushed branch; the capability `## Purpose`
  refresh is task 3.4 in the archive commit; the broker image remains inside the advisories' affected ranges (parked,
  unassessed).
- **Subject packs the caller classifies as applicable**: `gradle-build`, `jvm-testing`.
- **Report audience and language**: owner-facing, Russian.
- **Fresh or follow-up**: follow-up, continuing the `kafka-client-override` consultation session.

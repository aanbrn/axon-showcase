# Design

## Context

`AGENTS.md` is the only guidance file a session loads automatically, and it loads in full into the main agent and every
subagent. The owner's objective is a substantial reduction of that always-loaded surface at a moderate implementation
and verification cost. The subject-pack mechanism shipped in `separate-agents-md-core-and-subject-packs` (merged as
`6518d081`, measured −32,188 bytes of root) is the lever this change uses: a subject pack moves text out of the
always-loaded root and pays it back only when a task enters the subject, so the saving equals **what a task can skip**,
never the bytes moved.

**The premise, stated once.** A deployment, image-build, or local-stack instruction is needed when a task chooses,
changes, reviews, executes, or diagnoses one of those things. A change whose decisions sit outside the subject —
production code, tests, the build, an editorial or documentation task, most workflow edits — pays ~34.6 KB for it today
and needs none of it. Two adjacent classes do enter the subject: a workflow edit touching the deployment smoke's
`daemon.json` step, and a build-file edit adding a chart Deployment's `installDependsOn` entry. Both are named in the
matrix below, so the premise is bounded by the matrix rather than asserted as "every other change". The owner asked for
the largest reduction at moderate cost, so this design inventories the whole candidate cluster, states what each unit's
governed decision is, and recommends one change rather than a series of small conversions.

### Reproducible baseline

| Fact             | Command                                                                         | Result                                     |
| ---------------- | ------------------------------------------------------------------------------- | ------------------------------------------ |
| Revision         | `git rev-parse HEAD`                                                            | `e42213a84ee54b9d0609b356f1309e6c03cfe88f` |
| Preceding change | `git log -1 --format=%s`                                                        | `add-technical-advisor` (#655, merged)     |
| Working tree     | `git status --porcelain --untracked-files=all`                                  | empty before the branch was created        |
| Branch           | `git switch -c extract-deployment-operations-guidance --no-track origin/main`   | created from freshly fetched `origin/main` |
| `AGENTS.md`      | `wc -c -l AGENTS.md`                                                            | `293123` bytes / `3036` lines              |
| Pack sizes       | `wc -c -l .opencode/skills/{jvm-development,jvm-testing,gradle-build}/SKILL.md` | `10529` / `12822` / `20278` bytes          |

Size by top-level section, measured from each `##` heading through the line before the next one (the 13-byte remainder
is the title line and its blank line; the rows sum to 293,110 bytes):

| Section                          |   Bytes | Lines |
| -------------------------------- | ------: | ----: |
| `## Gotchas`                     | 110,361 | 1,117 |
| `## OpenSpec Workflow Agreement` |  70,449 |   743 |
| `## Conventions`                 |  56,646 |   511 |
| `## Continuous Integration`      |  12,772 |   145 |
| `## Kubernetes Deployment`       |  10,671 |   124 |
| `## Local Development`           |   7,327 |    84 |
| `## Docker Images`               |   6,742 |    69 |
| `## Subject guidance packs`      |   4,443 |    40 |
| `## Structured Writing Standard` |   4,147 |    56 |
| `## Build & Test`                |   3,826 |    66 |
| `## Architecture`                |   1,776 |    28 |
| `## Project Overview`            |   1,214 |    15 |
| `## Key Environment Variables`   |   1,185 |    15 |
| `## Prerequisites`               |   1,150 |    14 |
| `## Line Length`                 |     401 |     7 |

**The candidate cluster, measured by line range** (`##` heading through the line before the next heading; the two
`## Build & Test` units by their own line ranges):

| Unit                                          | Spans (1-based, inclusive) |      Bytes |
| --------------------------------------------- | -------------------------- | ---------: |
| `## Docker Images`                            | 1628–1696                  |      6,742 |
| `## Kubernetes Deployment`                    | 1697–1820                  |     10,671 |
| `## Local Development`                        | 1821–1904                  |      7,327 |
| `## Key Environment Variables`                | 1905–1919                  |      1,185 |
| `### Deployment, Helm & Kubernetes` (gotchas) | 2281–2354                  |      7,690 |
| Helm update-check bullet in `## Build & Test` | 925–930                    |        672 |
| DB-scripts block in `## Build & Test`         | 936–942                    |        281 |
| **Total**                                     |                            | **34,568** |

The cluster is one subject, not one section: it is spread across four top-level sections and one Gotchas subsection.

### Prior decisions recovered

Read from `docs/ideas.md`, the archived reduction changes, the current `agent-skills` requirements, and the newest audit
(`docs/audits/2026-10-19.md`).

| Prior unit / record                                                                    | What it settled                                                                                                                                                                                                                                                                                              | How this design answers it                                                                                                                                                                                                                                                             |
| -------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 2026-10-19 agent-tooling audit, instruction-budget ledger                              | Lists "Helm install commands and chart-value reference", "Buildpack/builder pin mechanics", and "Environment-variable reference" as **safe to load on demand**, each with a trigger and a pointer; reports **no verified extraction candidate** because no destination carried every transferred identifier. | This change supplies the missing owned destination and verifies the transfer, which is the precondition the audit named. Its stale size figures (363,767 B / 3,681 lines) are not reused; the baseline above is measured at `e42213a8`.                                                |
| The same audit's "no opportunity arises for `## Gotchas`"                              | Read the subsection as a unit: evidence-anchored incident memory whose subjects are cross-cutting.                                                                                                                                                                                                           | This design reads each bullet's governed decision, as `separate-agents-md-core-and-subject-packs` did: the four deployment bullets govern a deployment or image-build decision, and no general evidence, git, workflow, or documentation rule sits in them (checked bullet by bullet). |
| `separate-agents-md-core-and-subject-packs` (2026-10-21, merged)                       | Established the subject-pack class, the root classification table, the `Packs:` line, the `Source:` pairing, and the capture/audit routing; measured −32,188 bytes of root.                                                                                                                                  | Reused unchanged: no policy amendment, no new destination class, no new routing mechanism. This change adds one row to the existing table and one destination under the existing class.                                                                                                |
| `add-extraction-procedure` (2026-10-19, merged)                                        | The ordered extraction move: verify the retrieval path, check the build for a gate that reads the block, create the destination, land the content, replace with a pointer plus every imperative, retarget cross-references, re-run the retrieval path, re-measure.                                           | The task list is that procedure, plus the gate retarget this move needs.                                                                                                                                                                                                               |
| `reduce-agents-md-loaded-surface` (2026-10-20, merged)                                 | Its Non-Goals excluded "the gate-coupled deployment blocks" from that unit's scope.                                                                                                                                                                                                                          | That was that change's boundary, not a finding against this one; this change is exactly the unit that pays the gate coupling's price.                                                                                                                                                  |
| `extract-load-test-commands`, `extract-ci-narrative` (2026-10-19, merged)              | Load-test content belongs to the load-tests spec and script; the CI narrative's gate-placement rule stays at root.                                                                                                                                                                                           | Both are **out of scope**; `## Continuous Integration` and the load-test block are untouched.                                                                                                                                                                                          |
| `separate-agents-md-operational-from-evidence` (2026-10-20, merged)                    | Evidence moves to a scoped `docs/guidance-evidence/` record with a per-rule `evidence:` token.                                                                                                                                                                                                               | No moved unit carries an `evidence:` token (grep over the cluster), so no record pairing breaks; the moved `captured:` markers travel with their rules and the marker scan already covers project skills.                                                                              |
| `hold-back-nginx-buildpack`, `address-2026-09-21-update-checks`, the buildpack gotchas | The buildpack/builder pairing rule, the run-the-image verification, and the upstream `paketo-buildpacks/nginx#1340` reference are incident memory attached to the image-build subject.                                                                                                                       | They move verbatim with the subject; the upstream reference stays inside the moved text, which `upstreamReferences` already scans as a project-authored skill.                                                                                                                         |

### Parked ideas reconciled

`docs/ideas.md` was read at `e42213a8`. Only the entries below bear on this decision; the rest were read and are
unrelated. **No parked entry is implemented, removed, or reworded by this change.**

| Parked idea (2026-10-19/20)                                                    | Relationship                                  | Disposition                                                                                                                                                                          |
| ------------------------------------------------------------------------------ | --------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Carry the retrieval-path result in the audit ledger's "safe to load on demand" | Constrains how a destination claim is trusted | **Constraint applied**: every load below carries an evidence class, and a missed probe is reported rather than smoothed over. The ledger mechanism itself stays parked.              |
| Extend the extraction conditions so the retained pointer identifies each unit  | Constrains the source↔destination association | **Constraint applied** through the pack's `Source:` line naming the replaced headings, as that change already uses for the three existing packs. The general condition stays parked. |
| Convert the remaining Git-tranche Gotchas rules                                | Same file, different subject                  | **Not bundled** — the six measured candidates total −3,722 B of root, a different unit.                                                                                              |
| Trend the audit counts across reports                                          | Measures whether the reduction holds          | **Not adopted**; this design reports one measurement and does not build a series.                                                                                                    |

## Goals / Non-Goals

**Goals:**

- Move every obligation whose governed decision belongs to deployment, image builds, the local stack, or
  environment/port wiring into the destination that owns its decision, preserving every imperative, warning, exception,
  identifier, gate condition, and `captured:` marker.
- Keep at root what a task needs before it can classify itself: the classification table and its row, the trigger, the
  missing-pack stop, and the pointer at each vacated section.
- Retarget the two build gates that read the moved text, and prove each retarget with a committed control and a real
  gate failure.
- Keep the capture, audit, and formatting mechanisms working without widening them: verify the coverage they already
  declare rather than re-extending it.

**Non-Goals:**

- A size cap, a percentage target, or a truncation mechanism.
- Moving or touching `## Continuous Integration`, the load-test block, the frontend/npm content, the CI-deployment-smoke
  narration, or any workflow, git, evidence, documentation, or safety rule.
- Consolidating, shortening, or rewriting the moved rules; this is a relocation whose only edits are the documented
  cross-reference retargets and ownership statements listed below.
- A per-section budget check, a pack-load-frequency study, or the parked Git-tranche conversion.
- Any change to a chart, image, workflow, application code, or runtime behavior; `check` keeps its members.
- Implementing anything in this planning unit.

## Method and evidence classes

| Class    | Meaning                                                                                        |
| -------- | ---------------------------------------------------------------------------------------------- |
| Measured | A command over the repository produced the figure now stated.                                  |
| Verified | The mechanism or artifact was read (source, build script, spec, definition) and states it.     |
| Observed | A synthetic probe exercised the behavior; it is evidence for that harness, not for production. |
| Reasoned | Derived from the above by arithmetic or an argument whose assumptions are stated.              |
| Proposed | A design choice or a test to run during implementation; not yet evidence.                      |

## Obligation inventory

Every unit takes one disposition: **move** to its destination (the new pack, or `jvm-testing` for the Kafka note),
**retain** at root with its reason, or **route** to an existing mechanism. "Bytes" are the unit's measured share; the
grouped spans above bound each section's total.

### Moved to `deployment-operations`

| Unit (source anchor)                                                                      | Governed decision                                                                                           | Earliest point needed                           | Preservation risk                                                    |
| ----------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------- | ----------------------------------------------- | -------------------------------------------------------------------- |
| `## Docker Images` image list, registry defaults, release publishing                      | Which image a service produces, which registry a target pulls from, how a release publishes (ADR-0018/0019) | Before building, publishing, or wiring an image | Low; a wrong name or registry fails at pull                          |
| `## Docker Images` `-PimagePlatform` override                                             | How to build for a non-default platform                                                                     | Before a cross-platform build                   | Low                                                                  |
| `## Docker Images` web UI pack build, pins, bump-together rule, run image                 | Which buildpack/builder pins a pack build uses and why they move together                                   | Before bumping or diagnosing a buildpack        | High; a mismatched pin pair builds and then exits 127                |
| `## Docker Images` nginx `8080`/`stub_status`/exporter chain                              | What serves the UI and what scrapes it                                                                      | Before changing the UI image or its Service     | Medium; the metrics chain silently empties                           |
| `## Docker Images` Docker 29 store / `overlay2` workaround                                | Whether a store failure is the daemon's setup or the change                                                 | Before diagnosing a `dockerBuildImage` failure  | Medium; a false pin change follows a host defect                     |
| `## Docker Images` `SHOWCASE_API_BASE_URL` contract                                       | What the UI image requires and where it fails fast                                                          | Before deploying or wiring the UI               | Medium; a missing value crash-loops the pod                          |
| `## Docker Images` toolchain warnings and CNB timestamps                                  | Which build output is noise                                                                                 | While reading a build log                       | Low; misreading costs an investigation                               |
| `## Kubernetes Deployment` manual install block                                           | How a person installs the stack by hand; the block the catalog gate reads                                   | Before a manual or Gradle-driven install        | High; a stale version ships a wrong chart                            |
| `## Kubernetes Deployment` per-release tasks, order, namespaces, contexts                 | Which release installs where, in what order, against which context                                          | Before any install or uninstall                 | High; a wrong order or namespace breaks discovery                    |
| `## Kubernetes Deployment` re-apply-wipes-data rule                                       | Why an infra values change needs the app release upgraded                                                   | Before re-applying infra values                 | High; the gateway then answers 503                                   |
| `## Kubernetes Deployment` `installDependsOn` rule                                        | Why a chart Deployment needs its image build in the app release's dependency list                           | Before adding a chart Deployment                | High; an unbuilt image reaches the cluster                           |
| `## Kubernetes Deployment` chart validation, `common` dependency, lint sets               | Whether `check` catches a chart that cannot bundle its dependency                                           | Before editing the chart or its build           | Medium; the failure moves to the nightly smoke                       |
| `## Kubernetes Deployment` value files and `webUi.apiBaseUrl`                             | Which values file a target gets and what it must set                                                        | Before adding a target or a value               | High; a missing base URL times out the release                       |
| `## Kubernetes Deployment` `*-client` pod labels                                          | Where a netpol-companion label belongs                                                                      | Before editing the chart or a values file       | Medium; a chart-coupled label couples the chart to release names     |
| `## Kubernetes Deployment` ingress hostnames and `setup-hosts.sh`                         | How to reach the deployed system by hostname                                                                | Before deploying or verifying a host            | Medium; a `Host:`-header curl is the workaround                      |
| `## Kubernetes Deployment` SSE keep-alive vs ingress read timeout                         | What keeps a quiet SSE stream alive behind the ingress                                                      | Before changing either interval or annotation   | Medium; a stream dies after a quiet period                           |
| `## Local Development` compose/bootRun/vite commands and stack semantics                  | How to run the stack or one service locally                                                                 | Before running anything locally                 | Low; the command surface is also in the build                        |
| `## Local Development` ports and debug-port mapping                                       | Which port serves what, and which published ports are JDWP                                                  | Before calling a local service                  | Medium; a debug port read as an HTTP port wastes a diagnosis         |
| `## Local Development` `compose*` task semantics                                          | When a compose task runs and what it starts/tears down                                                      | Before running or adding a compose task         | Medium; a broad build must never start containers                    |
| `## Local Development` infra versions single-sourced, three gates' semantics              | Which surface owns an infra version and what each gate proves                                               | Before bumping an infra component               | High; a separate override reintroduces drift                         |
| `## Local Development` concrete chart coordinates, live-install verification              | Which version a chart bump must reach and how a bump is proven                                              | Before bumping a chart                          | Medium; `verifyInfraImageVersions` does not prove deployment         |
| `## Key Environment Variables` CORS origins/headers, DB password, debug, health, JVM opts | What a surface declares and which four surfaces must agree (ADR-0002)                                       | Before wiring a deployment or a container       | High; a header dropped on one surface fails the UI cross-origin call |
| `### Deployment, Helm & Kubernetes` chart-first diagnosis, render, deployed signal, trace | How to diagnose a deployment and how to prove a deployment-only behavior                                    | Before the first hypothesis or probe            | High; a "cluster problem" is usually a chart or stale apply          |
| The same subsection's host-state rule (amd64 emulation)                                   | Whether a shifting image-build failure is host state before touching a pin                                  | Before changing a pin on a shifting failure     | High; a host defect mimics a pin defect                              |
| The same subsection's run-the-image verification and builder-pair rule                    | How a buildpack pin is proven and which pair is valid                                                       | Before choosing or clearing a pin               | High; a clean build with a wrong-architecture binary passes review   |
| The same subsection's CNB-id vs Docker Hub repository rule                                | Which repository a registry lookup targets, and how the two version operations differ                       | When adding or reading a buildpack update check | Medium; a 404-ing lookup reads as "no update"                        |
| `## Build & Test` Helm update suppression and observability bump smoke                    | Why a major infra chart bump is suppressed and how an observability bump is proven                          | Before interpreting the Helm update report      | Medium; a suppressed row reads as current                            |
| `## Build & Test` DB-scripts block                                                        | How to initialize the event store before running the command service standalone                             | Before a standalone command-service run         | Low; the service fails at startup                                    |

### Moved to `jvm-testing`

| Unit                                                              | Governed decision                                                                             | Earliest point needed                                                | Why here, not the deployment pack                                                                                                                                                                                                                                                                                                                |
| ----------------------------------------------------------------- | --------------------------------------------------------------------------------------------- | -------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `## Local Development` Kafka 3.9.0 `KAFKA_LISTENERS` note (401 B) | What a `KafkaContainer` needs so Kafka 3.9.0 starts, and what an image-tag bump must not drop | While writing or reworking a Kafka-container test, and on a tag bump | The override is written in the test code (`ShowcaseProjectorIT`, `ShowcaseApiGatewayE2E`, `ShowcaseLiveEventStreamIT`), so a test author loads `jvm-testing` and would never load a deployment pack; a test may be written with no tag change at all. The deployment pack carries a one-line pointer for the bump path rather than a second copy |

### Retained at root, with the reason

| Source anchor                                                                                                          | Governed decision                       | Why it stays                                                                                                            |
| ---------------------------------------------------------------------------------------------------------------------- | --------------------------------------- | ----------------------------------------------------------------------------------------------------------------------- |
| `## Subject guidance packs` classification row                                                                         | Which pack this subject loads           | The classification table's row **is** the retained root entry point; without it the destination is undiscoverable.      |
| `## Build & Test` minimal `check` command and the pack pointers                                                        | How to verify any change                | Needed before a task can know which pack to load; the only build command a docs-only task needs beside `spotlessApply`. |
| `## Continuous Integration`, `## Conventions`, `## OpenSpec Workflow Agreement`, `## Architecture`, `## Prerequisites` | Their own subjects                      | Out of this cluster or cross-cutting; untouched.                                                                        |
| The event-stream `keepAliveInterval` **chart value name**                                                              | A deployed stream's configured interval | It moves with the ingress block; the root keeps nothing of it, because no always-loaded rule depends on the value.      |

### Routed, not moved

| Unit                                                                                   | Route                                                                    | Why                                                                                                                                                                                                              |
| -------------------------------------------------------------------------------------- | ------------------------------------------------------------------------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Build-gate mechanics (`verifyInfraImageVersions` derivation, retry, cacheability)      | `showcase/quality/infra-image-versions` spec (already owns the contract) | A spec owns a capability outcome; the pack keeps the operational "which surface and what it proves" text.                                                                                                        |
| Gradle task surface (`buildpackUpdates`, `dependencyUpdates`, `compose*` registration) | `gradle-build` pack                                                      | The pack owns the Gradle command, gate, and task-registration surface; the moved text names it rather than restating it.                                                                                         |
| Test-container _authoring_ rules (imports, container wiring)                           | `jvm-testing` pack                                                       | They live there, and the Kafka 3.9.0 note joins them (see "Moved to `jvm-testing`" above) because the override is written in the test code; the deployment pack instead carries a pointer for the image-tag bump |

## Transfer manifest

The extraction contract the preflight applies. Source blocks are **anchored to `e42213a8`** — the coordinates are in
that revision, not in the working tree — and every block has one destination, one heading transformation, and a fixed
position. Headings are normalized to `##`: a source `##` keeps its level, a source `###` becomes `##`, and the two
headingless blocks gain a `##` heading. The order is part of the contract because in-pack references say "above" and
"below".

| #   | Source block (`e42213a8`)                                        | Destination   | Heading in the destination           | Note                                                                   |
| --- | ---------------------------------------------------------------- | ------------- | ------------------------------------ | ---------------------------------------------------------------------- |
| 1   | `## Docker Images` (1628–1696)                                   | pack          | `## Docker images`                   | first: the deployment section refers to "the Docker Images note above" |
| 2   | `## Kubernetes Deployment` (1697–1820)                           | pack          | `## Kubernetes deployment`           | carries the install block `verifyInstallCommands` reads                |
| 3   | `## Local Development` (1821–1899, plus the separator line 1904) | pack          | `## Local development`               | the Kafka note is excluded (row 8)                                     |
| 4   | `## Key Environment Variables` (1905–1919)                       | pack          | `## Key environment variables`       | "see Local Development" resolves to row 3                              |
| 5   | `### Deployment, Helm & Kubernetes` (2281–2354)                  | pack          | `## Deployment, Helm & Kubernetes`   | follows the sections it diagnoses                                      |
| 6   | `## Build & Test` → Helm update-check bullet (925–930)           | pack          | `## Helm update checks`              | headingless in the source; placed after row 5                          |
| 7   | `## Build & Test` → DB-scripts block (936–942)                   | pack          | `## DB scripts`                      | headingless in the source; last                                        |
| 8   | `## Local Development` → Kafka 3.9.0 note (1900–1903)            | `jvm-testing` | appended to its Testcontainers group | verbatim; the pack carries a pointer for a tag bump                    |

### Destination shape

The new skill follows the three existing packs' frontmatter exactly — `name` and `description`, no `license`, metadata,
or extra keys — and its `Source:` line names the replaced headings **at their original levels and wording**, so the
pairing survives a later retitle:

```markdown
---
name: deployment-operations
description:
  Deployment, container-image, local-stack, and environment/port guidance for this repository — Helm releases and
  values, image and buildpack pins, Docker Compose and `bootRun`, the deployed-system probes, and the environment
  variables the surfaces must agree on.
---

# Deployment operations

Source: `AGENTS.md`'s `## Docker Images`, `## Kubernetes Deployment`, `## Local Development`,
`## Key Environment Variables`, and `### Deployment, Helm & Kubernetes`, plus the Helm update-check bullet and the
DB-scripts block from `## Build & Test`.
```

No "load before…" first line: the trigger lives in the root classification row, as it does for the three existing packs.
`jvm-testing`'s own `Source:` line gains `## Local Development` for the Kafka note (documented edit 20).

## Documented edits

The move is verbatim except for these edits, which come from three sources: the move's own edits (the vacated sections'
pointers, and the enumerations that gain a member), the live hits of a grep for `verifyInstallCommands`,
`verifyDocumentedPins`, `AGENTS.md`, and "command surface" across `AGENTS.md`, `README.md`, `docs/`, `.opencode/`, and
`build-logic` at `e42213a8`, and the in-pack sentences the retarget falsifies. Each entry names the site so the
preservation check can distinguish a deliberate edit from a dropped sentence.

One grep hit is deliberately unchanged, recorded here so a re-run of the derivation resolves it: `README.md`'s "the
catalog's Paketo pins a live doc states" (generic, and the gate still reads `README.md`). `AGENTS.md`'s "each subject
pack carries its subject's full command surface" is the clause edit 3 extends rather than replaces — its wording stays,
so the derivation resolves it as edit 3.

| #   | Site                                                                                                                                                                           | Edit                                                                                                                                                                                                                                                                                                                                                                                                             |
| --- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | `.opencode/skills/gradle-build/SKILL.md` — its "see `AGENTS.md`'s `## Kubernetes Deployment` → Chart validation" cross-reference, and its Subject line's "the command surface" | The cross-reference names the `deployment-operations` pack; the Subject line narrows to the build command surface, since the deployment and local-stack commands move.                                                                                                                                                                                                                                           |
| 2   | `AGENTS.md` `## Architecture` (web UI line)                                                                                                                                    | "(see Docker Images)" → "(see the `deployment-operations` pack)"                                                                                                                                                                                                                                                                                                                                                 |
| 3   | `AGENTS.md` `## Conventions` README design intent                                                                                                                              | The clause "the root `AGENTS.md` carries the verification command every task needs and the environment/port reference, each subject pack carries its subject's full command surface" becomes "the root `AGENTS.md` carries the verification command every task needs and the classification, each subject pack carries its subject's full command surface and its reference material" — one pack clause, not two |
| 4   | `AGENTS.md` `## Conventions` pack-summary paragraph                                                                                                                            | Names the fourth pack's subject beside the three existing ones                                                                                                                                                                                                                                                                                                                                                   |
| 5   | `AGENTS.md` capture destination clause (step 2 bullet)                                                                                                                         | The subject-pack enumeration gains `deployment-operations`                                                                                                                                                                                                                                                                                                                                                       |
| 6   | `AGENTS.md` `## Subject guidance packs` preamble + table                                                                                                                       | "Three packs" → "Four packs"; one row added; the table's stated overlap rule already covers a row pair firing together                                                                                                                                                                                                                                                                                           |
| 7   | `.opencode/agent/lesson-capture.md` frontmatter + body                                                                                                                         | Both enumerating clauses gain the new pack                                                                                                                                                                                                                                                                                                                                                                       |
| 8   | `openspec/config.yaml` context                                                                                                                                                 | The pack list gains the new pack; the Docker-images and Deployment restatements point at the pack                                                                                                                                                                                                                                                                                                                |
| 9   | `AGENTS.md` `## Build & Test` Helm bullet                                                                                                                                      | Replaced by a one-line pointer to the pack                                                                                                                                                                                                                                                                                                                                                                       |
| 10  | `AGENTS.md` `## Build & Test` DB-scripts block                                                                                                                                 | Moved; the config context's `./db.sh init` line points at the pack                                                                                                                                                                                                                                                                                                                                               |
| 11  | The moved `verifyDocumentedPins` sentence (`AGENTS.md:1875`)                                                                                                                   | "wherever `AGENTS.md` or `README.md` states them" → "wherever `AGENTS.md`, `README.md`, or this pack states them", since the gate now scans all three                                                                                                                                                                                                                                                            |
| 12  | `build-logic/.../InstallCommandRules.kt` + `InstallCommandRulesTests.kt`                                                                                                       | The KDoc names the source document; `mismatchReason` takes the source label as a parameter so its two messages no longer hard-code `AGENTS.md`; the test class's `@DisplayName("AGENTS.md install command rules")` names the rule set rather than the old document                                                                                                                                               |
| 13  | `build-logic/.../VerifyInstallCommandsTask.kt`                                                                                                                                 | KDoc, the `agentsFile` property (renamed `sourceFile`, doc included) and the success `println` name `AGENTS.md`; each names the source document                                                                                                                                                                                                                                                                  |
| 14  | `build.gradle.kts` `verifyInstallCommands`                                                                                                                                     | The task description ("Verifies the AGENTS.md manual helm install commands …") and the `agentsFile.set(...)` assignment follow the rename                                                                                                                                                                                                                                                                        |
| 15  | `docs/guidance-evidence/documentation-and-durable-claims.md`                                                                                                                   | The closure-tracking sentence places `paketo-buildpacks/nginx#1340` "in `## Docker Images` and the deployment gotcha" → names the pack's two sections                                                                                                                                                                                                                                                            |
| 16  | `AGENTS.md`'s ``### Workflow: Extract an `AGENTS.md` section``, step 2                                                                                                         | "a gate that reads `AGENTS.md` as its input, such as `verifyInstallCommands`, `verifyDocumentedPins`, and `verifyCapturedMarkers`" → a gate that scans the root's or a subject pack's text, with the same examples, so the next extraction is not told two of the three read only the root                                                                                                                       |
| 17  | `README.md`'s verification-command block                                                                                                                                       | The `verifyInstallCommands` comment ("verify the AGENTS.md manual install commands match the catalog") names the pack                                                                                                                                                                                                                                                                                            |
| 18  | `AGENTS.md` `## Build & Test` intro (`:887`)                                                                                                                                   | "The full Gradle command surface … are in the `gradle-build` pack" → the build command surface is there and the deployment and local-stack commands are in the new pack                                                                                                                                                                                                                                          |
| 19  | `.opencode/skills/gradle-build/SKILL.md` update-check list                                                                                                                     | "`buildpackUpdates` the Paketo builder and buildpack pins" gains a pointer: the buildpack/builder pin mechanics — the builder-and-buildpacks pair, the run-the-image verification, and the CNB id versus Docker Hub repository — are the `deployment-operations` pack's                                                                                                                                          |
| 20  | `.opencode/skills/jvm-testing/SKILL.md`                                                                                                                                        | Its `Source:` line gains `## Local Development` for the Kafka note, and the note joins its Testcontainers group verbatim                                                                                                                                                                                                                                                                                         |
| 21  | The new pack's `## Local development`, infra-versions paragraph                                                                                                                | One sentence naming the constraint without restating it: "The Kafka image-tag bump's `KAFKA_LISTENERS` constraint is in `jvm-testing`'s Kafka 3.9.0 note."                                                                                                                                                                                                                                                       |

### Cross-references and clauses the move touches

Each was read at `e42213a8`. Five cross-references resolve unchanged inside the pack — which must therefore preserve the
source order of its sections, a reorder being a documented edit rather than a formatting choice — and one clause is
rewritten by item 11.

| Site (pre-change)                                         | Text                                                                  | Disposition                                                                                       |
| --------------------------------------------------------- | --------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------- |
| `AGENTS.md:1737` (`## Kubernetes Deployment`)             | "(see the Docker Images note above)"                                  | Unchanged: `## Docker Images` precedes the deployment section in the pack, so "above" resolves    |
| `AGENTS.md:1792` (`## Kubernetes Deployment`)             | "(see the Docker Images note)"                                        | Unchanged: same ordering                                                                          |
| `AGENTS.md:1873` (`## Local Development`)                 | "in this file's Kubernetes Deployment section"                        | Unchanged: the pack carries that section, so "this file's" still resolves                         |
| `AGENTS.md:1875` (`## Local Development`)                 | "wherever `AGENTS.md` or `README.md` states them"                     | **Edited** by item 11 — the gate's document set moves with the pins                               |
| `AGENTS.md:1916` (`## Key Environment Variables`)         | "see Local Development"                                               | Unchanged: both sections are in the pack                                                          |
| The Docker Images text's "the buildpack-pin gotcha below" | forward reference from `## Docker Images` into the Gotchas subsection | Unchanged: the gotcha subsection follows the deployment sections in the pack, so "below" resolves |

**Deliberately left.** The same guidance-evidence paragraph's earlier clause says `#755` "stays cited in ADR-0007,
`README.md`, and `## Build & Test`". That location is already stale — `#755` is cited in ADR-0007, `README.md`, and
`.opencode/skills/gradle-build/SKILL.md` since `6518d081`, and `AGENTS.md` holds no `#755` (only `100755`) — so this
change did not cause it and does not fix it here: a stale fact a change passes by is its own docs unit. Task 2.6 parks
it.

## Gate wiring

Three gates read `AGENTS.md`, and one scan's scope already covers the destination. All four were verified in source.

| Gate                    | Current wiring (verified)                                                                                                                                     | Change owed                                                                                                                                                                                                                                                                         | Committed control                                                                                                                                                                 |
| ----------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `verifyInstallCommands` | `agentsFile.set(… "AGENTS.md")`; `InstallCommandRules.installCommands` parses `helm install … --version`, `mismatchReason` names "AGENTS.md"                  | Point the input at the pack; the rules' message takes the source label as a parameter                                                                                                                                                                                               | The rules report a document with no install command (`noCommandsReason`), so a document the gate cannot check fails instead of passing; a mismatched version fails the real task. |
| `verifyDocumentedPins`  | `docs.from(AGENTS.md, README.md)`; `VerifyDocumentedPinsTask.verify` keys the docs by `it.name`; `DocumentedPinRules` holds the parsing and the mismatch rule | Add the pack to the existing `AGENTS.md` + `README.md` set (keeping both), key every doc by its project-relative path, and declare the scanned set (`LIVE_DOCS`) so the task fails when a declared document is not scanned or when the pin document states no pin for a catalog key | The rules test covers all three documents, two same-named `SKILL.md` docs, and the absent/unstated cases; dropping the pack from `docs.from` fails the task                       |
| `verifyCapturedMarkers` | `commit-hygiene.py --markers`; `MARKER_GLOBS = (".opencode/agent/*.md", ".opencode/skills/*/SKILL.md")`                                                       | **None** — the new pack matches the glob. Verify by a marker in a deliberately misplaced position in the pack, then revert.                                                                                                                                                         | The existing misplaced-marker cases in `scripts/test-commit-hygiene.py`.                                                                                                          |
| `upstreamReferences`    | `UpstreamReferenceScope` covers `AGENTS.md`, `README.md`, `docs/ideas.md`, `docs/adr/`, and every project-authored skill                                      | **None** — verify the moved `paketo-buildpacks/nginx#1340` and `buildpacks/pack#2527` references still appear in the report.                                                                                                                                                        | The existing scope tests, which already include the project-skill case.                                                                                                           |

A gate that scans an emptied document reads as clean, so the two retargets also carry a real failure: corrupt a version
in the pack, run the gate, observe the named failure, revert. The scanned-document set is the committed half, following
`UpstreamReferenceScope`'s precedent, because a task-input path in `build.gradle.kts` has no unit test of its own.

The retarget **adds** the pack to the existing `AGENTS.md` and `README.md` entries rather than replacing them: the move
takes no coverage away from the gate, so a later edit that restores a pin to the root is still checked. The document-set
function keys each document by its **project-relative path**, not its file name: `SKILL.md` is the file name of every
subject pack, so a name key would silently drop one pack's pins once a second pack states them. That is a rule-level fix
with a test case (two `SKILL.md` documents both scanned), not a task-input workaround.

## Delta rationale

`openspec/config.yaml`'s design rule requires each amended clause quoted beside its replacement, with the reason. Three
clauses change; two in `infra-image-versions` and one in `agent-skills`.

**Amendment 1 — where the manual install commands live.**

- Before: "The `AGENTS.md` Kubernetes Deployment section's manual `helm install … --version` commands SHALL quote each
  chart's catalog version, so a chart bump cannot leave the documented command stale."
- After: "The `deployment-operations` pack's Kubernetes Deployment block's manual `helm install … --version` commands
  SHALL quote each chart's catalog version, so a chart bump cannot leave the documented command stale."
- Why: the block moves in this change. The obligation, its subject, and its failure mode are unchanged; only the
  document that carries it changes, and the gate is retargeted in the same change (`verifyInstallCommands`).

**Amendment 2 — where the Paketo pins are stated, and which docs are verified.**

- Before: "A live doc (`AGENTS.md` or `README.md`) SHALL state the catalog's Paketo pins — the buildpack builder and the
  buildpacks — at their catalog versions, so a buildpack bump cannot leave the documented pin stale."
- After: "That pack SHALL state the catalog's Paketo pins — the buildpack builder and the buildpacks — at their catalog
  versions, so a buildpack bump cannot leave the documented pin stale. `check` SHALL verify every stated pin in that
  pack, `AGENTS.md`, and `README.md`, keeping the coverage the root's pins had before the move."
- Why: `README.md` states no Paketo pin today (`grep -cE 'paketo-buildpacks/|builder-jammy-base:' README.md` reports 0),
  so the only document satisfying the "live doc" clause is `AGENTS.md`'s `## Docker Images`, which moves. Leaving the
  clause generic would let the requirement be satisfied by a document that states nothing, i.e. vacuously; naming the
  pack makes the obligation concrete. The verification clause names all three documents because the gate keeps scanning
  the root and `README.md` — the retarget adds coverage rather than trading one document for another.

**Amendment 3 — the capture destination enumeration.**

- Before: "a section of the subject pack (`jvm-development`, `jvm-testing`, or `gradle-build`) whose subject owns the
  lesson's decision".
- After: "a section of the subject pack (`jvm-development`, `jvm-testing`, `gradle-build`, or `deployment-operations`)
  whose subject owns the lesson's decision".
- Why: the set gains a member. The obligation is unchanged, and the change edits the other two copies of the same
  enumeration (`AGENTS.md`'s capture clause and `lesson-capture.md`), so leaving the spec's copy at three would be the
  drift the single-sourcing rule forbids. The `MODIFIED` block carries the requirement's full block and every scenario
  in its existing order and names.

**The install-command scenario keeps its title.** "The AGENTS.md manual install commands match the catalog" cannot be
renamed — `openspec validate` matches a `MODIFIED` block's scenarios by name and rejects a rename — so its body names
the pack while its title keeps the old location. That is an artefact of the no-rename rule, not a claim about where the
commands live; the requirement description and the body are authoritative.

**No fourth amendment.** `agent-skills`' subject-pack permission and classification requirement are generic: a fourth
row is what they already require, and the pack's `Source:` line is the association they require. The advisor's hand-over
expected an `agent-skills` delta for the destination class; the class needs no change, and only the enumeration does.

## Routing design

The root keeps the classification table as the retained entry point. The added row:

| Pack                    | Trigger (abridged here; the table states it in full)                                                                                                                                              | Path                                              |
| ----------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------- |
| `deployment-operations` | choosing, changing, reviewing, executing, or diagnosing a deployment, a container image or its buildpacks, the local stack, or the environment/port wiring — not only before running a deployment | `.opencode/skills/deployment-operations/SKILL.md` |

Each vacated section is replaced by one pointer naming the destination and the trigger, so a reader who lands on
`## Docker Images` or the deployment Gotchas subsection reaches the pack before the governed decision. The pack opens
with its `Source:` line, naming `## Docker Images`, `## Kubernetes Deployment`, `## Local Development`,
`## Key Environment Variables`, `### Deployment, Helm & Kubernetes`, and the two `## Build & Test` units.

**Why the trigger is broad.** The advisor's review of the first draft found the trigger "deploying, building an image,
or running the stack" fires too late: the builder/buildpack pairing rule is needed _before a pin is chosen_, and chart
design and value wiring decisions precede execution. The row therefore names choosing, changing, reviewing, executing,
and diagnosing.

**Why the pack is not `gradle-build`.** A deployment change is not a build change; widening `gradle-build` would make
every dependency or build task load operational instructions it never uses. The two rows can fire together (an image or
buildpack bump reads the update report), and the root's existing overlap rule already says to load every matching row.

## Loading designs compared

| Design                                                | Root always-loaded         | Added always-loaded metadata | Per-task load (docs / Java / build / deployment)       | Verdict                                                                        |
| ----------------------------------------------------- | -------------------------- | ---------------------------- | ------------------------------------------------------ | ------------------------------------------------------------------------------ |
| **A. Retention**                                      | 293.1 KB                   | 0                            | 293.1 / 293.1 / 293.1 / 293.1 KB                       | Baseline                                                                       |
| **B. Root pointers to the capability specs**          | ~292 KB                    | 0                            | same, with no destination owning the operational text  | Rejected: a spec is not loaded at the decision and owns outcomes, not commands |
| **C. Widen `gradle-build`**                           | ~258 KB                    | 0                            | build class grows by 34.6 KB                           | Rejected: every Gradle task pays for operational text it never uses            |
| **D. New `deployment-operations` pack** — recommended | ~258 KB                    | ~0.9 KB                      | 258.7 / 258.7 / 258.7 / 294 KB                         | Recommended: only the deployment class pays, and it ends ≈ +0.9 KB             |
| **E. Nested `AGENTS.md`**                             | unchanged + a file on read | 0                            | fires on a file read, not on a proposal-stage decision | Rejected: the subject spans `helm/`, `docker-compose.yml`, and test surfaces   |

## Representative task validation

Two questions are kept apart: **which packs the existing classification requires** (the complete set) and **whether the
new pack is required**. The full matrix is checked statically against the root table and the pointers; fresh-session
probes are reserved for the paths whose _discovery_ differs, not for one fixture per row.

| Task class                                                          | Required packs (complete set)                                                                          | `deployment-operations` required?       | Retained root text that must suffice                              |
| ------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------ | --------------------------------------- | ----------------------------------------------------------------- |
| Docs-only or editorial change                                       | none — unless the edit states a deployment, image, stack, or environment fact, whose pack then applies | no (unless that fact is in its subject) | the verification command and the classification                   |
| Java production change                                              | `jvm-development`                                                                                      | no                                      | as above                                                          |
| Test authoring, any tier                                            | `jvm-testing`                                                                                          | no                                      | as above                                                          |
| Kafka-container test authoring, no tag bump                         | `jvm-testing`                                                                                          | no                                      | as above — the note is in that pack                               |
| Build change to build-and-test config (gates, suites, catalog)      | `gradle-build`                                                                                         | no                                      | as above                                                          |
| Build change to an image-build task or a builder/buildpack pin      | `gradle-build` + `deployment-operations`                                                               | yes                                     | the classification row's image/buildpack clause                   |
| Dependency or tooling bump                                          | `gradle-build`                                                                                         | no                                      | as above                                                          |
| Chart value, chart Deployment addition, or values file              | `deployment-operations` (+ `gradle-build` when the build script changes)                               | yes                                     | the pointer at the vacated `## Kubernetes Deployment`             |
| Local-stack run (`compose*`, `bootRun`, `db.sh`) or port/env wiring | `deployment-operations`                                                                                | yes                                     | the pointer at the vacated `## Local Development`                 |
| Deployment diagnosis (503, crash-loop, hostname)                    | `deployment-operations` **before the first hypothesis**                                                | yes                                     | the same pointer, reachable from the Gotchas subsection's pointer |
| Deployment-smoke workflow edit (`daemon.json`, install order)       | `deployment-operations`                                                                                | yes                                     | the classification row's image clause                             |
| Image or buildpack bump                                             | `deployment-operations` (+ `gradle-build` when the update report is read)                              | yes                                     | the classification row's bump clause                              |
| Proposal that only mentions deployment                              | what the change will decide                                                                            | per its decisions                       | the classification row                                            |

The docs/editorial row's negative half is probe 5 below: a genuinely unrelated editorial task loads no pack.

**Five fresh-session probes**, each in its own isolated read-only sandbox and each told only its task, never the pack it
should load:

1. A docs-only change — ideally no pack. The fixture actually used touched the tool list and its Helm version, so it
   loaded the pack; it is recorded as a qualified positive, and probe 5 supplies the negative control.
2. An installed release answering 503 after an infra values re-apply — the deployment pack loads before the first
   hypothesis, not after.
3. A Gradle-only entry: a task that must change a buildpack/builder pin or its registry rule, starting from
   `gradle-build`'s update-check list pointer (documented edit 19) — it must reach the deployment pack through that
   pointer, which is the route edit 19 adds. Edit 1's chart-validation cross-reference is a second, separate route and
   is checked statically rather than probed.
4. Kafka-container test authoring with no image-tag change — `jvm-testing` loads and the `KAFKA_LISTENERS` note is
   reachable there.
5. An unrelated editorial task (a duplicated word in a retrospective) — no pack loads; this is the negative control.

## Measurement

Measured on the implemented tree (`wc -c`), replacing the planning estimate. The candidate was built and checked in a
scratch clone before the real root lost a byte.

| Component                                          |      Bytes | Class    | Source                                                                                                                                                     |
| -------------------------------------------------- | ---------: | -------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `AGENTS.md` before                                 |    293,123 | Measured | `git show e42213a8:AGENTS.md \| wc -c`                                                                                                                     |
| `AGENTS.md` after                                  |    261,382 | Measured | `wc -c AGENTS.md`                                                                                                                                          |
| **Net root reduction**                             | **31,741** | Measured | before − after; −10.8%                                                                                                                                     |
| New pack                                           |     34,908 | Measured | `wc -c .opencode/skills/deployment-operations/SKILL.md`                                                                                                    |
| `jvm-testing` growth (Kafka note + `Source:` line) |       +467 | Measured | 13,289 after vs 12,822 before                                                                                                                              |
| Root additions (pointers + row + preamble)         |      2,827 | Reasoned | 34,568 moved − 31,741 net; the eight named spans measure 2,053 B, the remainder is the preamble change and the formatter's reflow of the surrounding prose |

The reduction is 31,741 B rather than the planned ~33,000 B: the retained pointers and the classification row add 2,827
B (34,568 − 31,741), about 2.5× the ~1,150 B the plan estimated, and the formatter re-wrapped the surrounding prose. The
figure is the measured one.

## Preflight results

Run in a scratch clone of `e42213a8` before the real tree changed; the clone carried the candidate pack, root, and
`jvm-testing` edit.

| Check                                                                     | Result                                                                                                                                                                                                                                                                                                                                                                     |
| ------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Source→candidate containment                                              | 0 misses: every line of the seven ranges is in the pack before formatting; after formatting the word-level comparison is 0 misses (only the documented edit 11 differs)                                                                                                                                                                                                    |
| Root absence                                                              | 0 moved content lines survive in the candidate root                                                                                                                                                                                                                                                                                                                        |
| Markers                                                                   | 10 moved `captured:` markers, 10 in the pack, each inside its rule; the marker scan passes in the clone and the real tree; 0 `evidence:` tokens                                                                                                                                                                                                                            |
| Identifiers                                                               | every listed identifier resolves in the pack, `jvm-testing`, or the root — none is missing                                                                                                                                                                                                                                                                                 |
| Pointers                                                                  | each of the five pointers names the pack, its path, and the trigger; the pack carries the matching section                                                                                                                                                                                                                                                                 |
| Gate controls                                                             | both gates pass naming the pack; a wrong chart version and a wrong Paketo pin each fail the real task with the pack's relative path; a document with no install block fails `verifyInstallCommands`, and a pin document missing a catalog pin fails `verifyDocumentedPins`; dropping the pack from `docs.from` fails with "does not scan: …"; `-p build-logic test` passes |
| Probe 1 — docs-only                                                       | Loaded the deployment pack: the fixture's edit touched the tool list and its Helm version, a tooling fact the pack covers. Recorded as a qualified positive, superseded as the negative control by probe 5                                                                                                                                                                 |
| Probe 2 — 503 after an infra values re-apply                              | First run logged no reads (a harness artifact); the re-run with a read log loaded the pack second, quoted the moved 503 sentence, and hypothesized after it. Pass                                                                                                                                                                                                          |
| Probe 3 — buildpack/builder pin                                           | Both `gradle-build` and `deployment-operations` loaded; the pair rule, the run-the-image verification, and the registry rule were reproduced through edit 19's pointer. Pass                                                                                                                                                                                               |
| Probe 4 — Kafka test without a tag bump                                   | `jvm-testing` loaded and the `KAFKA_LISTENERS` constraint was reproduced from the moved note. Pass                                                                                                                                                                                                                                                                         |
| Probe 5 — unrelated editorial task (a duplicated word in a retrospective) | `AGENTS.md` only, `Packs: none`; the negative control the earlier docs probe could not provide. Pass                                                                                                                                                                                                                                                                       |

Two implementation-time judgements the plan did not specify, both wiring-level:

- The configuration cache rejects `project` at execution time, so **both** gates take a `repoRoot` (`@Internal`
  directory property) and derive their labels from it in the task action: the install gate relativizes its `sourceFile`,
  the pins gate relativizes the files in `docs`. No project-relative path is written twice.
- `LIVE_DOCS` alone was a declaration the gate did not read, so the pins gate now asserts at run time that its scanned
  set covers every declared document and fails naming any it does not; dropping the pack from `docs.from` fails the task
  with "The documented-pin gate does not scan: …", which is the control that reproduces the defect the path keying
  fixed.

## Risks / trade-offs

| Risk                                                                                           | Mitigation                                                                                                                                                                                                             |
| ---------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| A Gradle-only task editing the buildpack update check does not load the pack's repository rule | The documented edit 19 puts a pointer at `gradle-build`'s update-check list naming the pack for the pin mechanics; the probe runs that class.                                                                          |
| A test author writing a Kafka-container test never loads the deployment pack                   | The note moves verbatim into `jvm-testing`, which that author loads by classification; the deployment pack carries a pointer for a tag bump, and no second copy exists.                                                |
| A gate whose document is missing passes vacuously                                              | The pins gate asserts its scanned set covers `LIVE_DOCS` and fails naming an uncovered document; the install gate fails a document that yields no install command; both failure sides are exercised by real bad input. |
| A second pack states pins and the name-keyed document map drops one of them                    | The document set is keyed by project-relative path, with a test case that scans two `SKILL.md` documents.                                                                                                              |
| A pointer that names the destination but not what to do                                        | Each pointer states the trigger and the condition, not only the path.                                                                                                                                                  |
| The deployment class pays a small increase                                                     | Accepted and reported: the owner's objective is the always-loaded surface, and the increase is bounded by the metadata estimate.                                                                                       |
| `spotlessApply` reflows the moved markdown, and an inline code span could be split             | The pack is formatted once as the last write, no span is authored across a source line break, and task 2.5 verifies `spotlessCheck` plus the absence of a split span over the pack.                                    |
| A `captured:` marker lands outside a rule block after the move                                 | The existing marker scan runs over the pack, and the move keeps each marker at its rule's end.                                                                                                                         |

## Claims table

The pre-write derivation pass's record, **re-derived from the final edit list** rather than appended per edit. Every row
about an artifact this change edits anchors to that artifact's pre-change content (`git show e42213a8:<path>`), because
the figure or sentence stops reproducing once the change lands.

| Claim (as stated in these artifacts)                                                                                              | Source / derivation                                                                                                                                                                                                                                                                                                                                            |
| --------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `AGENTS.md` is 293,123 bytes / 3,036 lines at `e42213a8`                                                                          | `git show e42213a8:AGENTS.md \| wc -c -l`                                                                                                                                                                                                                                                                                                                      |
| The section byte table sums to 293,110 B, remainder 13                                                                            | `python3` over the `##`-delimited spans of `git show e42213a8:AGENTS.md`; the title line and its blank line are 13 bytes                                                                                                                                                                                                                                       |
| The seven moved spans total 34,568 B                                                                                              | `python3` over the ranges 1628–1696, 1697–1820, 1821–1904, 1905–1919, 2281–2354, 925–930, 936–942 of `git show e42213a8:AGENTS.md`                                                                                                                                                                                                                             |
| The existing packs are 10,529 / 12,822 / 20,278 B                                                                                 | `git show e42213a8:.opencode/skills/{jvm-development,jvm-testing,gradle-build}/SKILL.md \| wc -c`                                                                                                                                                                                                                                                              |
| `verifyInstallCommands` reads `AGENTS.md` through `agentsFile.set`                                                                | `git show e42213a8:build.gradle.kts`, the `verifyInstallCommands` registration                                                                                                                                                                                                                                                                                 |
| `verifyDocumentedPins` reads `AGENTS.md` and `README.md` through `docs.from`                                                      | `git show e42213a8:build.gradle.kts`, the `verifyDocumentedPins` registration                                                                                                                                                                                                                                                                                  |
| `InstallCommandRules`' KDoc and its two `mismatchReason` messages hard-code `AGENTS.md`                                           | `git show e42213a8:build-logic/src/main/kotlin/InstallCommandRules.kt`                                                                                                                                                                                                                                                                                         |
| `VerifyInstallCommandsTask` names `AGENTS.md` in its KDoc, its `agentsFile` doc, and its success `println`                        | `git show e42213a8:build-logic/src/main/kotlin/VerifyInstallCommandsTask.kt`                                                                                                                                                                                                                                                                                   |
| `InstallCommandRulesTests` is `@DisplayName("AGENTS.md install command rules")`                                                   | `git show e42213a8:build-logic/src/test/kotlin/InstallCommandRulesTests.kt`                                                                                                                                                                                                                                                                                    |
| The marker scan already covers project skills                                                                                     | `git show e42213a8:scripts/commit-hygiene.py` (`MARKER_GLOBS`); the `verifyCapturedMarkers` `inputs.files` tree                                                                                                                                                                                                                                                |
| `upstreamReferences` already covers project skills                                                                                | `git show e42213a8:build-logic/src/main/kotlin/UpstreamReferenceScope.kt`; the `upstreamReferences` registration                                                                                                                                                                                                                                               |
| `infra-image-versions` names the `AGENTS.md` block and "a live doc (`AGENTS.md` or `README.md`)"                                  | `git show e42213a8:openspec/specs/showcase/quality/infra-image-versions/spec.md`, the single-sourcing requirement and two scenario bodies                                                                                                                                                                                                                      |
| `agent-skills` permits the destination **class** generically; one scenario enumerates the three packs                             | `git show e42213a8:openspec/specs/showcase/quality/agent-skills/spec.md`, the subject-pack permission, the classification requirement, and "Lessons are captured after implementation"                                                                                                                                                                         |
| `code-quality`'s markdown scope covers project-authored `.opencode/` skills                                                       | `git show e42213a8:openspec/specs/showcase/quality/code-quality/spec.md`, the markdown-scope requirement                                                                                                                                                                                                                                                       |
| No moved unit carries an `evidence:` token (count 0)                                                                              | `python3` over the seven ranges of `git show e42213a8:AGENTS.md`: `text.count("evidence:")` = 0                                                                                                                                                                                                                                                                |
| The Kafka note is 401 B (`AGENTS.md` lines 1900–1903 at `e42213a8`)                                                               | `python3` over `git show e42213a8:AGENTS.md`, that range; the remaining `## Local Development` bytes are 6,926, including the separator line                                                                                                                                                                                                                   |
| The deployment pack's share is 34,167 B                                                                                           | 34,568 − 401; the manifest's rows 1–7                                                                                                                                                                                                                                                                                                                          |
| `AGENTS.md` is 261,382 B after the move                                                                                           | `wc -c AGENTS.md` on the implemented tree                                                                                                                                                                                                                                                                                                                      |
| The net root reduction is 31,741 B (−10.8%)                                                                                       | 293,123 − 261,382; both `wc -c`                                                                                                                                                                                                                                                                                                                                |
| The new pack is 34,908 B                                                                                                          | `wc -c .opencode/skills/deployment-operations/SKILL.md`                                                                                                                                                                                                                                                                                                        |
| `jvm-testing` grew by 467 B                                                                                                       | `wc -c` before (12,822) and after (13,289)                                                                                                                                                                                                                                                                                                                     |
| Root additions reconcile at 2,827 B                                                                                               | 34,568 moved − 31,741 net; the eight pointer/row spans measure 2,053 B, the remainder the preamble change and reflow                                                                                                                                                                                                                                           |
| The moved cluster carries 10 `captured:` occurrences across 8 names                                                               | `python3` over the seven ranges of `git show e42213a8:AGENTS.md`: `text.count("captured:")` = 10; the names are `add-deployment-smoke`, `bump-buildpacks-2026-10-05`, `bump-buildpacks-2026-10-12`, `expose-grafana-by-hostname`, `fix-the-deployment-smoke-503s`, `propagate-ui-trace-context`, `right-size-infra-resources`, `trim-deployment-smoke-install` |
| `README.md` states no Paketo pin today                                                                                            | `git show e42213a8:README.md`, then two greps: `grep -c 'paketo-buildpacks/'` and `grep -c 'builder-jammy-base:'` each report 0 (a pin-form search, not a bare word)                                                                                                                                                                                           |
| The prior reduction changes' byte deltas                                                                                          | `git show <sha>^:AGENTS.md \| wc -c` vs `git show <sha>:AGENTS.md \| wc -c` for `c5dea506`, `b7f26469`, `e07f5f8f`, `3903a889`, `6518d081`                                                                                                                                                                                                                     |
| The prior decisions named in the recovered-decisions table say what its rows claim                                                | `git show e42213a8:openspec/changes/archive/2026-10-21-separate-agents-md-core-and-subject-packs/{proposal,design}.md`; the same commit's `reduce-agents-md-loaded-surface` archive; `AGENTS.md`'s ``### Workflow: Extract an `AGENTS.md` section``                                                                                                            |
| The moved pins sentence names `AGENTS.md`/`README.md` (edit 11)                                                                   | `git show e42213a8:AGENTS.md`, line 1875                                                                                                                                                                                                                                                                                                                       |
| The extraction workflow's step 2 example list names the three gates (edit 16)                                                     | `git show e42213a8:AGENTS.md`, ``### Workflow: Extract an `AGENTS.md` section``, step 2                                                                                                                                                                                                                                                                        |
| `## Build & Test`'s intro says "the full Gradle command surface … are in the `gradle-build` pack" (edit 18)                       | `git show e42213a8:AGENTS.md`, line 887                                                                                                                                                                                                                                                                                                                        |
| `README.md`'s verification block comments the gate as reading `AGENTS.md` (edit 17)                                               | `git show e42213a8:README.md`, line 981                                                                                                                                                                                                                                                                                                                        |
| `gradle-build`'s Subject line says "the command surface" (edit 1)                                                                 | `git show e42213a8:.opencode/skills/gradle-build/SKILL.md`, lines 5 and 15                                                                                                                                                                                                                                                                                     |
| `gradle-build`'s update-check list names `buildpackUpdates` for the Paketo pins and has no pointer to the pin mechanics (edit 19) | `git show e42213a8:.opencode/skills/gradle-build/SKILL.md`, the build-tool-versions bullet                                                                                                                                                                                                                                                                     |
| The Kafka `KAFKA_LISTENERS` override is written in three test classes, so a test author reaches the note through `jvm-testing`    | `grep -rn KAFKA_LISTENERS --include '*.java'` at `e42213a8`: `ShowcaseProjectorIT.java:57`, `ShowcaseApiGatewayE2E.java:89`, `ShowcaseLiveEventStreamIT.java:48`                                                                                                                                                                                               |
| `jvm-testing` has a Testcontainers group the Kafka note joins                                                                     | `git show e42213a8:.opencode/skills/jvm-testing/SKILL.md`, the "Testcontainers 2.0.5" bullet and its neighbours                                                                                                                                                                                                                                                |
| The three existing packs' frontmatter is `name` + `description` only                                                              | `git show e42213a8:.opencode/skills/{jvm-development,jvm-testing,gradle-build}/SKILL.md`, first six lines of each                                                                                                                                                                                                                                              |
| A guidance-evidence record places the `#1340` reference in `## Docker Images` and names `#755`'s old site                         | `git show e42213a8:docs/guidance-evidence/documentation-and-durable-claims.md`, the closure-tracking paragraph                                                                                                                                                                                                                                                 |
| The parked Git-tranche candidates total −3,722 B of root                                                                          | `git show e42213a8:docs/ideas.md`, the 2026-10-20 entry, which carries the per-candidate measurements                                                                                                                                                                                                                                                          |
| The 2026-10-19 audit's ledger entries and stale figures                                                                           | `git show e42213a8:docs/audits/2026-10-19.md`, the instruction-budget ledger and extraction-opportunities subsections                                                                                                                                                                                                                                          |
| The parked ideas named above are open and unrelated                                                                               | `git show e42213a8:docs/ideas.md`, the dated sections cited                                                                                                                                                                                                                                                                                                    |

**Dismissals.** `scripts/claim-scan.py --base origin/main` seeded the table; its candidates are resolved by class, and
the ones that are a claim in their own right are rows above.

- **`count`** — a figure that states a measurement or a result of this change is a row above; the class's other
  candidates are dismissed by kind: the loading-design table's `Reasoned`/`Proposed` figures, which implementation
  re-measures (task 3.5) and which are marked as estimates where they appear; the rounded "34.6 KB" of the measured
  34,568 B row; and scanner artefacts — a tool version read as a count ("Docker 29 store"), a probe label ("step 2
  bullet"), an HTTP status ("503"), a list ordinal ("11").
- **`absolute`** (`every`, `only`, `never`, `always`) — a quantifier inside a rule this design states about its own
  scope (for example "every other class ends ~33 KB below it"), not a claim over a corpus; the class-level claim it
  rests on is the per-class load table, which the implementation measures. No corpus-wide universal claim is made here.
- **`cross-ref`** — slug pairs such as `buildpack/builder` and `environment/port` describe a relationship, not a tracked
  reference; the tracked ones (`#655`, `paketo-buildpacks/nginx#1340`, `buildpacks/pack#2527`,
  `spring-projects/spring-boot#49251`, `ADR-0018/0019`) are read from the artifact that carries them: `git log`,
  `docs/adr/`, and the moved text itself.
- **`path`** — a repository path or command the row's own source column resolves; naming a file is not a claim about its
  content.

## Decision

Build one unit: create `deployment-operations`, move the cluster verbatim (the Kafka note to `jvm-testing`), keep one
pointer per vacated section and one classification row, retarget the two gates with committed controls, and report
complete loads per task class. Entry gate: the move is verified in a scratch clone before the real root loses a byte; if
a positive control fails, stop with the root untouched and return the failing case to the owner rather than shipping a
partial move.

# Design

## Context

The owner asked whether large, coherent sets of technical instructions can load only for relevant tasks while preserving
every obligation, and whether that reduces the surface an agent actually receives — not merely the root file's byte
count. This design answers with a measured pilot in two subject areas: JVM development (production code, runtime
diagnosis, tests) and the Gradle build (build logic, dependency management). It inventories every obligation, compares
concrete loading designs, drafts the destinations, and recommends one unit.

**The premise, stated once.** `AGENTS.md` is the only guidance file a session loads automatically, and it is loaded in
full. A subject pack moves text out of that always-loaded surface and pays it back only when a task enters the subject.
The saving therefore equals **what a task can skip**, never the bytes moved: a task that enters every subject loads
every pack plus the smaller root and can end at parity or above. The pilot's ceiling is the two subjects' combined share
of the root; any claim beyond that is wrong. This design reports each task class separately for that reason.

### Reproducible baseline

| Fact             | Command                                                                          | Result                                         |
| ---------------- | -------------------------------------------------------------------------------- | ---------------------------------------------- |
| Revision         | `git rev-parse HEAD`                                                             | `4972e6877c2980d2aa3a701945c63a88a882ff0f`     |
| Preceding change | `git log -1 --format=%s`                                                         | `consolidate-docs-update-obligations` (merged) |
| Working tree     | `git status --porcelain --untracked-files=all`                                   | empty before the branch was created            |
| Branch           | `git switch -c separate-agents-md-core-and-subject-packs --no-track origin/main` | created from freshly fetched `origin/main`     |
| `AGENTS.md`      | `wc -c -l AGENTS.md`                                                             | `323938` bytes / `3362` lines                  |

Size by top-level section, measured from each `##` heading through the line before the next one; the rows sum to 323,926
bytes, and the 12-byte remainder is the title line and its blank line.

| Section                          |   Bytes | Lines |
| -------------------------------- | ------: | ----: |
| `## Gotchas`                     | 127,175 | 1,295 |
| `## OpenSpec Workflow Agreement` |  69,543 |   736 |
| `## Conventions`                 |  69,062 |   624 |
| `## Build & Test`                |  10,761 |   149 |
| `## Continuous Integration`      |  12,772 |   145 |
| `## Kubernetes Deployment`       |  10,671 |   124 |
| `## Local Development`           |   7,327 |    84 |
| `## Docker Images`               |   6,742 |    69 |
| `## Structured Writing Standard` |   4,147 |    56 |
| `## Architecture`                |   1,776 |    28 |
| `## Project Overview`            |   1,214 |    15 |
| `## Key Environment Variables`   |   1,185 |    15 |
| `## Prerequisites`               |   1,150 |    14 |
| `## Line Length`                 |     401 |     7 |

The pilot's two subjects are spread across `## Conventions` (the JVM and build rules), `## Build & Test` (the command
surface and test tiers), and `## Gotchas` (the build, runtime, and test subsections). A subject pack is therefore not a
section move; it is a **decision-scoped** move across three sections.

### Prior decisions recovered

Read from the archived extraction changes, the current `agent-skills` requirements, and the newest audit.

| Prior unit                                                        | What it settled, and its reason                                                                                                                                                                                                                                                                                                                        | How this design answers it                                                                                                                                                                                                                                                                                                                                                                                                      |
| ----------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `reduce-agents-md-loaded-surface` (2026-10-20, merged)            | Rejected nested `AGENTS.md` because "opencode traverses **up** from the working directory and the first match wins, so a root session never sees it"; rejected an `instructions` array because it "combines the listed files into every session"; deferred "converting always-needed procedures into skills" until a measurement established the need. | The nested-file premise is **outdated in the installed v2.1.1**: the instruction plugin collects every `AGENTS.md` along the ancestor chain, and a read inside a subtree loads that subtree's file (verified below). Nested files remain unsuitable for the JVM subject because no single directory contains it, but the recorded reason no longer holds as stated. The skill conversion's deferral asked for this measurement. |
| The same change's Non-Goals                                       | Excluded moving `## Build & Test`'s command surface, `## Key Environment Variables`, the `## OpenSpec Workflow Agreement`, and the gate-coupled deployment blocks.                                                                                                                                                                                     | A Non-Goal is that change's scope, not a finding. This unit moves only the JVM and Gradle parts of the command surface, keeps a verification entry point at root, and touches none of the other exclusions.                                                                                                                                                                                                                     |
| `add-extraction-procedure` (2026-10-19, merged)                   | A destination must be owned and discoverable **before** the decision; three "safe to load on demand" rules failed their retrieval paths and stayed.                                                                                                                                                                                                    | The packs are created by this change, owned (`agents-auditor`'s scope, the capture's destination list), and the retrieval path is probed rather than assumed. A pack that fails the probe bar is retained.                                                                                                                                                                                                                      |
| `trim-restated-agents-md-rules` (2026-10-11, merged)              | The owner deliberately kept the capture section's one-word promotion-gate list; "this restates the spec" does not by itself authorize deletion.                                                                                                                                                                                                        | Nothing is deleted for restatement. Every moved bullet travels verbatim, and no retained obligation is dropped for being short or repetitive.                                                                                                                                                                                                                                                                                   |
| `optimize-agents-auditor-for-instruction-budget` (2026-10-18)     | The instruction budget is measured, never capped; no threshold is configured.                                                                                                                                                                                                                                                                          | No cap, percentage target, or truncation is proposed. The figures below are measurements and stated estimates.                                                                                                                                                                                                                                                                                                                  |
| `separate-agents-md-operational-from-evidence` (2026-10-20)       | Evidence is kept per `## Gotchas` subsection under `docs/guidance-evidence/`; a destination-level pointer cannot prove the source↔destination pairing, so a per-rule `evidence:` token was used.                                                                                                                                                       | No pilot rule carries an `evidence:` token or a record section (verified by grep), so no record pairing breaks. The packs use a per-pack `Source:` line naming the replaced headings for the same pairing role, following the `ship-change` pattern; a future move of an evidence-recorded rule must retarget the pairing in its own change.                                                                                    |
| `load-stage-workflow-instructions-on-demand` (2026-10-20)         | A stage pack works when root keeps the trigger, the authorization boundary, the cross-stage ordering, and the missing-destination stop; the body loads on the trigger; the probe method tests the real text.                                                                                                                                           | This design generalizes the pattern from a **stage** trigger to a **subject** trigger, and keeps the same root floor: classification, stop, overlap, delegation, review, resume. Its probes are reused against the complete proposed root.                                                                                                                                                                                      |
| `consolidate-docs-update-obligations` (2026-10-21, merged)        | The documentation-update obligations have one authority; restatements point at it.                                                                                                                                                                                                                                                                     | The packs' `Source:` lines are the only source↔destination association; the docs-refresh convention stays the single owner of README/Javadoc duties.                                                                                                                                                                                                                                                                            |
| `extract-load-test-commands`, `extract-ci-narrative` (2026-10-19) | The audit's load-test and CI opportunities shipped; load-test content belongs to the load-tests spec and script, not to a new pack.                                                                                                                                                                                                                    | Load-test and CI content is **out of scope** and stays at root; no pack absorbs it.                                                                                                                                                                                                                                                                                                                                             |
| 2026-10-19 agent-tooling audit                                    | "No opportunity arises for `## Gotchas`: its entries are evidence-anchored incident memory whose subjects are cross-cutting meta rules an agent must see before starting work."                                                                                                                                                                        | The audit read the subsection as a unit. This design reads each bullet's **governed decision**: the Java/Gradle bullets govern a technical subject, while the workflow, git, evidence, and documentation bullets remain cross-cutting and stay. The audit's conclusion binds the latter, not these.                                                                                                                             |
| `make-captured-rules-traceable` (2026-09-18)                      | A `captured:` marker sits at the end of the rule it records; the pre-commit guard enforces placement.                                                                                                                                                                                                                                                  | The moved bullets keep their markers, and `verifyCapturedMarkers` already scans project-authored skills. Verified: 25 markers move, 25 appear in the drafts.                                                                                                                                                                                                                                                                    |
| `establish-structured-writing-standard` (2026-10-17)              | The mandatory is separated from the explanatory.                                                                                                                                                                                                                                                                                                       | The packs carry the operational rules; the root pointers carry only the trigger and the path.                                                                                                                                                                                                                                                                                                                                   |

### Parked ideas reconciled

`docs/ideas.md` was read at `4972e687`. The entries below constrain, overlap, or could be confused with this unit; the
remaining entries (git-tranche conversion, workflow triggers, review-loop levers, ADR gates, `git worktree`, product
phase, and the GitHub-surface entries) were read and are unrelated. **No entry is removed or implemented by this
change.**

| Parked idea                                                                    | Relationship                                                   | Disposition                                                                                                                                                                                                                                                                                                                                                                      |
| ------------------------------------------------------------------------------ | -------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Extend the extraction conditions so the retained pointer identifies each unit  | Constrains the association a new destination must carry        | **Applied locally**: each pack carries a `Source:` line naming the replaced root headings. The general condition in `agent-skills` stays parked; this change does not widen it beyond the subject-scope permission it needs. The entry's premise is refreshed at implementation, since the subject-scope class now carries the association it says the next class would inherit. |
| Carry the retrieval-path result in the audit ledger's "safe to load on demand" | Constrains how a destination claim is trusted                  | **Constraint applied**: every path below carries an evidence class (observed, verified, reasoned, proposed), and a probe that missed is reported rather than smoothed over.                                                                                                                                                                                                      |
| Convert the remaining Git-tranche Gotchas rules                                | Same file, different subject, explicitly excluded by the owner | **Not bundled.**                                                                                                                                                                                                                                                                                                                                                                 |
| Report Gradle's `--info` environment dump upstream                             | Sits beside the build gotchas the Gradle pack moves            | **Independent.** The rule stays with the moved build text; the upstream report is not touched.                                                                                                                                                                                                                                                                                   |
| Gate the `@NullMarked` package-info convention                                 | Beside the Java pack's NullAway rule                           | **Independent.** The pack moves the existing rule; it does not add a gate.                                                                                                                                                                                                                                                                                                       |
| Explain the gateway controller CT's load-sensitive hang                        | Beside the JVM testing pack's slice/load guidance              | **Independent.** Diagnostics are not moved into the pack.                                                                                                                                                                                                                                                                                                                        |
| Sharpen route-candidate verification                                           | The same pairing question                                      | **Constraint applied** through the `Source:` lines; the auditor clause change stays parked.                                                                                                                                                                                                                                                                                      |
| Trend the audit counts across reports                                          | Measures whether a reduction holds                             | **Not adopted**; this unit reports one measurement and does not build the series.                                                                                                                                                                                                                                                                                                |
| A durable home for a smoke-run's beyond-seed findings                          | A different durable-destination question                       | **Unrelated.**                                                                                                                                                                                                                                                                                                                                                                   |

### Verified loading behavior

Checked against the OpenCode documentation and the installed `opencode` v2.1.1 binary (its bundled JavaScript read with
`strings`; the read-trigger branch is explicit in `ReadTool` and `opencode.config.instruction`).

| Mechanism                         | Verified fact                                                                                                                                                                                                                                                 | Evidence class                                                           |
| --------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------ |
| Root `AGENTS.md`                  | Loaded into the session from the ancestor chain of the working directory (the plugin collects every `AGENTS.md` from the session directory up to the project root or home, not only the first).                                                               | Verified                                                                 |
| Nested `AGENTS.md`                | **Does load**: a `read` of a file inside a subtree loads every `AGENTS.md` from that file's directory up to the project root that the session has not loaded yet (`ReadTool` → `SessionInstructions.load`).                                                   | Verified                                                                 |
| `instructions` array              | Combined with `AGENTS.md` every session; it widens the always-loaded surface and cannot narrow it.                                                                                                                                                            | Verified (docs)                                                          |
| Skills                            | The name and description are listed in `<available_skills>` every session; the body loads when the `skill` tool is called or the file is read. `metadata["opencode/autoinvoke"] = "false"` hides a skill from that list while an exact-name call still works. | Verified                                                                 |
| Subagent definitions              | Loaded when the subagent is invoked; a subagent does not inherit a skill body the caller loaded. Whether a subagent session seeds the root file is not established here, so the reviewer instructions are placed in the **definitions**, not only at root.    | Definition verified; root seeding not verified                           |
| Project commands                  | Loaded when invoked; not an implicit routing mechanism.                                                                                                                                                                                                       | Verified (docs)                                                          |
| Context compaction                | The compaction prompt tells the summarizer to omit instruction files because they are supplied separately; a skill body is a tool result and is not re-supplied.                                                                                              | Verified prompt; re-supply for a continued session not directly observed |
| Nested `AGENTS.md` for this pilot | Unsuitable for the JVM subject: no directory contains it (18 JVM modules, one sibling web UI). It could host a `build-logic/` slice only, which is not the whole Gradle subject.                                                                              | Reasoned                                                                 |

The nested-file finding is why the prior design's rejection is not reused. The packs still load through an explicit
instruction because a nested file cannot scope a cross-module subject.

## Goals / Non-Goals

**Goals:**

- Move every obligation whose governed decision belongs to one technical subject into a destination that loads for that
  subject only, preserving every imperative, warning, exception, identifier, gate condition, and `captured:` marker.
- Keep at root what a task needs before it can classify itself: authorization, git and work-preservation invariants, the
  classification table, the missing-destination stop, and the genuinely cross-cutting rules.
- Make the retrieval path explicit, probed, and honest about its failure modes.
- Amend the placement policy explicitly rather than reinterpreting it.
- Close the maintenance loop: a capture lands in the owning pack, and the auditor can see the packs.

**Non-Goals:**

- A size cap, a percentage target, or a truncation mechanism.
- Moving the workflow, git, evidence, documentation, deployment, frontend, CI, or load-test material; the owner excluded
  the last four, and the first four are cross-cutting.
- Consolidating or shortening the moved rules; this is a relocation whose only edits are the six cross-reference
  retargets and the one sentence split listed below.
- A pre-approved partial variant; a failed positive control returns the unit to the owner with the root untouched.
- An aggregate savings claim; every load is reported per task class, with its assumptions.
- Redesigning the capture or audit system beyond the destination list and the placement permission.
- Implementing anything in this planning unit.

## Method and evidence classes

Every claim below carries one of four classes:

| Class    | Meaning                                                                                        |
| -------- | ---------------------------------------------------------------------------------------------- |
| Measured | A command over the repository produced the figure now stated.                                  |
| Verified | The mechanism or artifact was read (docs, binary, config) and states the behavior.             |
| Observed | A synthetic probe exercised the behavior; it is evidence for that harness, not for production. |
| Reasoned | Derived from the above by an arithmetic or an argument whose assumptions are stated.           |
| Proposed | A design choice or a test to run during implementation; not yet evidence.                      |

## Obligation inventory

Every pilot unit takes one disposition: **move** to a named pack, **retain** at root with its reason, or **route** to a
mechanism that already owns it. "Bytes" are the unit's own measured bytes. "Retained root" is what the root still says
after the move. Rows are grouped by pack; the retained table follows.

### Pack `jvm-development`

| Source anchor                                                                    | Governed decision                                                                   | Earliest point needed              | Applicable tasks            | Dependencies        | Home | Retained root instruction                     | Preservation risk                                                                          |
| -------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------- | ---------------------------------- | --------------------------- | ------------------- | ---- | --------------------------------------------- | ------------------------------------------------------------------------------------------ |
| `## Conventions` Lombok (2,408 B)                                                | How to declare a value type, builder, or local (`@Value`, `toBuilder`, `val`/`var`) | Before choosing or writing a type  | Java impl, proposal, review | None                | P1   | Classification row + `## Conventions` pointer | A skipped load writes boilerplate or a record; Spotless/Checkstyle do not catch the choice |
| `## Conventions` MapStruct (98 B)                                                | Component model for a mapper                                                        | Before writing a mapper            | Java impl                   | Lombok              | P1   | Pointer                                       | Low; a wrong model fails at runtime                                                        |
| `## Conventions` ErrorProne/NullAway (846 B)                                     | Package nullness, `@Nullable`/`@NonNull`, the suppressed false positive             | Before writing or compiling Java   | Java impl, dependency bump  | `package-info.java` | P1   | Pointer                                       | Low; `compileJava` catches most                                                            |
| `## Conventions` WebFlux blocking-execution routing (945 B)                      | Do not remove the gateway's scheduler routing as a BlockHound fix                   | Before editing gateway controllers | Java impl, review           | BlockHound          | P1   | Pointer                                       | Medium; error/validation outcomes regress; tests catch                                     |
| `## Gotchas → Runtime code` controller validation handler (622 B)                | Whether a controller-local validation handler fires                                 | Before adding a controller method  | Java impl, diagnosis        | Method validation   | P1   | Pointer                                       | Medium; `bodyErrors` silently lost                                                         |
| `## Gotchas → Runtime code` new gateway controller/endpoint registration (492 B) | Register a package in `springdoc` and the Vite proxy                                | Before adding an endpoint          | Java impl, frontend         | Two config files    | P1   | Pointer                                       | Medium; endpoint absent from docs/dev proxy                                                |
| `## Gotchas → Runtime code` NullAway strictness (186 B)                          | Which annotation a nullable field needs                                             | Before compiling                   | Java impl                   | —                   | P1   | Pointer                                       | Low; compile fails                                                                         |
| `## Gotchas → Runtime code` Jackson 3 artifacts (459 B)                          | Whether `tools.jackson.*` means a migration                                         | When reading the classpath         | Java impl, diagnosis, bump  | ADR-0003            | P1   | Pointer                                       | Low; a wrong conclusion starts a migration                                                 |
| `## Gotchas → Runtime code` Spring Data ES date format (571 B)                   | Why nanosecond timestamps truncate                                                  | When a date mapping is wrong       | Diagnosis, projection impl  | Pinned library      | P1   | Pointer                                       | Medium; "simplifying" the pattern reintroduces truncation                                  |
| `## Gotchas → Runtime code` sibling-component extraction (620 B)                 | What wiring to mirror when lifting a component                                      | Before extracting                  | Java impl                   | Sibling service     | P1   | Pointer                                       | High; missing upcaster chain/observation registry is silent                                |
| `## Gotchas → Runtime code` generic getter overload (786 B)                      | Which `String.valueOf` overload a generic call binds                                | When a cast fails at runtime       | Diagnosis                   | —                   | P1   | Pointer                                       | Medium; source reads correct                                                               |
| `## Gotchas → Runtime code` Caffeine `AsyncCache` (1,624 B)                      | How a failed/empty future and expiry behave                                         | Before adding a cache              | Java impl, diagnosis        | Reactor, Caffeine   | P1   | Pointer                                       | High; a failed future reads as a miss and an entry wedges                                  |

### Pack `jvm-testing`

| Source anchor                                              | Governed decision                                                           | Earliest point needed                      | Applicable tasks            | Dependencies                | Home | Retained root instruction    | Preservation risk                                       |
| ---------------------------------------------------------- | --------------------------------------------------------------------------- | ------------------------------------------ | --------------------------- | --------------------------- | ---- | ---------------------------- | ------------------------------------------------------- |
| `## Conventions` test display names (3,307 B)              | The class/method display-name convention, and the test-must-fail-first rule | Before writing a test                      | Test impl, review           | `check-and-test-authoring`  | P2   | Classification row + pointer | Medium; a display name or a vacuous guard passes review |
| `## Conventions` Spring bean mocks (261 B)                 | `@MockitoBean` rather than `@MockBean`                                      | Before writing a slice test                | Test impl                   | Spring Boot 3.4+            | P2   | Pointer                      | Low; compile/behavior catches it                        |
| `## Conventions` test tier placement (835 B)               | Which tier a test belongs to from its collaborators                         | Before writing a test                      | Test impl, proposal, review | Test tiers taxonomy         | P2   | Pointer                      | High; a wrong tier mocks the app's own wiring           |
| `## Conventions` nested resilience groups (289 B)          | `<Feature>Behavior` naming for `@Nested` groups                             | Before writing a resilience test           | Test impl                   | —                           | P2   | Pointer                      | Low; naming only                                        |
| `## Conventions` BlockHound jvmArgs (346 B)                | Which suites need the two `-XX` flags                                       | Before adding `BlockHound.install()`       | Test impl, build config     | Gradle suite config         | P2   | Pointer                      | Low; the suite fails loudly                             |
| `## Conventions` asserting log output (795 B)              | `OutputCaptureExtension` vs `withLogConsumer` by JVM boundary               | Before asserting logs                      | Test impl, diagnosis        | —                           | P2   | Pointer                      | Medium; the assertion silently captures nothing         |
| `## Conventions` `@DirtiesContext` (455 B)                 | When a full-context boot leaks global JVM state                             | Before adding the annotation               | Test impl                   | JGroups, JCache             | P2   | Pointer                      | Low; an unnecessary annotation slows tests              |
| `## Conventions` code coverage (736 B)                     | Where coverage is measured and why the gate needs Docker                    | Before running or reading the gate         | Test impl, verification     | `code-coverage-conventions` | P2   | Pointer                      | Medium; a Docker-free check hides a regression          |
| `## Build & Test` test suite order (374 B)                 | The `test → componentTest → integrationTest → e2eTest` order                | Before running tests                       | Test impl, verification     | Gradle suites               | P2   | Pointer                      | Low; the order is also in the build                     |
| `## Build & Test` test tiers (2,295 B)                     | The four tiers' collaborators, suffixes, and examples                       | Before choosing or running a test          | Test impl, proposal, review | Test tier placement         | P2   | Pointer                      | High; the taxonomy is the decision's basis              |
| `## Build & Test` test commands (443 B)                    | Which command runs which tier                                               | Before running a test                      | Test impl, verification     | Gradle suites               | P2   | Pointer                      | Low; the command is also in the build                   |
| `## Gotchas → Testing` gateway e2e images (765 B)          | The four images must exist before the gateway e2e suite                     | Before running the gateway e2e             | Verification                | `bootBuildImage`            | P2   | Pointer                      | Low; the suite fails at container start                 |
| `## Gotchas → Testing` `@Nested` with slice tests (342 B)  | A `@Nested` class loads the full context in a slice test                    | Before grouping slice cases                | Test impl                   | Spring test slices          | P2   | Pointer                      | Medium; the suite fails on infrastructure beans         |
| `## Gotchas → Testing` Testcontainers 2.0.5 import (371 B) | The post-migration `PostgreSQLContainer` import                             | Before writing or running a container test | Test impl, bump             | Testcontainers 2.0.5        | P2   | Pointer                      | Low; the old import compiles through a shim             |

### Pack `gradle-build`

| Source anchor                                                             | Governed decision                                                                                                                                                                                                | Earliest point needed                                               | Applicable tasks                    | Dependencies                                  | Home | Retained root instruction       | Preservation risk                                                                                  |
| ------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------- | ----------------------------------- | --------------------------------------------- | ---- | ------------------------------- | -------------------------------------------------------------------------------------------------- |
| `## Conventions` Checkstyle (205 B)                                       | Where the style gate and ruleset live                                                                                                                                                                            | When changing a style rule                                          | Build impl, review                  | `code-check-conventions`                      | P3   | Classification row + pointer    | Low; a wrong edit fails `check`                                                                    |
| `## Conventions` SpotBugs (206 B)                                         | The bug-finder configuration and filters                                                                                                                                                                         | When changing bug analysis                                          | Build impl                          | `code-check-conventions`                      | P3   | Pointer                         | Low                                                                                                |
| `## Conventions` LZ4 relocation (147 B)                                   | Why the root build substitutes `lz4-java`                                                                                                                                                                        | When touching the substitution                                      | Build impl, bump                    | ADR-0020                                      | P3   | Pointer                         | Medium; removing it reintroduces a CVE line                                                        |
| `## Conventions` build-tool versions in the catalog (1,158 B)             | That a convention plugin reads a version from `libs.versions.toml`                                                                                                                                               | Before changing a plugin/tool version                               | Build impl, bump                    | ADR-0015                                      | P3   | Pointer                         | Medium; a hard-coded version drifts silently                                                       |
| `## Conventions` convention plugin verified through a consumer (578 B)    | How to verify a plugin's effect                                                                                                                                                                                  | Before claiming a plugin works                                      | Build impl                          | No TestKit                                    | P3   | Pointer                         | High; a plugin change "verified" by reading the script                                             |
| `## Conventions` `-parameters` (50 B)                                     | That all `JavaCompile` tasks add the flag                                                                                                                                                                        | When changing compilation                                           | Build impl                          | —                                             | P3   | Pointer                         | Low                                                                                                |
| `## Gotchas → Build, Gradle & dependency management` (12,146 B, 10 rules) | Build-property naming, configuration cache, upstream plugin warnings, verdict outputs, npm overrides, tool transitives, custom suite classpaths, load-bearing dependencies, the Docker-free check, wrapper bumps | Before changing a build file, a task, or concluding a build failure | Build impl, bump, diagnosis, review | Catalog, plugin versions, ADRs 0015/0020/0021 | P3   | Pointer + the build-failure row | High; these are the repository's most expensive recurring failures, and a skipped load repeats one |
| `## Build & Test` `build` / `bootBuildImage` commands (176 B)             | Which command builds what                                                                                                                                                                                        | Before building                                                     | Build impl, verification            | —                                             | P3   | Minimal `check` command stays   | Low                                                                                                |
| `## Build & Test` Snyk scan and policy (994 B)                            | The scan command, policy path, and ignore-expiry sweep                                                                                                                                                           | Before a security scan                                              | Bump, security                      | Snyk CLI, `.snyk`                             | P3   | Pointer                         | Medium; a stale ignore passes silently                                                             |
| `## Build & Test` `dependencyUpdates` (237 B)                             | Which report covers catalog coordinates                                                                                                                                                                          | Before an update pass                                               | Bump                                | `/dependency-updates`                         | P3   | Pointer                         | Low                                                                                                |
| `## Build & Test` `buildLogicUpdates` (133 B)                             | Which report covers included-build pins                                                                                                                                                                          | Before an update pass                                               | Bump                                | `/dependency-updates`                         | P3   | Pointer                         | Low                                                                                                |
| `## Build & Test` build-environment constraints (1,345 B)                 | Why the report shows a spurious current version                                                                                                                                                                  | When reading the update report                                      | Bump                                | ADR-0021                                      | P3   | Pointer                         | Medium; a false row drives a wrong bump                                                            |
| `## Build & Test` dependency suppression lists (513 B)                    | Where majors and hold-backs are suppressed and why                                                                                                                                                               | Before suppressing or bumping                                       | Bump                                | `dependency-management` spec                  | P3   | Pointer                         | Medium; the parsing rule (`group:module`) is easy to lose                                          |
| `## Build & Test` calendar versioning (505 B)                             | When `YYYY.TRAIN` counts as a major                                                                                                                                                                              | Before classifying an update                                        | Bump                                | ADR-0021                                      | P3   | Pointer                         | Low                                                                                                |

### Retained at root, with the reason

| Source anchor                                                                                                  | Governed decision                                        | Why it stays                                                                                                                    |
| -------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------- |
| `## Conventions` no comments, SPDX header                                                                      | Every source file's shape, across languages              | Cross-cutting; a docs, frontend, or workflow task edits source too.                                                             |
| `## Conventions` Javadoc/JSDoc                                                                                 | Every declaration a task touches, in Java and TypeScript | Cross-language; moving it to P1 would drop the JSDoc half for frontend tasks until a frontend pack exists.                      |
| `## Conventions` formatting (`spotlessApply`, markdown, 120 columns)                                           | Every edit                                               | Cross-cutting and the canonical verification step; also the docs-only task's only build command.                                |
| `## Conventions` avoid redundancy                                                                              | Every code edit                                          | Language-agnostic code-quality rule.                                                                                            |
| `## Conventions` IDE inspections (optional)                                                                    | Any file                                                 | Cross-cutting and explicitly not a gate.                                                                                        |
| `## Conventions` ADRs, docs refresh, README conventions, OpenCode casing                                       | Every change                                             | Cross-cutting repository duties.                                                                                                |
| `## Conventions` frontend, diagrams, subagents, agent tooling, scratch paths                                   | Their own subjects, outside this pilot                   | The owner excluded those packs; retaining them avoids a half-move.                                                              |
| `## Structured Writing Standard`, `## Line Length`, `## Project Overview`, `## Architecture`                   | Before classifying or drafting anything                  | Global.                                                                                                                         |
| `## OpenSpec Workflow Agreement` (all)                                                                         | Every unit's lifecycle                                   | Global authorization, branch, review, capture, archive, and shipping rules; the stage pack already owns the shipping mechanics. |
| `## Gotchas → Git`, `Evidence and search`, `Documentation`, `Environment`, `Agent tooling`, `CI`, `Deployment` | Cross-cutting or excluded subjects                       | Needed across stages or before a subject is known.                                                                              |
| `## Gotchas → Testing` measurement/load rules (derived measurement, before/after pair, Gatling EL)             | Load-test work                                           | The load-tests subject is out of scope; these are not JVM unit-test rules.                                                      |
| `## Build & Test` load tests, web UI commands, Helm/npm suppression notes, DB scripts                          | Their subjects                                           | Load tests, frontend, deployment, and local development are out of scope.                                                       |
| The minimal `check` verification command                                                                       | Every implementation change                              | Needed before a task can know which pack to load, and the only build command a docs-only task needs besides `spotlessApply`.    |

**Routing, not moving:** the audit's `## Purpose` refresh, the `openspec/config.yaml` conventions block, and the README
technology list are not agent instructions. The config block is a second copy of the Java conventions; this change
points it at the packs rather than leaving two authorities (a task, not a spec delta).

### Documented edits

The move is verbatim except for the edits listed here. Each is listed so the preservation check can distinguish a
deliberate edit from a dropped sentence.

| Source sentence (in `AGENTS.md`)                                          | Pack                  | Edit                                                                                          |
| ------------------------------------------------------------------------- | --------------------- | --------------------------------------------------------------------------------------------- |
| "keep the coverage gate green, see Gotchas"                               | `jvm-testing`         | "see the coverage rule above and the coverage-gate gotcha in `gradle-build`"                  |
| "a test's tier is decided by its collaborators (see Test tiers)"          | `jvm-testing`         | "see the tiers below"                                                                         |
| "the gateway's and the web UI's; see Test tiers"                          | `jvm-testing`         | "see the tiers below"                                                                         |
| "see the Chart validation bullet above"                                   | `gradle-build`        | "see `AGENTS.md`'s `## Kubernetes Deployment` → Chart validation"                             |
| "see the config-read-path gotcha"                                         | `gradle-build`        | "see `AGENTS.md`'s `### Agent tooling, OpenCode & permissions`, the config-read-path bullet"  |
| "(see Continuous Integration)"                                            | `gradle-build`        | "(see `AGENTS.md`'s `## Continuous Integration`)"                                             |
| the `/dependency-updates`, `/gradle-update`, `/opsx-tool-update` sentence | `gradle-build` + root | split: the two report commands stay in the pack; the `/opsx-tool-update` clause stays at root |

The `jvm-testing` e2e-images bullet also inherits one 153-character line inside an inline code span from
`AGENTS.md:2540`. Prettier cannot wrap inside a span, and the line predates this change, so it is recorded as an
accepted inherited exceedance rather than a new one.

## Loading designs compared

| Design                                                                 | Root always-loaded                 | Added always-loaded metadata | Per-task load (Java prod / Java+test / Gradle / docs)           | Total maintained | Retrieval                                                             | Verdict                  |
| ---------------------------------------------------------------------- | ---------------------------------- | ---------------------------- | --------------------------------------------------------------- | ---------------- | --------------------------------------------------------------------- | ------------------------ |
| **A. Retention**                                                       | 323,938 B                          | 0                            | 323.9 / 337.9 / 345.3 / 323.9 KB                                | 323.9 KB         | None                                                                  | Baseline                 |
| **B. Root pointers to existing entry points**                          | ~322 KB                            | 0                            | 322 / 322 / 322 / 322 KB                                        | 322 KB           | No destination owns JVM/Gradle; specs are not loaded at the decision  | Rejected                 |
| **C. Two packs** (JVM combined, Gradle)                                | 290.3 KB                           | ~0.7 KB                      | 314.5 / 328.6 / 332.9 / 291.0 KB                                | ~334 KB          | One classification row per subject; a Java test writes production too | Viable, coarser          |
| **D. Three packs** (JVM production, JVM testing, Gradle) — recommended | 290.3 KB                           | ~1.1 KB                      | 302.0 / 329.0 / 333.3 / 291.4 KB                                | ~334 KB          | Three rows; production-only and diagnosis skip the testing pack       | Recommended              |
| **E. Nested `AGENTS.md`**                                              | 290.3 KB + the nested file on read | 0                            | Same as D once the directory is read; no cross-module JVM scope | Same             | Fires on a file read, not on a proposal-stage decision                | Complement, not a design |
| **F. `instructions` array**                                            | 323,938 B + the listed files       | 0                            | Grows every session                                             | Grows            | Always loaded                                                         | Rejected                 |

Design D's per-task figures **were** the planning estimates from the measurement section, not measurements of a running
session; the assumptions were one load per pack per session, the proposed root at 290.3 KB, the added metadata, and
every required reference. The final measured figures are 323,666 B for the mixed Java-and-build class and 350,520 B
(+3.7%) for the full class, in "Final measured complete loads" below; the planning figures remain historical.

**Why the three-pack split:** combining JVM production and testing (design C) makes every Java task load the 12.9 KB
testing pack even when it writes no test, losing the production-only and diagnosis saving. The split costs one more
classification row and one more description.

## Mandatory routing design

The root carries this contract (the complete text is in `drafts/root-routing.md`):

1. **Classify by the decision, not the file type.** A row fires when the task will _choose, change, execute, judge,
   interpret, or conclude_ something in the subject. Reading a build script to understand a module does not fire the
   Gradle row; adding a dependency, running `dependencyUpdates`, or reading its report does. For a proposal, classify by
   what the change will decide.
2. **Load before the first governed decision.** A governed decision is a choice, an edit, an execution, a review
   judgment, a report interpretation, or a diagnosis. Running an existing test or reading a dependency report is a
   governed decision, so its pack loads before the command or experiment is selected — not after its result is read. A
   moved warning can be what chooses the experiment: the integration-test/Docker prerequisite, the gateway e2e images,
   and the slice/`@Nested` constraint all decide which command or inspection is sound. The diagnostic boundary is the
   **first hypothesis or first diagnostic experiment**, not the conclusion: the moved runtime, test, and build rules
   choose the probe (which cast, which cache check, which rerun), so a probe they govern must not run first.
3. **State the classification before the first edit.** One `Packs:` line naming the packs loaded in full, or
   `Packs: none` with the reason. The probe that improved on the name-only instruction changed three things at once —
   the explicit path, the decision-scoped trigger, and this line — so it does not isolate this line's effect. Task 2.4
   is deferred as independent research: the line **reports** the classification, the fixture suite verifies the actual
   read, and the line-alone effect remains unattributed. The line alone is not accepted as proof.
4. **Overlap.** Every obligation lives in exactly one pack; a pack that needs another's rule names it. When two rows
   match, both load, and the more specific rule governs within its own obligation.
5. **Delegation.** A delegated agent does not inherit the caller's pack; the caller names every applicable pack in the
   prompt.
6. **Resume.** After compaction or a resumed session, re-classify and reload before the next governed decision. This is
   a residual reliance: the compaction prompt drops instruction files and a tool-result pack body is not re-supplied.
7. **Missing destination.** Stop and report; never proceed from memory or from the root pointers. Loading a pack grants
   no permission, and a skipped load cannot authorize an action.

### Review classification and the bounded input contract

A changed-file classifier misses the cases that matter: a Markdown-only proposal choosing a JVM implementation, a
dependency-policy proposal with no catalog edit, or documentation asserting a test-tier or runtime behavior. The
reviewer therefore classifies the **decisions and claims in the supplied artifacts** — the proposal, design, delta
specs, and diff — using changed paths as evidence rather than the sole predicate.

The reviewer input contract is preserved rather than widened, for both passes:

- The caller classifies the change and **names the applicable packs in the hand-over**, alongside the artifacts and
  checks it already names, and instructs the reviewer to follow its definition's input contract.
- The reviewer loads each named pack in full. Reading a named file is not a repository search, so it stays inside the
  supplied list.
- A subject evident in the supplied artifacts but absent from the hand-over is reported as a **missing input**; the
  reviewer checks **every** matching row, not only the packs the hand-over names, so a named pack does not cover a
  second subject. That report is the reviewer's only output, it does not search for a rule, and the caller re-invokes
  with the pack named.
- The reviewer's report states which packs it loaded, so the screen's scope remains verifiable from the report.

`review-thorough` and the `/review-thorough` command carry the same classification and missing-input stop in their input
lists, so a deep pass does not depend on the implementer's session either.

## Representative task validation

Each row states the route from the root to every required obligation, the stage at which the load must have happened,
the evidence class, and the fixture assertion. **Stage-aware, not whole-task:** a production change may later need a
test, so `jvm-testing` is expected at the first test decision, not banned from the task. "Unrelated" means a pack whose
subject the task never reaches.

| Task                                                                  | Route and earliest required load                                                                                                                         | Expected by stage                                                | Evidence                                | Fixture assertion                                                      |
| --------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------- | --------------------------------------- | ---------------------------------------------------------------------- |
| Java implementation, no explicit testing request                      | `jvm-development` before the first production decision; `jvm-testing` before a test is chosen, written, or run; `gradle-build` only for a build decision | P1 at stage 1; P2 only at a test stage; P3 only at a build stage | Reasoned; probe E (no visible JVM read) | P1 read before the first edit; no P2/P3 read unless their stage starts |
| Test-only change                                                      | `jvm-testing` + `check-and-test-authoring` before the first test decision                                                                                | P2 + C before the test decision                                  | Reasoned; probe D                       | P2 read before the first test edit; C read before authoring            |
| Spring runtime diagnosis, no edits                                    | `jvm-development` before the first hypothesis or diagnostic experiment                                                                                   | P1 before diagnosis                                              | Reasoned                                | P1 read before the first diagnostic command                            |
| Dependency bump                                                       | `gradle-build` + the `dependency-management` spec before running or reading a report                                                                     | P3 + DM before the report                                        | Observed (probe C); reasoned            | P3 read before the first bump command                                  |
| Build-logic convention change                                         | `gradle-build` before changing the plugin; `jvm-development` only if it writes JVM production code                                                       | P3 before the change                                             | Reasoned                                | P3 read before the first build-script edit                             |
| Gradle failure initially reported as an application defect            | `jvm-development` for the stack trace; `gradle-build` before the first build hypothesis or diagnostic experiment                                         | P1 first; P3 before the first build hypothesis or command        | Reasoned; **residual risk**             | The first build hypothesis/command is preceded by a P3 read            |
| Run the existing integration tests                                    | `jvm-testing` before selecting the command (Testcontainers/Docker prerequisite, tier, suite order)                                                       | P2 before the command                                            | Reasoned                                | P2 read before the first `integrationTest` invocation                  |
| Check coverage                                                        | `jvm-testing` and `gradle-build` before running or reading the gate (coverage rule, Docker-free form, gate configuration)                                | P2 + P3 before the gate (observed)                               | Observed (preflight C)                  | Both packs read before the first gate command                          |
| Run the gateway end-to-end suite                                      | `jvm-testing` before the command (four service images, separate opt-in task)                                                                             | P2 before the command                                            | Reasoned                                | P2 read before the first `e2eTest` invocation                          |
| Explain a test failure                                                | `jvm-testing` before the first hypothesis or diagnostic experiment (tier, slice/`@Nested`, log capture)                                                  | P2 before the first experiment                                   | Reasoned                                | P2 read before the first diagnostic command                            |
| Read the dependency-update report                                     | `gradle-build` before interpreting it (build-environment constraints, suppression lists, calendar versioning)                                            | P3 before the interpretation                                     | Reasoned                                | P3 read before the first classification of a report row                |
| Task discovering a second subject midway                              | The root rule loads the new pack at the discovery, before the next decision in that subject                                                              | One pack at each stage                                           | Reasoned                                | A Java change that edits the catalog reads P3 before the edit          |
| Proposal requiring technical constraints before implementation        | Classify by what the change will decide; load before the design choice                                                                                   | P1 and/or P3 before the decision                                 | Reasoned                                | A proposal that adds a dependency reads P3 while drafting              |
| Fresh reviewer, Java + test diff                                      | The hand-over names P1 and P2; the reviewer loads them from the supplied artifacts, with paths as evidence                                               | P1 + P2 + DQ before the judgment                                 | Verified definition; observed gap       | Reviewer reads both packs before the first finding                     |
| Fresh reviewer, Markdown-only proposal choosing a JVM implementation  | The hand-over names P1; changed paths are not the predicate                                                                                              | P1 + DQ before the judgment                                      | Reasoned                                | Reviewer reads P1 although no Java file changed                        |
| Fresh reviewer, dependency-policy proposal without a catalog edit     | The hand-over names P3                                                                                                                                   | P3 + DQ before the judgment                                      | Reasoned                                | Reviewer reads P3 although no build file changed                       |
| Fresh reviewer, documentation asserting test-tier or runtime behavior | The hand-over names P2 or P1 by the claim, not the path                                                                                                  | The claimed subject's pack + DQ                                  | Reasoned                                | Reviewer reads the pack before judging the claim                       |
| Purely editorial task / review                                        | No row matches; root's Formatting covers the edit                                                                                                        | No pack                                                          | Observed (probe B)                      | No pack read; `Packs: none` states the reason                          |
| Resumed or compacted session                                          | Root re-classification rule → reload before the next decision                                                                                            | Same as the task's current stage                                 | Reasoned; residual                      | Compaction mid-change is followed by a reload                          |
| Documentation-only task                                               | No row matches; formatting only                                                                                                                          | No pack                                                          | Observed (probe B)                      | No pack read                                                           |

**Acceptance criteria** — the probe suite applies these consistently:

- A required pack read **before** its stage's first decision — for a diagnosis, before the first hypothesis or
  diagnostic experiment — is a pass.
- A required pack read only **after** that decision is a failure, even when the line reports it.
- A pack read before any decision in its subject is **excess**: it is recorded, and its bytes are charged against the
  task's measured benefit. Excess is not a preservation failure.
- The `Packs:` line is compared with the observed reads; a line without a read is a failure of reporting, not evidence
  of loading.
- A task whose excess makes its complete load at least today's complete load loses its benefit; the result is reported,
  not hidden.
- The suite runs in a disposable clone carrying the complete drafted root. A positive-control failure stops the unit
  before any root deletion (see "Partial implementation").

**Positive controls (must load):** the Java, test, dependency, build-logic, integration-run, coverage, gateway-e2e,
failure-explanation, report-reading, and four reviewer rows. **Negative controls (must not load):** editorial and
docs-only. **Stage-aware controls:** the Java, misreported-failure, and discovered-subject rows, which must load their
later pack at their stage rather than at the start or never.

## Observed retrieval probes

Six probes ran in a throwaway git repository under `$TMPDIR/opencode/subject-pack-probe` against `opencode` v2.1.1,
`opencode run --standalone --auto --model opencode-go/deepseek-v4.1-flash`. The fixture carried a ~1.5 KB synthetic
root, a `Foo.java`/`libs.versions.toml`/`README.md` tree, and **two** token-bearing packs: `jvm-development` and
`gradle-build`. The `jvm-testing` pack did not exist in the fixture, so no probe exercises the test-only route. They are
**synthetic**: the real root is 324 KB and a production session faces far more competing context.

| Probe | Instruction strength                             | Request                           | Visible reads and token echo                                                                                                        | Reading                                                      |
| ----- | ------------------------------------------------ | --------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------ |
| A     | Pack name only; read/design/write/review trigger | Add a field to `Foo.java`         | No pack call and no pack read; edited from the description's gist and ran the root's verification command                           | **Missed load** on the dominant class                        |
| B     | Same                                             | Fix a README typo                 | No pack read; stated that none applies                                                                                              | Correct negative control                                     |
| C     | Same                                             | Bump the catalog version          | Read `gradle-build/SKILL.md`, echoed its token, and reasoned explicitly that `jvm-development` does not apply                       | Correct load                                                 |
| D     | Explicit path + decision trigger + `Packs:` line | Add a unit test                   | Read `jvm-development/SKILL.md` (the only JVM pack in the fixture); echoed both tokens; no `gradle-build` read is visible           | JVM classification exercised; the second token is unverified |
| E     | Same                                             | Add a field to `Foo.java`         | Read `gradle-build/SKILL.md`; echoed both tokens; explained the Gradle read came from the verification step; no JVM read is visible | A pack was reached, but not the required JVM pack            |
| F     | Same                                             | Read the catalog version, no edit | Read `gradle-build/SKILL.md`; token echoed                                                                                          | Over-load on a read-only question                            |

**What the probes establish:** a name-only instruction missed every pack on a Java edit. Under the explicit-path design,
every tested edit produced a visible read of at least one pack, the negative controls held, and the Gradle case reached
its required pack. The Java-edit case did **not** show a visible read of its required pack, and a token was echoed
without a read. The probes therefore support _reachability_ under the explicit instruction, not _accuracy_ of the
subject selection; the implementation-time fixture suite is the authority.

**What they do not establish:** production compliance, behavior under a 324 KB root, reviewer-session behavior, the
test-only route (the fixture had no `jvm-testing` pack), or the execution and semantic-review cases (no probe exercised
them). The suite in the validation table runs against the complete proposed root and covers those cases.

## Measurement

Three quantities are kept apart: the **shared baseline** (the proposed root plus the always-listed metadata every
session carries), the **task body** (the packs and required references a task loads), and the **maintained content**
(the files the repository keeps anywhere). Loads are absolute bytes; a delta compares the same task's complete load
today and after the change. The harness system prompt is not in the repository and is excluded; every reference a task
already had to read is counted on both sides, so it cancels in the delta but stays visible in the total.

### Components (planning measurement, historical)

**Planning measurement (historical).** These values were measured at `4972e687` from the drafts; the final operands are
in "Final measured complete loads".

| Component                                                                    | Bytes              | Basis                                                                              |
| ---------------------------------------------------------------------------- | ------------------ | ---------------------------------------------------------------------------------- |
| Current root `R0`                                                            | 323,938            | `wc -c`                                                                            |
| Proposed root `R1` (routing, pointers, capture edits; measured in the clone) | 290,298            | `wc -c` on the clone's proposed `AGENTS.md`                                        |
| Root reduction `R1 − R0`                                                     | −33,640 (−10.4%)   | Arithmetic                                                                         |
| Added always-loaded metadata `M` (3 skill entries + agent description)       | 1,109 (estimate)   | 1,034 skill entries (326/313/395) + 75 agent delta; derivation in the claims table |
| `jvm-development` `P1`                                                       | 10,603             | `wc -c`                                                                            |
| `jvm-testing` `P2`                                                           | 12,914             | `wc -c`                                                                            |
| `gradle-build` `P3`                                                          | 20,501             | `wc -c`                                                                            |
| `check-and-test-authoring` `C` (pre-existing)                                | 14,032             | `wc -c`                                                                            |
| `review-quick` definition `DQ` (pre-existing)                                | 13,585             | `wc -c`                                                                            |
| `dependency-management` spec `DM` (pre-existing)                             | 21,373             | `wc -c`                                                                            |
| Three pack drafts (maintained)                                               | 44,018             | `wc -c`                                                                            |
| `openspec/config.yaml` context: its two restating lists replaced             | −987 (1,813 → 826) | Measured; supporting surface, not the session root                                 |
| Total maintained `R1 + drafts`                                               | 334,316 (+3.2%)    | Arithmetic                                                                         |

**Assumptions, stated:** one load per pack per session; the reviewer's definition is loaded once per review session; the
`dependency-management` spec is read once per update pass; a task does not re-read a reference it already loaded. No
aggregate saving is claimed: the table below is a per-class model, not a measurement of production sessions.

### Representative complete loads (planning estimate)

**Planning estimate (historical).** The values below were computed before implementation from the drafts and the
proposed root; they are not final measurements. The final operands and table are in "Final measured complete loads".

`Baseline` is `R1 + M` (291,407 B). `Body` is every pack and reference the task must read. The delta compares the same
body plus today's root, so `C`, `DQ`, and `DM` appear on both sides where they already applied.

| Task class                                                     | Baseline | Body                      | Total now | Total today | Delta            |
| -------------------------------------------------------------- | -------: | ------------------------- | --------: | ----------: | ---------------- |
| Documentation-only / editorial                                 |  291,407 | —                         |   291,407 |     323,938 | −32,531 (−10.0%) |
| JVM production only, or runtime diagnosis                      |  291,407 | P1 10,603                 |   302,010 |     323,938 | −21,928 (−6.8%)  |
| Run integration tests, run gateway e2e, explain a test failure |  291,407 | P2 12,914                 |   304,321 |     323,938 | −19,617 (−6.1%)  |
| Coverage-gate interpretation                                   |  291,407 | P2 12,914 + P3 20,501     |   324,822 |     323,938 | +884 (+0.3%)     |
| Test-only change (authors a test)                              |  291,407 | P2 12,914 + C 14,032      |   318,353 |     337,970 | −19,617 (−5.8%)  |
| JVM production + tests                                         |  291,407 | P1 + P2 + C = 37,549      |   328,956 |     337,970 | −9,014 (−2.7%)   |
| Dependency bump                                                |  291,407 | P3 20,501 + DM 21,373     |   333,281 |     345,311 | −12,030 (−3.5%)  |
| Dependency-report interpretation                               |  291,407 | P3 + DM = 41,874          |   333,281 |     345,311 | −12,030 (−3.5%)  |
| JVM + build, no tests                                          |  291,407 | P1 + P3 = 31,104          |   322,511 |     323,938 | −1,427 (−0.4%)   |
| JVM + tests + build                                            |  291,407 | P1 + P2 + P3 + C = 58,050 |   349,457 |     337,970 | +11,487 (+3.4%)  |
| Review, Java + test diff (quick)                               |  291,407 | DQ + P1 + P2 = 37,102     |   328,509 |     337,523 | −9,014 (−2.7%)   |
| Review, Markdown-only proposal choosing a JVM implementation   |  291,407 | DQ + P1 = 24,188          |   315,595 |     337,523 | −21,928 (−6.5%)  |
| Review, dependency-policy proposal without a catalog edit      |  291,407 | DQ + P3 = 34,086          |   325,493 |     337,523 | −12,030 (−3.6%)  |
| Review, documentation asserting a test-tier behavior           |  291,407 | DQ + P2 = 26,499          |   317,906 |     337,523 | −19,617 (−5.8%)  |
| Editorial review                                               |  291,407 | DQ 13,585                 |   304,992 |     337,523 | −32,531 (−9.6%)  |

**The planning estimate's reading:** the root was estimated to fall 10.4%, but no task's complete load fell by that
amount once its required references were counted. The single-subject classes were estimated at 3.5–6.8%; the mixed
JVM-and-build class at 0.4%; the full three-pack class rose 3.4%, and coverage-gate interpretation was at parity (+0.3%)
because it loads P2 and P3. These are historical estimates, not final measurements.

### Final measured complete loads

The operands were re-measured from the final working tree after the last candidate correction (`review-thorough.md`).
`R1` and the three packs are measured; `M` remains the explicit estimate; `C`, `DQ`, and `DM` are measured as the files
the task loads.

| Operand                           | Planning estimate (historical) | Final measurement | Basis                                                                        |
| --------------------------------- | -----------------------------: | ----------------: | ---------------------------------------------------------------------------- |
| Root `R1`                         |                        290,298 |           291,750 | `wc -c AGENTS.md`                                                            |
| Always-loaded metadata `M`        |               1,109 (estimate) |  1,109 (estimate) | 1,034 skill entries + 75 B agent delta; the overhead is not measurable       |
| Baseline `R1 + M`                 |                        291,407 |           292,859 | Arithmetic; `M` is estimated                                                 |
| `jvm-development` `P1`            |                         10,603 |            10,529 | `wc -c`                                                                      |
| `jvm-testing` `P2`                |                         12,914 |            12,822 | `wc -c`                                                                      |
| `gradle-build` `P3`               |                         20,501 |            20,278 | `wc -c`                                                                      |
| `check-and-test-authoring` `C`    |                         14,032 |            14,032 | `wc -c`                                                                      |
| `review-quick` definition `DQ`    |                         13,585 |            14,932 | `wc -c`; the planning value predates the reviewer-contract edits             |
| `dependency-management` spec `DM` |                         21,373 |            21,373 | `wc -c`                                                                      |
| Three final packs                 |                44,018 (drafts) |            43,629 | `wc -c .opencode/skills/{jvm-development,jvm-testing,gradle-build}/SKILL.md` |
| Root reduction                    |               −33,640 (−10.4%) |   −32,188 (−9.9%) | 291,750 vs `git show 4972e687:AGENTS.md \| wc -c` = 323,938                  |
| Maintained root + final packs     |                334,316 (+3.2%) |   335,379 (+3.5%) | Arithmetic over the final root and packs                                     |

`Total today` compares the same body with the pre-change files: the non-review classes use the 323,938 B root (plus
`C`/`DM` where they apply), while the review classes use the pre-change `DQ` 13,585, so their final delta includes the
review definition's 1,347 B growth. The complete-load totals are a model, not a session measurement.

| Task class                                                     | Baseline | Body (final)              | Total final | Total today | Delta final | Delta % |
| -------------------------------------------------------------- | -------: | ------------------------- | ----------: | ----------: | ----------: | ------: |
| Documentation-only / editorial                                 |  292,859 | —                         |     292,859 |     323,938 |     −31,079 |   −9.6% |
| JVM production only, or runtime diagnosis                      |  292,859 | P1 10,529                 |     303,388 |     323,938 |     −20,550 |   −6.3% |
| Run integration tests, run gateway e2e, explain a test failure |  292,859 | P2 12,822                 |     305,681 |     323,938 |     −18,257 |   −5.6% |
| Coverage-gate interpretation                                   |  292,859 | P2 12,822 + P3 20,278     |     325,959 |     323,938 |      +2,021 |   +0.6% |
| Test-only change (authors a test)                              |  292,859 | P2 12,822 + C 14,032      |     319,713 |     337,970 |     −18,257 |   −5.4% |
| JVM production + tests                                         |  292,859 | P1 + P2 + C = 37,383      |     330,242 |     337,970 |      −7,728 |   −2.3% |
| Dependency bump                                                |  292,859 | P3 20,278 + DM 21,373     |     334,510 |     345,311 |     −10,801 |   −3.1% |
| Dependency-report interpretation                               |  292,859 | P3 + DM = 41,651          |     334,510 |     345,311 |     −10,801 |   −3.1% |
| JVM + build, no tests                                          |  292,859 | P1 + P3 = 30,807          |     323,666 |     323,938 |        −272 |   −0.1% |
| JVM + tests + build                                            |  292,859 | P1 + P2 + P3 + C = 57,661 |     350,520 |     337,970 |     +12,550 |   +3.7% |
| Review, Java + test diff (quick)                               |  292,859 | DQ + P1 + P2 = 38,283     |     331,142 |     337,523 |      −6,381 |   −1.9% |
| Review, Markdown-only proposal choosing a JVM implementation   |  292,859 | DQ + P1 = 25,461          |     318,320 |     337,523 |     −19,203 |   −5.7% |
| Review, dependency-policy proposal without a catalog edit      |  292,859 | DQ + P3 = 35,210          |     328,069 |     337,523 |      −9,454 |   −2.8% |
| Review, documentation asserting a test-tier behavior           |  292,859 | DQ + P2 = 27,754          |     320,613 |     337,523 |     −16,910 |   −5.0% |
| Editorial review                                               |  292,859 | DQ 14,932                 |     307,791 |     337,523 |     −29,732 |   −8.8% |

**The final measured reading.** The root reduction is **measured**: 291,750 B vs 323,938 B = **−32,188 B (−9.9%)**. The
metadata `M` (1,109 B) is an **estimate**, so the baseline and every complete-load total are arithmetic over measured
files plus that estimate, not measurements of production sessions. The single-subject classes fall 5.1–6.3% (coverage
interpretation is +0.6% because it loads P2 and P3); JVM + build is −0.1%; JVM + tests is −2.3%; the full class is
+3.7%; the review classes are −1.9% to −8.8%. The measured root reduction is not a task's saving: the metadata estimate
and the per-task arithmetic are separate effects, and no aggregate saving is claimed.

**Excess loading:** a pack read before its subject's first decision but never used is charged in full. If a task's
excess makes `Total now` at least `Total today`, its benefit is gone; that is reported per class, not hidden behind the
root figure.

**Duplication introduced:** the three descriptions (always listed), the root routing table, the pointers, and the
`Source:` lines. **Duplication removed:** the moved bullets are single copies; the `openspec/config.yaml` context's
Conventions and Build & test lists are pointed at the packs, removing a second copy of the moved rules; the root's
`## Build & Test` restates the build file less than before. **Not changed:** the README's technology list and the specs'
operational references. The README's Testing section (lines 737–753) remains a human-facing summary, not the agent's
authority; the docs-refresh convention keeps it in step, and this change names it rather than rewriting it.
**Maintenance cost:** three destinations in the auditor's scope, the capture's fourth destination, the reviewer
hand-over, and the upstream scan. **Retrieval risk:** the classification is prose; the probes show reachability but not
accuracy, and the implementation-time suite is the gate.

## Scratch preflight

The complete proposed set was exercised in a disposable clone (OpenCode v2.1.1, `opencode-go/deepseek-v4.1-flash`)
before approval. `preflight.md` records every fixture's input, observed reads and order, the two reviewer failures and
their fix, the compaction test, and the revised figures. Its corrections are carried here: the diagnostic boundary is
the **first hypothesis or diagnostic experiment**; coverage-gate interpretation loads `jvm-testing` **and**
`gradle-build` (parity, not the earlier −6.5%); the reviewer pre-check is the definition's **first** instruction, and a
review does not verify a proposal's premise by searching. Actual compaction was exercised through the server API and the
agent reloaded both packs before its next edit.

## Claims table

Each fact this change states about its evidence, with the source or command that derives it. Rows were re-run at
`4972e687` after the last draft edit. A row about a live artifact anchors to that artifact's state, so it stops
reproducing once the change lands.

| Claim                                                                      | Source or command                                                                                                                                                                                                                           |
| -------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The preceding consolidation change is merged at `HEAD`                     | `git log -1 --format=%s` → `consolidate-docs-update-obligations: … (#653)`                                                                                                                                                                  |
| Baseline revision `4972e687…` and an empty tree before the branch          | `git rev-parse HEAD`; `git status --porcelain --untracked-files=all`                                                                                                                                                                        |
| `AGENTS.md` is 323,938 B / 3,362 lines                                     | `wc -c -l AGENTS.md`                                                                                                                                                                                                                        |
| The section table sums to 323,926 B (plus a 12-byte title/blank remainder) | `python3` heading-range scan over `##` headings; each row runs to the line before the next heading                                                                                                                                          |
| The moved spans are 39,678 B (12.2%)                                       | `python3` sum over the verbatim ranges named in the inventory tables, plus the 443 B test-command block                                                                                                                                     |
| The four capture edits add 228 B                                           | `python3` replacement of the four capture passages against `wc -c`                                                                                                                                                                          |
| The three drafts are 10,603 / 12,914 / 20,501 B (44,018)                   | `wc -c drafts/jvm-development.md drafts/jvm-testing.md drafts/gradle-build.md`                                                                                                                                                              |
| 25 `captured:` markers move and 25 appear in the drafts                    | `python3` count over the moved ranges against the three draft files                                                                                                                                                                         |
| Four `owner/repo#NNN` references move                                      | `python3` regex `[a-z0-9-]+/[a-z0-9-]+#\d+` over the drafts → error-prone#5855, gradle-helm-plugin#140/#145, gradle-versions-plugin#755                                                                                                     |
| No moved rule carries an `evidence:` token or a record section             | `grep -c 'evidence:'` over the moved spans → 0; `grep` of `docs/guidance-evidence/` for the moved subjects → no match                                                                                                                       |
| The proposed root is 290,298 B (−33,640, −10.4%)                           | `wc -c` on the clone's proposed `AGENTS.md`, built from the drafts; the diagnostic-boundary wording and reviewer pre-check followed                                                                                                         |
| Added always-loaded metadata is 1,109 B (estimate)                         | Three entries of 326 / 313 / 395 B (descriptions 231 / 222 / 303 and names 15 / 11 / 12, plus an assumed 80 B listing overhead each) + 75 B lesson-capture description delta (estimate); the overhead is not measurable from the repository |
| The total maintained content is 334,316 B (+3.2%)                          | 290,298 + 44,018 (three drafts)                                                                                                                                                                                                             |
| The config context's two lists shrink by 987 B                             | `python3` byte sum over `openspec/config.yaml` lines 22–48 against the drafted replacement                                                                                                                                                  |
| The planning complete-load table (historical)                              | Arithmetic over the planning `R1`, `M`, drafts, `C` 14,032, `DQ` 13,585, and `DM` 21,373; superseded by the final-measurement rows below                                                                                                    |
| The test-command block is 443 B                                            | `python3` byte sum over `AGENTS.md` lines 840–850                                                                                                                                                                                           |
| The OpenSpec context limit is 51,200 bytes                                 | `.opencode/skills/openspec-propose/SKILL.md:63` ("no larger than 51,200 bytes in UTF-8")                                                                                                                                                    |
| Six cross-references and one sentence are the only edits                   | The "Documented edits" table above; each source sentence and its replacement                                                                                                                                                                |
| The capture destinations are stated at four `AGENTS.md` sites              | `AGENTS.md` lines 508–517 (step 2, step 3, merge-time) and 532–534 (decision point)                                                                                                                                                         |
| `lesson-capture.md` states destinations at eight sites                     | Its frontmatter, opening, "You are given", destination paragraph, "name the existing rule", do-not-edit, retirement report, and verdict line                                                                                                |
| `review-quick.md`'s bounded input contract is preserved                    | Its "inspected what it is given" paragraph; the packs enter as named hand-over inputs, not a search                                                                                                                                         |
| The README names `AGENTS.md` as the capture destination at three sites     | `README.md` lines 377, 391–394, 450–458                                                                                                                                                                                                     |
| The auditor definition has no subject-pack class                           | `.opencode/agent/agents-auditor.md` class table and condition list                                                                                                                                                                          |
| The parked extraction-condition entry's premise is partly stale            | `docs/ideas.md` lines 32–37 ("the next destination class inherits the gap")                                                                                                                                                                 |
| v2.1.1 loads every ancestor `AGENTS.md` and a subtree file on read         | `strings /opt/homebrew/bin/opencode` → `opencode.config.instruction` (`up({targets:["AGENTS.md"]…})`) and `ReadTool` → `SessionInstructions.load`                                                                                           |
| A skill body loads on the `skill` call; `opencode/autoinvoke` hides it     | `https://opencode.ai/docs/skills/`; the same binary parses `metadata["opencode/autoinvoke"]`                                                                                                                                                |
| Compaction omits instruction files and re-supplies them separately         | The binary's compaction prompt: "Leave out instructions and setup … The next agent receives current versions of all of these separately"                                                                                                    |
| The probe results in the probe table                                       | Six `opencode run --standalone --auto --model opencode-go/deepseek-v4.1-flash` runs in `$TMPDIR/opencode/subject-pack-probe`; transcripts not committed; the fixture had two packs and no execution case                                    |
| The preflight environment is v2.1.1 with `opencode-go/deepseek-v4.1-flash` | `opencode --version`; the clone's `.opencode/opencode.json`                                                                                                                                                                                 |
| The fixture reads and order are as recorded in `preflight.md`              | The transcripts under `$TMPDIR/opencode/preflight/logs/*.txt`, parsed for `Skill`/`SKILL.md` reads and the first edit/command                                                                                                               |
| The missing-pack failure was fixed and re-verified                         | `G.txt`/`G2.txt`/`G3.txt` (no missing-input report, no P1) against `G4.txt` (`missing-input`, stop, no search)                                                                                                                              |
| Actual compaction completed and the pack reload followed                   | `POST /api/session/<id>/compact`; the export's two `compaction` records (one `failed`, one `completed`) and the post-compaction run's `Skill jvm-development` + `Skill jvm-testing` before its first edit                                   |
| The prior-decision quotes                                                  | Each archived `design.md` at the path named in the prior-decisions table                                                                                                                                                                    |
| The parked ideas and their dispositions                                    | `docs/ideas.md` at `4972e687`, entries by title                                                                                                                                                                                             |
| `openspec/config.yaml` restates the Java conventions                       | `openspec/config.yaml` lines 22–29                                                                                                                                                                                                          |
| The README technology list is not a rule copy                              | `README.md` lines 231–233 (technology names only)                                                                                                                                                                                           |
| The repository has 18 JVM modules plus the web UI                          | `AGENTS.md` `## Project Overview`; `README.md` project structure                                                                                                                                                                            |
| The 2026-10-19 audit's Gotchas conclusion                                  | `docs/audits/2026-10-19.md`, "No opportunity arises for `## Gotchas` …"                                                                                                                                                                     |
| The current placement policy's stage limit                                 | `openspec/specs/showcase/quality/agent-skills/spec.md` lines 762–768                                                                                                                                                                        |

**Scan candidates dismissed by kind.** `scripts/claim-scan.py` produced candidates in four kinds; each kind is dismissed
for one reason rather than line by line.

| Candidate kind | Dismissal reason                                                                                                                                                                                                                                                                                        |
| -------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `absolute`     | A prose qualifier (`only`, `every`, `never`) in a rule statement or a risk sentence, not a claim about a corpus; the substantive universal claims are carried by the inventory tables and the span sum, each derived above.                                                                             |
| `path`         | A repository path or a command string cited as the location of a rule or an artifact; the substantive paths are derived by the config, README, and audit rows.                                                                                                                                          |
| `count`        | A figure, line number, or round/class count owned by the artifact that records it: a byte/line/marker figure derived above or in the final-measurement rows, a line number in the preflight table, or a round/class/session field in the review ledger. None is an independent claim about this change. |
| `cross-ref`    | A slash-joined shorthand naming a file pair or a concept pair (`README/Javadoc`, `Java/Gradle`, `slice/load`), not a cross-reference to a capability, ADR, or spec.                                                                                                                                     |

### Post-implementation measurement claims

The rows above are the planning claims; these are the final-measurement claims, re-derived from the working tree after
the last candidate correction.

| Claim                                                                                                                        | Source or command                                                                                                                                                                                                       |
| ---------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The final root is 291,750 B                                                                                                  | `wc -c AGENTS.md`                                                                                                                                                                                                       |
| The final packs are 10,529 / 12,822 / 20,278 B (43,629 total)                                                                | `wc -c .opencode/skills/{jvm-development,jvm-testing,gradle-build}/SKILL.md`                                                                                                                                            |
| `C` is 14,032 B, `DQ` is 14,932 B, `DM` is 21,373 B                                                                          | `wc -c .opencode/skills/check-and-test-authoring/SKILL.md .opencode/agent/review-quick.md openspec/specs/showcase/quality/dependency-management/spec.md`; the planning `DQ` 13,585 predates the reviewer-contract edits |
| The final baseline is 292,859 B                                                                                              | 291,750 (measured) + 1,109 (the explicit `M` estimate); `M` is not a measurement                                                                                                                                        |
| The measured root reduction is −32,188 B (−9.9%)                                                                             | `wc -c AGENTS.md` = 291,750 against `git show 4972e687:AGENTS.md \| wc -c` = 323,938                                                                                                                                    |
| The final complete-load table is arithmetic, not a session measurement                                                       | The "Final measured complete loads" table; each total is the baseline plus the named measured files, with the estimated `M`                                                                                             |
| The final maintained root + packs is 335,379 B (+3.5%)                                                                       | 291,750 + 43,629 against 323,938                                                                                                                                                                                        |
| The ablation was not run; the loads are not attributed to the `Packs:` line                                                  | Tasks 2.4; the observed first reads in `preflight.md`; the design's routing item 3                                                                                                                                      |
| Task 2.2's observed fixtures are A–I, D2, GT, and R1/R4; the reasoned classes are not claimed as observed                    | `preflight.md` fixture table; the design's "Representative task validation" table                                                                                                                                       |
| The final snapshot matches the candidate files                                                                               | The reversal manifest's post-images; the final sandbox's 16/16 byte-identity check                                                                                                                                      |
| The acceptance-gap coverage table records every required case, its fixture/probe, final-candidate status, and observed reads | `preflight.md` "Acceptance-gap controls"; the logs under `$TMPDIR/opencode/acceptance-gaps/logs/`                                                                                                                       |
| The new final-candidate controls are J2b, K, L, M, N2, O2b, P, Q2c, and O3/F2b/G4b/GT2                                       | `preflight.md`; the logs; the template's 16/16 byte identity                                                                                                                                                            |
| The O partial-handover stop miss was corrected and re-verified                                                               | `review-quick`/`review-thorough` pre-checks; the delta; O3/G4b/GT2 stop, F2b loads P1, O2b loads P3                                                                                                                     |
| The auditor control reported subject-pack merge/removal but no positive extraction; owner decision outstanding               | `AUD`/`AUD2`/`AUD3`–`AUD5` logs; `tasks.md` 3.5                                                                                                                                                                         |
| Task 3.8 is closed as a process deviation, not reconstructed                                                                 | `tasks.md` 3.8; the current manifest 16/16 and sandbox 16/16                                                                                                                                                            |
| `openspec validate --all` reports 27 passed                                                                                  | The command's output; the change delta and all specs validate                                                                                                                                                           |
| The config file is 10,060 B, under the 51,200-byte context limit                                                             | `wc -c openspec/config.yaml`; the limit row above                                                                                                                                                                       |
| The Docker-free and full `check` runs pass                                                                                   | `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` and `./gradlew check`; task 4.2's records                                                                                                                     |

## Policy amendment

The current `agent-skills` placement policy says:

> a global safety, branch, review, or repository-wide workflow **invariant** … stays at root `AGENTS.md`, while that
> workflow's stage-specific execution mechanics … MAY move to a project-owned destination only where the retained root
> entry point requires reading that destination completely before the governed decision. This permission is limited to a
> stage whose trigger is that decision: it does not extend to guidance needed across more than one stage or before a
> stage can be identified …

A subject pack is **not** a stage pack: its trigger is the task's subject, and it is needed across the proposal,
implementation, review, and diagnosis stages. Moving it under the current text would be a reinterpretation. The delta
therefore adds an explicit **subject-scope destination class** with its own conditions:

- the subject is narrower than the repository and named by a root classification table;
- the root table names the trigger, the path, and the missing-pack stop;
- the pack loads in full before the first governed decision in the subject, where a governed decision includes an
  execution and a report interpretation, not only an authoring choice;
- a task outside the subject can complete without it;
- every extraction condition (preservation, ownership, retrieval, net effect) still holds;
- a cross-cutting safety, workflow, git, evidence, or documentation rule is not eligible — it stays at root.

The review classification (the decisions and claims under review, with changed paths as evidence) is a separate ADDED
requirement, not a condition of the placement permission. The delta carves the subject case out of the "across more than
one stage" exclusion in its own sentence, so the capability-mechanism and warning-inference exclusions stay
unconditional. It extends the capture's destination list and the audit's ledger to the packs. It does **not** create a
catch-all guidance directory, a nested file, or an `instructions` entry.

## Risks / trade-offs

| Risk                                                            | Mitigation                                                                                                                          | Residual                                                                                                          |
| --------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------- |
| A skipped pack silently drops a convention                      | Explicit path, decision-scoped trigger, `Packs:` line, reviewer/delegate rules, implementation-time fixtures with positive controls | Prose is not a gate; a model can still skip. A missed pack is reported by the `Packs:` line's absence             |
| Over-loading makes mixed tasks pay the scaffolding back         | Three packs, not one; excess is charged per class in the complete-load table                                                        | The full three-pack class is +3.4%; mixed is −0.4%                                                                |
| Compaction drops the pack body                                  | Root re-classification rule; packs are re-readable                                                                                  | The root itself is re-supplied by the harness; the pack is not; a resumed task may act until it re-reads          |
| A reviewer judges without the pack                              | The caller names the packs in the hand-over; the definitions carry the classification and the missing-input rule                    | A manually invoked reviewer outside the definitions is not covered; an unlisted subject is reported               |
| The policy amendment widens beyond this pilot                   | The permission is named, conditioned, and accompanied by scenarios; the delta does not permit a catch-all                           | A later change could over-read it; the auditor and the review gate check it                                       |
| The packs accrete or duplicate the check-and-test skill         | One owner per obligation; the capture names the owning pack; the auditor reports duplication                                        | The display-name bullet's authoring clause overlaps the skill's invariant and is flagged below                    |
| The moved upstream references fall out of the report            | Extend `upstreamReferences` to every project-authored skill, excluding generated and vendored ones                                  | The exclusion is provenance-based and must stay in step with the auditor's scope                                  |
| A fixture side effect escapes the sandbox                       | No remote; worktree/session-move/`git worktree` denied; the sandbox and its sessions are removed together                           | Anything inside the sandbox is removed with it; an out-of-sandbox write is recorded and reported for owner review |
| A reversal cannot tell this unit's edits from unrelated changes | Explicit manifest with pre-image hashes; restore only matching paths; stop for owner review on any ambiguity                        | An out-of-manifest edit halts the reversal rather than risking a user's work                                      |
| A completion claim rests on a pre-correction snapshot           | Snapshot hashes plus the invalidation table; the claim is made only against the final snapshot (step 11)                            | A correction outside the table is a review defect, not a silent stale snapshot                                    |
| `Source:` lines go stale when a pack is reorganized             | The auditor's dead-reference class; the capture's destination names the pack                                                        | Manual, not gated                                                                                                 |
| The packs' content drifts from the build files or the specs     | The existing docs-refresh and auditor duties cover the packs; the moved rules already point at the authorities                      | The packs are new maintainable surface                                                                            |

## Final-state validation and reversal

No partial variant is pre-approved. **"Frozen" means the final verification snapshot** — the candidate state after the
last relevant correction — not a prohibition on fixing findings. The suite tests the final state, never a mutating
fixture on the real working tree; the real tree is verified read-only.

The order follows the review and capture lifecycle:

1. Create the packs and the supporting edits in a scratch clone, build the complete proposed root there, and run the
   validation fixtures (execution, review, second-subject discovery) and the `Packs:`-line ablation.
2. When every positive control passes, write the **reversal manifest** before the first edit and apply the real-tree
   edits in its order.
3. Record the **initial candidate hashes** — the post-apply state against which a correction's invalidation is measured.
4. Run the **implementation quick review to its clean state** (session continuity), and fix its findings.
5. Run the **capture** and apply its durable proposals; fix any findings they raise.
6. Re-run the bounded review over the corrections; stop when clean.
7. Run `spotlessApply` on the changed files as the **last write**; after it the candidate is frozen for verification.
8. Build the final-state sandbox from the final candidate files, verify each candidate is byte-identical to its real
   counterpart, and run the **full** mutating suite once against it.
9. Verify the real tree read-only; a failed check that would change a candidate leaves the read-only phase.
10. Run the deep review; a finding that changes a candidate takes the thaw path below.
11. Make the completion claim only against the snapshot after the last thaw.

**Post-snapshot corrections (thaw).** IF a deep-review finding or a read-only check changes a candidate after the final
snapshot, THEN fix it, re-run the bounded review, format the changed files, re-hash the affected candidates, rebuild the
sandbox, and rerun the checks the change affects (table below). The completion claim uses the refreshed snapshot; no
claim relies on a snapshot that predates a relevant correction.

### Snapshot invalidation

**Candidate files** are the implementation artifacts whose content can change behavior: `AGENTS.md`, the three packs,
the reviewer definitions, the `/review-thorough` command, `lesson-capture.md`, `agents-auditor.md`, the
`openspec/config.yaml` context, `README.md`, `docs/ideas.md`, and the `upstreamReferences` change (`build.gradle.kts`,
`UpstreamReferencesTask.kt`, `UpstreamReferenceScope.kt`, and its test). **Review records and result notes** —
`docs/review-rounds.tsv`, `tasks.md` progress notes, and the change-dir proposal/design/preflight — are not candidates.

| Correction                                                              | Invalidates      | Rerun                                                                       |
| ----------------------------------------------------------------------- | ---------------- | --------------------------------------------------------------------------- |
| root routing/table, a pack body, a reviewer definition, a recovery rule | that file's hash | retrieval fixtures; the read-only checks that read it                       |
| `lesson-capture.md` or `agents-auditor.md`                              | that file's hash | the capture/auditor definition checks and the read-only checks that read it |
| config context, README, ideas                                           | that file's hash | the docs/consistency checks; measurement if sizes change                    |
| `build.gradle.kts` or its test                                          | that file's hash | the upstream test and the build checks                                      |
| a ledger row, a task note, a change-dir note                            | nothing          | nothing — a ledger append alone does not repeat the behavioral suite        |

A file's snapshot match is its final SHA-256; a completion claim never relies on a snapshot that predates a relevant
correction.

**Fixture containment.** The sandbox has no git remote and denies `git worktree`, the worktree tools, and session-move
in its config. Any session or worktree a fixture creates is recorded and removed with the sandbox; a fixture may not
write outside `$TMPDIR`.

**Reversal.** The manifest records a modified path with its pre-image and post-image hashes, and a **created** path with
"no pre-image", its post-image hash, and `delete` as its reversal. IF the implementation must be reversed, THEN act only
on manifest paths, and only where the current content is the unit's post-image; leave a file carrying unrelated edits
untouched; preserve `docs/review-rounds.tsv` and the change dir; stop for owner review when a path is outside the
manifest or its ownership is ambiguous. `git status` is not a rollback ownership manifest — it cannot distinguish this
unit's edits from a concurrent or pre-existing change.

IF a pack misses a positive control at step 1, THEN the unit stops with the real root untouched. The owner reviews the
failing fixture, the pack, and a proposed reduced boundary — which packs move, which stay, and the re-derived root
table, pointers, capture destinations, reviewer hand-over, upstream scope, and measurements. A reduced unit re-enters
the review gate with its own amendments to this change's delta before implementation restarts.

No obligation is deleted before its destination is verified, so a failed retrieval test cannot leave deleted text routed
to a pack that does not ship. The approved unit is the full three-pack design; a reduced unit is a new owner decision,
not a silent implementation choice.

**The display-name bullet.** Its authoring clause ("A test written to guard a refactor must fail against the
pre-refactor code") sits beside the `check-and-test-authoring` skill's invariant. It is **not** copied from the skill —
the bullet predates it and adds the "run it before the change" procedure — so it moves verbatim, and the pack points at
the skill rather than restating its modes. The review may still judge it a duplicate; if so, the correction is to keep
the procedure in the pack and drop the restatement of the invariant.

## Decision

**Recommendation: design D, the three-pack subject-scope unit, gated by a probe-first implementation and an owner return
on failure.**

The constructed root reduction is 33,640 B (10.4%). Complete-load effects: −10.0% for docs-only, −6.8% for JVM
production/diagnosis, −5.8% for test-only, −2.7% for JVM production-plus-tests, −3.5% for Gradle, −0.4% for a mixed
JVM-and-build change, +3.4% for a change that needs all three, and +0.3% for coverage-gate interpretation. The review
classes fall 2.7–9.6%. The last row is the honest cost: a task that enters every subject pays the scaffolding back. The
scratch preflight loaded every required pack before its decision in the tested paths; the one missed pack (a reviewer
hand-over that omitted it) was fixed and re-verified, and actual compaction reloaded both packs. The implementation-time
suite remains the gate.

The unit is **conditional at implementation**: each pack runs the fixture suite against the complete proposed root. A
pack that misses a required load on its positive control stops the unit **before any root text is deleted**, and the
owner reviews a reduced boundary. No partial variant ships automatically; the "Partial implementation" section owns the
rule.

**Why not retention:** the complete design does not fail its preservation test — every obligation has a destination and
a copy, and the observed failure is a routing failure that the explicit instruction improves in the probe; the
implementation-time suite decides whether it is fixed. **Why not a smaller pilot first:** moving one subject leaves the
other subjects' content at root: the class that needs the moved pack pays the scaffolding back while only the classes
outside it save, so the smallest unit that improves both subject classes is the pair, and the third pack adds the
production-only, diagnosis, and test-only skips at one row's cost. A one-pack pilot would demonstrate the mechanism but
not the benefit.

**What would change this recommendation:** a probe against the complete root that misses a positive control after the
explicit routing, or a preservation audit that finds an obligation with no destination. Either outcome returns the unit
to the owner with the failing evidence and a proposed reduced boundary; nothing is deleted or re-routed in the meantime.

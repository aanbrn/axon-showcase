# Design

## Context

The build enables the Gradle configuration cache (`org.gradle.configuration-cache=true`, `gradle.properties`), so a task
action may not reference anything belonging to the Gradle script — `project`, `gradle`, `rootProject`, `providers`, or a
script-level `val`/`fun` — because serializing the captured script object fails the store.
`UpstreamReferencesTask.report()` read `project.rootDir` to relativize each source file into the citation label the
report renders, which is exactly that reference.

#581 (`6a65ad25`) enabled the cache on 2026-10-15. The `upstream-references` workflow's last successful run was
2026-10-10; its 2026-10-17 scheduled run failed in 36 seconds at `./gradlew upstreamReferences`, reporting "1 problem
was found storing the configuration cache" — this task's `Task.project` access — and discarding the entry. The task is
not a member of `check`, and the workflow is observational, so no gate saw it.

`merge-governance`'s "Upstream-reference report runs on a schedule and on demand" already requires the report to run and
to update the issue. The code violated that requirement, so this is code→spec alignment and no delta is owed.

## Goals / Non-Goals

**Goals:**

- Restore the task's configuration-cache compatibility without changing the report's content.
- Establish that no other first-party task in the build carries the same defect.

**Non-Goals:**

- No change to the workflow, its steps, or its notification behaviour.
- No change to the report's format, the extraction pattern, or the issue-update logic.
- No new gate: an empirical cache-store sweep over the tasks the workflows invoke is parked in `docs/ideas.md` rather
  than built here, because it needs its own delta and a control.

## Decisions

**The root directory becomes an explicit task property, not an injected `ProjectLayout`.** Alternatives considered: an
injected `ProjectLayout` (no registration change, but it relies on an injected service's project semantics that would
need verifying); a construction-time `val` (works, but hides the dependency and departs from the shape the other tasks
use); a map of file-to-label computed in the registration (more code for the same result). Chosen: an
`@get:Internal DirectoryProperty` set from the registration, which is how every other task in this build receives its
inputs and which needs no unverified assumption.

**The property is `@Internal`, not `@InputDirectory`.** It is the label base, not content: declaring the repository root
as an input would make the task's input set unusable, and the task re-runs unconditionally anyway — its verdict comes
from the GitHub API rather than from its inputs, so its registration carries `outputs.upToDateWhen { false }`, the
treatment the repository's other external-state verdict tasks take.

**The workflow is left unchanged.** Its only Gradle step is `./gradlew upstreamReferences`, so the task fix is the whole
remedy; editing the workflow would add a change with no behaviour to gain.

**No committed test guards this fix, and the reason is recorded rather than worked around.** `build-logic` has no
`gradleTestKit()`, so a task action cannot be exercised from its `src/test`; the verification is therefore the two runs
(a stored entry, then a reused one) plus the workflow dispatch, not a unit test.

## Risks / Trade-offs

- **A task no gate runs can carry this defect unnoticed, and the sweep here is static.** The static sweep covers every
  first-party task class and every `doLast`/`doFirst` body; it cannot prove anything about third-party plugin tasks
  beyond what their pinned versions document. The empirical sweep — running each task the workflows invoke with the
  cache enabled and requiring a stored entry — is parked as its own unit.
- **The report's content could drift if the two bases differed.** Both are the repository root: the registration already
  collects the sources with `fileTree(rootDir)`, and the labels are root-relative in both the pre-fix output (issue
  #343's body) and this run's report.

## Migration Plan

None. The report's content is unchanged, so nothing downstream adjusts; the next scheduled run or a dispatch picks the
fix up. The dispatch also refreshes the stale "Upstream references" issue, which is a live defect this change corrects
rather than merely records.

## Claims table

| Claim                                                                                                                                                                              | Source                                                                                                                                                                                                                                                                                                                                                                                     |
| ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| The store fails on `Task.project` at execution time, failing the run in 36 seconds, and it is the only problem reported                                                            | CI run log `39069456525` (`gh run view 39069456525 --log`, `gh run list` for the duration); a local `./gradlew upstreamReferences` printing "Configuration cache entry discarded with 1 problem."                                                                                                                                                                                          |
| The cache was enabled on 2026-10-15, after the workflow's last success on 2026-10-10                                                                                               | `git log -S org.gradle.configuration-cache -- gradle.properties` (`6a65ad25`, 2026-10-15); `gh run list --workflow=upstream-references.yml`                                                                                                                                                                                                                                                |
| `UpstreamReferencesTask` was the only first-party task action reading `project`                                                                                                    | grep of `build-logic/src/main/kotlin/*.kt` and `build.gradle.kts` for `project.`/`rootProject`/`gradle.`/`this.project`, each hit classified as configuration-time (a registration or convention-plugin lambda) or execution-time (a `@TaskAction` body or a `doLast`/`doFirst` closure); one hit in the latter class, the fixed line; the `doLast`/`doFirst` body of every `*.gradle.kts` |
| The report's labels are unchanged, both bases being the repository root                                                                                                            | issue #343's body, written by the pre-fix code, and `build/upstream-references/report.txt` from this run — both root-relative (`AGENTS.md:NNN`, `docs/adr/….md:NNN`); the registration's `fileTree(rootDir)`                                                                                                                                                                               |
| `merge-governance` already requires the report to run and to update the issue, and states the workflow is not a merge gate                                                         | `git show HEAD:openspec/specs/showcase/quality/merge-governance/spec.md` (the "Upstream-reference report runs on a schedule and on demand" requirement)                                                                                                                                                                                                                                    |
| No capability spec covers the configuration cache                                                                                                                                  | `grep -rln "configuration cache" openspec/specs/` — no match                                                                                                                                                                                                                                                                                                                               |
| The "Upstream references" issue was last refreshed on 2026-10-10                                                                                                                   | `gh issue list --search 'in:title "Upstream references" is:issue'` (issue #343, `updatedAt` 2026-10-10)                                                                                                                                                                                                                                                                                    |
| The task re-runs unconditionally by design                                                                                                                                         | `git show HEAD:build.gradle.kts` (the registration's `outputs.upToDateWhen { false }`)                                                                                                                                                                                                                                                                                                     |
| The workflow's only Gradle step is this task                                                                                                                                       | `git show HEAD:.github/workflows/upstream-references.yml`                                                                                                                                                                                                                                                                                                                                  |
| The other recent CI failures are different causes, not this class                                                                                                                  | `gh run list` since 2026-10-15: `audit.yml` fails on an expired OpenCode API key (`AI_APICallError`, run `39062356142`); the Deployment Smoke and Dependency Security failures each have a later success                                                                                                                                                                                   |
| Task classes are declared only under `build-logic/src/main/kotlin/`, and the build's only `doLast`/`doFirst` closures are six, all in that directory's `*.gradle.kts`              | the `tasks.register`/`tasks.withType` grep across every `*.gradle.kts`; the `doLast`/`doFirst` grep across the same set                                                                                                                                                                                                                                                                    |
| `build-logic` has no `gradleTestKit()`, so its `src/test` cannot exercise a task action                                                                                            | `grep -rn gradleTestKit build-logic/` — no match; `AGENTS.md`'s "a `build-logic` convention plugin's effect is verified through a consuming module" bullet                                                                                                                                                                                                                                 |
| The change's source edits touch only `UpstreamReferencesTask.kt` and `build.gradle.kts`                                                                                            | `git diff --name-only -- '*.kt' '*.kts'`                                                                                                                                                                                                                                                                                                                                                   |
| The build's task classes take their inputs as `@get:` properties their registrations assign, and its external-state verdict registrations declare `outputs.upToDateWhen { false }` | `git show HEAD:build.gradle.kts` (the registrations declaring `outputs.upToDateWhen { false }`, and the property assignments for `VerifyDocumentedPinsTask`, `VerifyInfraImageVersionsTask`, and `ToolingUpdatesTask`); `AGENTS.md`'s verdict-task bullet                                                                                                                                  |

**Dismissals** (the scan's candidates resolved to neither a row nor a fact, one reason per kind): **path, command, and
cross-reference candidates** (`[path]`, `[cross-ref]`) are names, paths, or `#NNN` references to a source the adjacent
row already names, not claims about the repository; **count candidates** (`[count]`) are a duration or a problem count
read from the run log the adjacent row names, or the change's own task and section numbering and its `Done` notes'
command results, which report a command's outcome rather than a fact about the repository; **absolute-quantifier
candidates** (`[absolute]`) are modal, structural, or scope statements where they appear — a `None` in a Capabilities
section, an `only`/`never`/`every` in a rule or a scope sentence — while a universal claim the change makes about the
repository carries its own row above rather than a dismissal; **the `docs/review-rounds.tsv` candidates** are this
unit's own appended record rows — a round number and a class label in a one-line record, not a claim; **the change dir's
own artifacts** are the text this change authors, so their quantifiers are the content being written rather than claims
about the repository.

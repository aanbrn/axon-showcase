# Design

## Context

Seventeen project-authored command definitions sit under `.opencode/commands/`; six generated `opsx-*` commands and the
generated/vendored skills are out of scope. `AGENTS.md` states what each of them owes — the Structured Writing Standard,
the change lifecycle, and the review gate — and the `agent-skills` spec owns their behavior. Three facts drove this
change:

- **The apply paths stop short of the review gate.** Four of the five apply-finding commands end at "run
  `spotlessApply`, then re-run `review-quick`"; `/audit-specs` applies nothing inline, already routing an accepted fix
  to the change workflow. `AGENTS.md`'s review gate requires the pre-write pass and its claims table, the routing
  decision, a `REQUIRED` deep review, the capture decision, and the manual pass.
- **No mutating command states the lifecycle it owes.** The audits and the reduction unit edit `AGENTS.md`, the specs,
  the docs, and agent definitions, yet none says the unit starts on its own branch, leaves work uncommitted, or waits
  for an explicit approval before committing or pushing.
- **Two commands assert what they cannot reproduce.** `/dependency-security-check` states a Snyk plan's quota, its
  billing rule, and its reset schedule — external facts that rot, and which the repository's own rules forbid asserting
  without a current authoritative source. `/gradle-update` reads "the report (or the report file at
  `build/dependencyUpdates/report.txt`)", a fallback that serves a previous run's report when the current one failed.

## Goals / Non-Goals

**Goals**

- Bring every in-scope command to the authoring standard, with labeled sections where the workflow is complex.
- Make each mutating command state the lifecycle it owes and the review gate it runs, by pointing at the authority.
- Make every `review-quick` invocation carry the session-continuity preflight.
- Repair the verified inconsistencies, and delete the claims that cannot reproduce.

**Non-Goals**

- Changing any command's trigger, or the behavior its script, subagent, or skill owns.
- Editing the six generated `opsx-*` commands, the generated `openspec-*` skills, or the vendored `axon4to5-*` skills.
- Weakening `/diagram`'s visual-review path, `/review-thorough`'s session and pass-budget semantics, or the reduction
  unit's candidate/opportunity distinction.
- Adding a per-command copy of the review gate's _steps_ or of another source's _semantics_. A command states the
  imperatives a reader must act on — its trigger, its safety conditions, its lifecycle — and points at the workflow or
  definition that owns the procedure.

## Decisions

### Only a complex workflow gets the full labeled-section set

`AGENTS.md` requires labeled sections "for complex workflows". Applying all seven labels to a ten-line command such as
`/setup-idea` would add headings that carry one line each — structure without content — so the small commands get a
scannable bullet list and the complex ones (`Trigger`, `Prerequisites`, `Procedure`, `Decision points`,
`Failure handling`, `Verification`, `Completion criteria`, used as the command needs them) get the labels.

- **Chosen:** the labeled set for a command whose workflow branches, mutates files, or runs a lifecycle; a short bullet
  list otherwise.
- **Rejected:** one uniform template. It bloats the small commands and invites headings with nothing under them.

### The lifecycle and the review gate are pointed at, not restated

Both are `AGENTS.md`'s — "Workflow: Change lifecycle" and "Workflow: The review gate" — and the session-continuity
preflight is that procedure's step 3. The distinction is between an imperative and a procedure: a warning a reader must
act on before the step that would touch it is stated wherever the step appears, since a pointer that drops it loses a
rule, while the procedure's steps are pointed at, since a per-command copy of them is a second statement of a rule the
repository already single-sources and the copies would drift.

- **Chosen:** each mutating command states the imperatives a reader must act on (its own branch from a freshly fetched
  `origin/main`; unrelated dirty changes preserved; work uncommitted; no commit, push, pull request, archive, or merge
  without the explicit approval; the full review gate) and names the workflow that owns the detail.
- **Rejected:** copying the review gate's steps into each command. The repository's single-sourcing rule forbids it, and
  the copy is the surface that goes stale.

### An audit's findings are delivered through the lifecycle, and the no-op case stops

The four audit commands and the reduction unit present findings and then apply them, which conflates reporting with
delivery: a unit that mutates files owes a branch, a route per finding, and the review gate. A report with nothing
actionable must stop rather than create an empty unit, and findings whose routes or scopes differ are separate units.

- **Chosen:** report generation ends at the presentation and the owner's selection; each approved finding is routed by
  its kind — a spec change through an OpenSpec change with a delta, a code or definition change through the normal
  change workflow, a reduction candidate per its own kind — and the apply path runs the full gate.
- **Rejected:** applying findings inline as the audits do today. It ships unreviewed edits to `AGENTS.md` and the specs,
  and it batches findings the routing keeps apart.

### The dependency reports report only the current run

An update report is a point-in-time read of live registries. A stale file left by an earlier run, or a task failure read
as "no updates", inverts the finding.

- **Chosen:** derive from the current successful invocation; treat a failed or indeterminate run as indeterminate;
  report a task failure apart from an available update; surface the web UI task's own error and exit evidence
  (`build/npm-outdated.err.txt`, `build/npm-outdated.exit`) when it reports a failure.
- **Rejected:** reading the report file after a failed run. It answers a question about the past.

### The Snyk quota paragraph is deleted rather than corrected

The command stated an external plan's quota, the billing rule that consumes it, and a reset schedule. Those are facts
about a third-party service, they rot, and the repository's rule for such a surface is to cite a current authoritative
source at the time of the claim — which a command definition cannot do.

- **Chosen:** operational guidance only — report the two scans separately, note a rate-limit message as an operational
  condition rather than a finding, and verify any quota statement from the provider at the time it matters.
- **Rejected:** updating the numbers. A corrected figure rots exactly as the old one did.

### The tool-update probe is collision-safe and restores what it touches

`/opsx-tool-update` exercises the config probe by mutating `openspec/config.yaml` and creating a probe change. A fixed
probe name collides with a real change of the same name, a failed control can leave the config edited, and a control
whose edit no-opped reads as a passing check.

- **Chosen:** a unique probe name per invocation, removal of only the probe this invocation created, restoration of the
  prior `config.yaml` on success, failure, and interruption, and a per-control assertion that the source text changed
  before the diagnostic is read.
- **Rejected:** the fixed name and the in-place control edits. Both leave state behind on an interrupted run.

### A safety confirmation is stated even where the skill owns the procedure

`/setup-agent-tools` asks for confirmation before it reads or edits the owner's global OpenCode configuration, or runs
an external installer — an imperative a reader must act on before the step that would touch it, so the command states
it. The skill owns the procedure and the points at which it asks; the command does not restate those.

- **Chosen:** one confirmation imperative in the command, with the skill as the procedure's authority.
- **Rejected:** leaving it to the skill alone. A command that runs an installer without stating the confirmation invites
  the step before the reader reaches the authority.

### The workflow pin inventory is derived, not listed

`toolingUpdates` declares each pin's workflow file as a `ToolingUpdateCheck`, and it fails when a declared file is
missing. A command that lists the three `openspec`-carrying workflows by hand is a second copy of that declaration, and
a fourth workflow gaining the pin would leave it stale.

- **Chosen:** derive the inventory from `toolingUpdates`' declared checks — each `ToolingUpdateCheck` in
  `build.gradle.kts` names its `workflowFile`, and the task's report names only the checks with an available update —
  and keep the pin update a targeted replacement.
- **Rejected:** the hand-written list of three files.

## Risks / Trade-offs

- **The restructuring is large and touches every command.** A reader who relied on the old numbering may misread a
  command, so each edit preserves the command's trigger, its authorities, and its report-only or mutating character.
- **A pointer can drop an imperative.** The lifecycle and preflight sections state the imperatives explicitly and point
  only for the detail, since a pointer that loses a warning loses a rule.
- **The delta states behavior the commands implement.** The three new requirements and the widened scenario are the
  contract; a command that stops honoring one is drift the review can see.

## Claims table

| #   | Claim                                                                                                                                                                             | Source                                                                                                                                                                                                        |
| --- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | Seventeen project-authored commands are in scope, and six generated `opsx-*` commands are excluded.                                                                               | `ls .opencode/commands/` (23 files; the six `opsx-apply/archive/explore/propose/sync/update` are generated)                                                                                                   |
| 2   | Four of the five apply-finding commands end at `spotlessApply` plus a `review-quick` re-run; `/audit-specs` applies nothing inline.                                               | `git show HEAD:.opencode/commands/` for the four audits and `apply-audit-reductions.md`                                                                                                                       |
| 3   | No command states the unit's branch, the uncommitted-work discipline, or the approval rule before this change.                                                                    | `git grep` at `HEAD` for `origin/main` (one hit — `seed-claims`'s `--base` default), `manual approval`, `uncommitted until`, `no commit`, and `own branch` (no hits)                                          |
| 4   | `/dependency-security-check` states a Snyk quota, billing rule, and reset schedule.                                                                                               | `git show HEAD:.opencode/commands/dependency-security-check.md`                                                                                                                                               |
| 5   | `/gradle-update` falls back to the report file.                                                                                                                                   | `git show HEAD:.opencode/commands/gradle-update.md`                                                                                                                                                           |
| 6   | `npmOutdated` writes `build/npm-outdated.txt`, `npm-outdated-raw.txt`, `npm-outdated.err.txt`, and `npm-outdated.exit`, and logs an error on a non-zero exit.                     | `build-logic/src/main/kotlin/frontend-conventions.gradle.kts`'s `npmOutdated` body                                                                                                                            |
| 7   | `toolingUpdates` declares each pin's workflow file as a `ToolingUpdateCheck` — three carrying the `openspec` pin — and its report names only the checks with an available update. | `build.gradle.kts`'s `toolingUpdates` registration                                                                                                                                                            |
| 8   | The standing reduction unit's review scenario narrows the gate to `review-quick` plus the manual pass.                                                                            | `git show HEAD:openspec/specs/showcase/quality/agent-skills/spec.md`, its "Application is owner-gated and reviewed" scenario                                                                                  |
| 9   | The session-continuity preflight is the review-gate procedure's step 3.                                                                                                           | `AGENTS.md`'s "Workflow: The review gate"                                                                                                                                                                     |
| 10  | The Structured Writing Standard already governs a command definition.                                                                                                             | `AGENTS.md`'s standard, which names "an agent, command, or skill definition under `.opencode/`", and the `agent-skills` requirement "Agent-authored technical artifacts follow a structured writing standard" |

The table is seeded from `scripts/claim-scan.py` over this change's authored diff (via `/seed-claims`), and the
candidates it raises but that are not claims are dismissed by class rather than one by one:

- **`[path]` and `[cross-ref]` candidates** — the scan's own false positives and the sites this change edits: the
  changed command files and the paths they name, and the slash-joined word pairs its pattern reads as references
  (`required/optional`, `commit/push/PR/archive/merge`). A site the change edits is carried by a task; a word pair is
  not a reference.
- **`[absolute]` candidates** — an `every`, `never`, or `only` is a rule or a scope statement where it appears, in a
  command's prose or in the change's own planning artifacts. Neither is a claim about prior evidence.
- **`[count]` candidates** — a numeral is a step number, a record field, or the artifact's own count, and the ledger's
  rows are the record this unit appends; none is a fact a reader checks elsewhere.

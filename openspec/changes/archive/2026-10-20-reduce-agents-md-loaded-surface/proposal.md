# Proposal

## Why

`AGENTS.md` is 356,723 bytes across 3,623 lines at `a36c3f7e`, loaded in full into every session. The audit's two
extraction opportunities have shipped and its three remaining candidates fail their retrieval paths, so they stay. What
remains reducible is task-scoped: check-authoring rules loaded before every task and read only when one is written, a
tool list the doctor and README already own, a `check` list restating the build file, and diagram mechanics whose owner
already states them. The check-authoring cluster also holds five evidence-hygiene rules — globs, searches, reports,
reproductions — that audits and reviews need without writing a check, so the cluster is split rather than moved whole.

## What Changes

- `AGENTS.md` — the eight check-authoring modes leave `## Gotchas → Writing checks and tests that can actually fail` for
  a new project skill, `.opencode/skills/check-and-test-authoring/SKILL.md`; the section keeps its heading, its
  invariant, and a trigger naming the skill.
- `AGENTS.md` — the cluster's five evidence-hygiene rules keep their text and move under a new `### Evidence and search`
  heading, because no trigger narrower than "before you search" would reach them and that trigger would fire on nearly
  every turn.
- `AGENTS.md` — the diagram geometry and rendering mechanics leave `## Gotchas → Documentation & durable claims` and the
  three diagram-related bullets in `## Conventions`; the definition gains what it does not yet carry, and the retained
  bullets state that every diagram edit is delegated — the conservative application of the spec's created/aligned/fixed
  contract, which does not name a comment-only edit.
- `AGENTS.md` — `## Prerequisites`' tool list becomes the doctor's and the README's subject, keeping the optional-path
  map, the hooks step, and the doctor-out-of-`check` rule.
- `AGENTS.md` — the `check` run line's task-name list is deleted; `build.gradle.kts`'s `check` block is authoritative.
- `verifyCapturedMarkers` was retargeted so the moved markers stay checked: the scan now covers `AGENTS.md` and the
  project-authored agent and skill definitions, with a known-bad test, and `commit-hygiene`'s delta records it.
- `.opencode/agent/lesson-capture.md` and `AGENTS.md`'s capture procedure — a proposal is routed by the decision its
  lesson governs: an authoring mode to the skill, a correction to that mode's section, an evidence rule to root, and a
  mixed lesson split. The root procedure's clauses that assume an `AGENTS.md` bullet are rewritten — its step 2
  destination, the "existing rule it extends" decision point, the "target rule" reference, the marker guard and the
  merged-rule sentence, and step 4's merge-time destination — so the capture loop closes instead of regrowing the rule
  at root.
- `openspec/specs/showcase/quality/agent-skills` (delta) — `ADDED`: the skill is the authoritative record, routing is by
  the decision a lesson governs, and root does not restate a mode; `MODIFIED`: the capture proposes to the owning
  artifact and names the rule it extends.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills`: it owns the placement and loading of agent guidance and the capture's routing, so the
  destination requirement and the two capture clauses live there.
- `showcase/quality/commit-hygiene`: the marker-placement verification and the pre-commit guard widen from `AGENTS.md`
  to the project-authored guidance files, so the moved markers stay placement-checked.

## Impact

- Files: `AGENTS.md`, `.opencode/agent/diagrammer.md`, `.opencode/agent/lesson-capture.md`, the new
  `.opencode/skills/check-and-test-authoring/SKILL.md`, `build.gradle.kts` (`verifyCapturedMarkers` inputs),
  `scripts/commit-hygiene.py` and `scripts/test-commit-hygiene.py` (the widened marker scan and its known-bad case),
  `scripts/git-hooks/pre-commit` (its header comment), and two delta specs under `openspec/specs/` at archive — the
  `agent-skills` capture requirement (copied whole, two clauses changed) and the `commit-hygiene` requirements (copied
  whole, scoped to the project-authored files).
- No runtime or deployment impact. The build's verification surface changes only by widening a check's scan.
- `verifyCapturedMarkers` now scans `AGENTS.md` and the project-authored guidance files — `.opencode/agent/*.md` and
  `.opencode/skills/*/SKILL.md` — excluding the vendored `axon4to5-*` and generated `openspec-*` skills, so the moved
  `captured:` markers stay placement-checked; `scripts/test-commit-hygiene.py` adds a known-bad case for a misplaced
  marker in a skill file. The other two gates' inputs — the helm install block `verifyInstallCommands` reads and the
  Paketo pins `verifyDocumentedPins` reads — sit in no moved span.
- Excluded by the request: the stale vision-pin and task-enumeration fixes (shipped as
  `apply-2026-10-19-audit-findings`) and the Redgate and gRPC findings (shipped as
  `remove-unrecorded-redgate-repository` and `correct-adr-0017-spring-grpc-gate`).

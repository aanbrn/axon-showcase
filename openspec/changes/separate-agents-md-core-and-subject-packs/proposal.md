# Proposal

## Why

`AGENTS.md` is 323,938 bytes across 3,362 lines and loads into every session (`wc -c -l AGENTS.md` at `4972e687`). About
39 KB of it is technical guidance for three subjects — JVM production code, JVM tests, and the Gradle build — that a
task outside a subject never needs, and that a task inside one subject does not need from the others. The subjects
include execution and diagnosis, not only authoring: running a test suite, checking coverage, reading a dependency
report, or explaining a failure is governed by the same guidance. No narrower destination exists today, and the
placement policy requires guidance needed across more than one stage to stay at root, so the move needs an explicit
policy decision rather than a reinterpretation.

## What Changes

- Split the JVM and Gradle technical guidance into three project skills: `jvm-development`, `jvm-testing`, and
  `gradle-build`.
- Add a root classification table that makes loading each pack mandatory before the first governed decision in its
  subject — including an execution or a report interpretation — and states the missing-pack stop, the overlap rule, the
  delegation rule, the semantic reviewer rule, and the re-classification rule after context loss.
- Remove the moved obligations from `AGENTS.md`, leaving a pointer at each vacated section and a minimal verification
  command at root.
- Amend the `agent-skills` placement policy with an explicit subject-scope destination class, so a subject pack is a
  permitted destination rather than a silent reinterpretation.
- Route capture additions to the pack that owns the lesson, in `AGENTS.md`, `lesson-capture.md`, and the spec.
- Extend the upstream-reference scan to every project-authored skill so the moved `owner/repo#NNN` references stay
  tracked.
- Have `review-quick`, `review-thorough`, and the review commands load the packs applicable to the change under review
  through a named hand-over, preserving `review-quick`'s bounded input contract.
- Apply the unit in a probe-first order: packs and the proposed root in a clone, the retrieval suite, then the root
  deletion; a failed positive control returns the unit to the owner with the root untouched. No partial variant ships
  automatically.
- Report complete per-class loads (baseline, task body, total), not root-plus-one-pack arithmetic, and claim no
  aggregate saving.

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- `showcase/quality/agent-skills`: the placement/extraction conditions gain a subject-scope destination class, the
  capture routing names the subject packs, and a new requirement states the packs' loading contract.
- `showcase/quality/merge-governance`: the upstream-reference report scans every project-authored skill as well as the
  root artifacts.

## Impact

- `AGENTS.md` shrinks by the measured moved spans and gains a routing section and three section pointers.
- Three project skills under `.opencode/skills/` become new maintained destinations.
- `upstreamReferences` gains a scan path and a test case.
- `review-quick` and `review-thorough` definitions gain a pack-classification step and the named-pack hand-over;
  `review-quick`'s capture and compliance-miss clauses widen to the packs.
- `.opencode/agent/agents-auditor.md` gains the subject-pack destination class and condition.
- `.opencode/agent/lesson-capture.md` and both restating lists in the `openspec/config.yaml` context are updated.
- `README.md`'s agent table and lesson-capture prose name the packs as destinations, and the parked extraction-condition
  entry in `docs/ideas.md` is refreshed.
- Per-task and review loads are reported as complete loads (baseline, task body, total), not as root-plus-one-pack
  arithmetic; no aggregate saving is claimed.
- No application code, API, dependency, deployment, or runtime behavior changes.

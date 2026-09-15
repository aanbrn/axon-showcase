## Why

The `agents-auditor`'s scope is a provenance partition: it audits what the repository authors and skips what it does not
(the generated `openspec-*` instruction files and the vendored `axon4to5-*` skills), because a local "fix" there is
overwritten or breaks provenance. That reasoning is sound for _fixing_ — but it makes a whole class of problem
invisible: third-party tooling that is inconsistent with how this repository actually uses it. A vendored skill that
prescribes a pattern our code no longer follows, or a generated command that names an artifact we removed, is skipped by
every audit and no gate reads it. The response is not a local edit (which the skipped set forbids) but a decision —
report it upstream, re-vendor, or change our usage.

## What Changes

- The `agents-auditor` gains a third output class: **third-party inconsistency** — advisory observations where a skipped
  file (a generated `openspec-*` instruction file or a vendored `axon4to5-*` skill) contradicts how this repository uses
  it. The class is bounded by a **harm test**: report only a contradiction that would mislead a workflow driven by the
  file or instruct a pattern the repository's code or conventions contradict, not a mere textual difference from our
  prose (which is expected by design).
- The class is **advisory, not a finding to fix**: the auditor never proposes a local edit to a skipped file, because
  the partition's rationale stands. Each item names the harm and the route — report upstream, re-vendor at a newer
  version, or adjust our usage — and the owner decides.
- The skipped files stay **out of the fix scope**; only the _observation_ is added. This is a widening of the auditor's
  output, not a re-scoping of what it may edit (it edits nothing).
- The `agent-skills` capability states the class, so the widening is a durable behavior rather than prose that can
  drift.

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- `showcase/quality/agent-skills`: the `agents-auditor` requirement gains the third-party-inconsistency advisory class
  and its harm test.

## Impact

- `.opencode/agent/agents-auditor.md` — the new advisory class, its harm test, and its routes; the frontmatter
  `description` notes it.
- `.opencode/commands/audit-agents.md` — its step-2 report line names the new class (it already defers to the report
  contract).
- `openspec/specs/showcase/quality/agent-skills/spec.md` — the `agents-auditor` requirement extended (a delta).
- `AGENTS.md` — the `agents-auditor` bullet notes the class where it describes the audit's scope and output.
- `README.md` — the `agents-auditor` row stays accurate (a one-line row; likely no edit).
- No code, workflow, or deployment change: this is agent tooling.

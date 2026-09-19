# Add a README auditor

## Why

The README is the repository's human-facing showcase, and its content — unlike its markdown formatting, which Spotless
now gates — has no check. Several defects in it were caught reactively rather than by a gate: the "two replicas", "36
panels" and "four services and a gateway" miscounts, a "22 capability specs" tally that drifted, and an OpenSpec-flow
diagram whose _semantics_ once needed confirming (its deliberate asymmetry is not a defect — `AGENTS.md` records it as
intentional). Two of the repository's own conventions — README design intent and Surface human-visible capabilities —
have no enforcement at all, so a change can silently leave a human-visible capability undocumented. The parked idea
already scoped the fix: a `readme-auditor` subagent with a `/audit-readme` trigger, a distinct artifact and audience
(humans, not agents) from the three existing auditors.

## What Changes

- **`.opencode/agent/readme-auditor.md`** (new) — a pro-model subagent (`opencode-go/deepseek-v4-pro`, `mode: subagent`,
  `temperature: 0`) auditing `README.md` only, on three verifiable axes: **accuracy/consistency** (every claim —
  commands, ports, versions, image and task names, links, the OpenSpec-flow diagram's semantics — matches the
  repository, cross-checked against `AGENTS.md` and the spec corpus); **design-intent fidelity** (the README convention:
  section order, the step-by-step Getting Started path, Gradle tasks over raw commands, curl-only, the
  prompting-exercise narrative); and **coverage / experience surfacing** (the Cool Story and every human-visible
  capability — the saga auto-start, the live SSE timeline, the `setup-hosts` hostnames, the Grafana access path —
  cross-checked against what the system does). Deliberately not an attractiveness judge: subjective quality (prose,
  redundancy, jargon, flow) is an advisory section for the user's judgment, never a defect, since the README is
  hand-curated by design.
- **`.opencode/commands/audit-readme.md`** (new) — the `/audit-readme` trigger.
- **`openspec/changes/add-readme-auditor/specs/showcase/quality/agent-skills/spec.md`** — an `ADDED` requirement
  capturing the auditor (its three axes, the advisory treatment of subjective quality, its `README.md`-only scope), and
  a `MODIFIED` block for "Report-producing subagents follow a shared report contract", whose bearer list names the three
  auditors and so gains the README auditor.
- **`AGENTS.md`** — a `readme-auditor` convention bullet beside the other auditor bullets, and the three stale auditor
  enumerations updated to four (the report-contract sentence ~795, the `docs/ideas.md` sweep clause ~634, and the
  multi-artifact-sweep sentence's "no auditor covers `README.md`" ~867).
- **`README.md`** — a row in the agent table and a row in the slash-command table, plus a sentence where the auditors
  are described.
- **`docs/ideas.md`** — remove the implemented `README auditor` idea; reword the sibling "Reconcile the architecture
  description" idea, whose reference to "the parked _README auditor_" now names a shipped auditor; and fix the off-peak
  entry's counts, which a fourth `/audit-*` agent makes wrong ("the three `/audit-*` agents … the pin's fifth agent,
  `diagrammer`" → four and sixth; "the three `/audit-*` sweeps" → four).

## Impact

- **Build**: none — two new `.opencode/` markdown files and documentation. `spotlessCheck` covers the markdown.
- **Tests**: none.
- **Workflow**: the audit runs on demand only (like the other three auditors); no gate depends on it.
- **Capability**: `showcase/quality/agent-skills` gains a requirement and its `## Purpose` needs the archive-commit
  refresh (a delta cannot carry a Purpose).

## New Capabilities

None.

## Modified Capabilities

- `showcase/quality/agent-skills` — an `ADDED` requirement for the `readme-auditor` (its three audit axes, its
  advisory-only treatment of subjective quality, and its `README.md`-only scope), and a `MODIFIED` block adding the
  README auditor to the shared report contract's bearer list.

# Proposal

## Why

An `agents-auditor` merge candidate can delegate content to a target the auditor has not checked. In the run that
produced PR #364, its Docker Images finding pointed the metrics sentence at the `deployment/web-ui` spec and stated that
the spec "already specifies … the nginx `stub_status` → `metricsExporter` → port `9113` chain" — but `9113` appears
nowhere under `openspec/specs/`; the spec holds the chain, not the port. Applying that suggestion dropped the port, the
Service port name, and the exporter's gate condition, and `review-quick` needed two rounds to restore them. Six such
drops recurred across the five findings. The contract constrains the merged text — it "preserves every anchor and piece
of evidence the originals carried" — but never the file that text points at, so a false delegation reads as verified.
The caller-side counterpart now exists in `AGENTS.md`; this closes the producer side.

## What Changes

- **`openspec/specs/showcase/quality/agent-skills/spec.md`** — the merge-candidate clause requires the audit to verify
  what a candidate's text delegates to a target against that target: its behavior **and** the identifiers, declarations,
  and gate conditions the delegated content names. A delegated item the target does not carry stays in the merged text,
  and the candidate says so.
- **`.opencode/agent/agents-auditor.md`** — the conciseness axis and method state the same check for a merge candidate's
  pointers, alongside the existing "preserves every anchor and piece of evidence" requirement.
- **No `AGENTS.md` change is expected**: the agents-auditor bullet already delegates the finding classes to the
  `agent-skills` spec, and the caller-side sweep rule landed with #365. A task verifies both rather than assuming it.
- **No README change is expected**: the verification rigor is not human-visible behavior.

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- `showcase/quality/agent-skills`: the agent-tooling audit's merge candidates must verify the pointer targets they
  delegate content to, and keep in the merged text any delegated item a target does not carry.

## Impact

- `.opencode/agent/agents-auditor.md` — the merge-candidate verification, in the conciseness axis and the method.
- `openspec/specs/showcase/quality/agent-skills/spec.md` — one `MODIFIED` requirement (the merge-candidate clause plus a
  scenario for the target check); the `## Purpose` is refreshed in the archive commit only if the scope text needs it.
- No code, no runtime behavior, no dependency, no build or deployment change. The auditor's output classes and the
  shared report contract are unchanged, so no verdict-line or bearer-list edit is owed.

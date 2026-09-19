# Reconcile the architecture description across README and AGENTS.md

## Why

The README and `AGENTS.md` state the same architectural facts twice, and the parked idea records that they have drifted.
Re-deriving the drift against the repository found the idea's cited example (a missing module) to be **false** — the
apparent omission was a detection artifact — leaving a smaller, real remainder: the README's `## Architecture` table
says "four components" while its own tree lists five service/gateway directories (the HTTP ports, on inspection, serve
distinct purposes rather than duplicating one fact). The module inventory is complete in both files; the other apparent
drifts do not exist.

## What Changes

- **`README.md`** — the `## Architecture` table's lead-in adopts the phrasing the repository already uses elsewhere
  ("four services and a web UI") so the table's four rows and the tree's five directories stop reading as a
  contradiction; no prose port restatement proved to be a genuine duplicate (the `AGENTS.md` "Ports:" paragraph carries
  deployment-only facts), so none was cross-referenced.
- **`AGENTS.md`** — no inventory change (it is complete); the "Ports:" paragraph stays, since it documents
  deployment-specific ports no service config holds.
- **`docs/ideas.md`** — remove the implemented idea.
- **`openspec/changes/reconcile-readme-agents-architecture/design.md`** — records the corrected drift analysis, so the
  false premise is not re-litigated.

## Impact

- **Build**: none — documentation only.
- **Tests**: none.
- **Docs**: a README lead-in sentence; the idea removal; and a captured lesson in `AGENTS.md`.

## New Capabilities

None.

## Modified Capabilities

None — documentation truth, so this change is `skip_specs`.

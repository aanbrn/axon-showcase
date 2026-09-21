# Apply the audit reports' findings

## Why

The two scheduled audit reports (`docs/audits/2026-09-20.md`, `2026-09-21.md`) recorded nine actionable findings between
them, several repeated across both runs. The most consequential is a dead pointer: `major-disabled.properties` sends a
reader to `dependency-management` for the Axon 4.x rationale, but that spec carries no Axon requirement — so the reason
the framework is pinned was recorded nowhere, even though the repository vendors the `axon4to5-*` migration skills.

This change applies the findings that are **documentation truth** about existing decisions and artifacts. The reports'
**spec-corpus** findings — two Helm requirements misplaced in `merge-governance`, a duplicated pair in `helm-chart`, and
seven imperative requirement headers — restructure spec content and are handled as their own change, since they owe a
different kind of delta (a moved requirement, and a header rename by delete-and-add).

## What Changes

- **`docs/adr/0011-defer-axon-framework-5-migration.md`** (new) — records the deferral and its reopen trigger (the
  extensions and modules the project uses have no 5.x release), mirroring ADR-0004's shape, with a Related-decisions
  section naming ADR-0009 as a sibling under the shared "Axon without Axon Server" intention.
- **`openspec/changes/apply-audit-report-findings/specs/…/dependency-management/spec.md`** — an `ADDED` requirement
  recording the Axon suppression and pointing at ADR-0011, so the dead pointer resolves.
- **`config/dependency-updates/major-disabled.properties`** — the `org.axonframework` comment names ADR-0011.
- **`docs/adr/0009-jgroups-distributed-command-bus.md`** — its Context and Decision now state the
  `axon-server-connector` exclusion **repo-wide** (six modules), not only the gateway and command service.
- **`AGENTS.md`** — the Formatting convention's removed-`codefmt`-skill reference dropped, and the "read the artifact's
  own definition" gotcha's `codefmt` example marked as a since-removed skill.
- **`.opencode/commands/dependency-security-check.md`** — its `.snyk` pointer no longer names the `dependency-security`
  spec, which holds a different concern (the real rationale is `.snyk`'s header and each ignore's `reason`).
- **`.opencode/commands/audit-agents.md`** — step 4 no longer lists `README.md` and the specs, which the audit never
  edits.
- **`docs/adr/0005-testable-exit-on-startup.md`** — Consequences states the command service **has adopted** the seam.
- **`docs/ideas.md`** — the two resolved questions (the Axon pin rationale; ADR-0009's exclusion scope) removed; the
  three still-open ones kept.

## Impact

- **Build**: none — documentation, a workflow-adjacent comment, and one spec requirement.
- **Tests**: none.
- **Capability**: `showcase/quality/dependency-management` gains a requirement (its `## Purpose` unchanged — it already
  covers the suppression surface).

## New Capabilities

None.

## Modified Capabilities

- `showcase/quality/dependency-management` — an `ADDED` requirement for the Axon framework suppression, recording the
  rationale the dead pointer promised.

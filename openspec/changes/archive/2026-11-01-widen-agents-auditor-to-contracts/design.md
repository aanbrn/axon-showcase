# Design — widen the agents-auditor's scope to the contracts

## Context

ADR-0025 §7's first consolidation moved the two governance contracts into `docs/contracts/`. The `agents-auditor` audits
the project-owned agent tooling, but its scope stops at `AGENTS.md`, the project-authored `.opencode/` files, and
`docs/guidance-evidence/`; the contracts are project-authored guidance and now have no reconciliation mechanism. The
consolidation's design recorded the drift as unmitigated and parked the widening.

## Decisions

### D1 — Widen the existing auditor, not a new one

The contracts are project-authored agent tooling, the artifact the `agents-auditor` already owns. A new auditor would
split one artifact's drift across two agents and repeat the pairing, budget, and provenance logic.

### D2 — The spec's scope sentence and one scenario body change; the scenario set stays

A `MODIFIED` block for "Project-owned agent tooling is audited for consistency and conciseness": the description adds
`docs/contracts/` to the scope, and the "An agent-tooling audit is produced" scenario's body names it too. All 34
scenarios are retained in their order and none is renamed.

### D3 — The parked entry is removed by the implementing change

`docs/ideas.md`'s entry is removed in this change, per the ideas file's rule that an idea is removed once implemented.

### D4 — The Purpose refresh is an archive-commit edit

The `agent-skills` Purpose enumerates the auditors' coverage as the guidance and project-authored `.opencode/` files;
adding `docs/contracts/` widens that class, and a delta cannot carry a Purpose. The archive commit refreshes it.

## Alternatives

- **A new contracts auditor** — rejected: the contracts are agent tooling, and the existing auditor already owns the
  pairing and budget checks.
- **No audit** — rejected: the consolidation recorded the drift as unmitigated.

## Risks

- The auditor's scope grows by one directory; its budget and pairing checks already apply to the guidance corpus, so the
  addition is one scope sentence and one scenario body.

## Claims table

| Claim                                                                   | Source                                                                                          | Result                                                                                                                                               |
| ----------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------- |
| The requirement's 34 scenarios are preserved                            | `openspec validate --all`; the delta's `#### Scenario:` count                                   | 34                                                                                                                                                   |
| The scope sites are updated                                             | the owning tasks 1.1-1.3 and 2.1; `git diff --stat 9a20c4b1`                                    | the definition's description, intro, scope list, and Method; `AGENTS.md`; the four summaries; the delta - the site list read from the per-file diffs |
| The parked entry is removed                                             | `docs/ideas.md`                                                                                 | the `## 2026-10-31` section is gone                                                                                                                  |
| The spec's scope sentence and the audit scenario name `docs/contracts/` | the delta                                                                                       | the scope sentence and the scenario body                                                                                                             |
| The four scope summaries stop enumerating the scope                     | `README.md` (3 sites); `.opencode/commands/audit-agents.md`                                     | 4 edits; two point at the definition                                                                                                                 |
| The smoke-run restored the contract's qualifier                         | `docs/contracts/knowledge-authoring.md`; `openspec/specs/showcase/quality/agent-skills/spec.md` | "of development activity" matches the spec                                                                                                           |

Dismissals, one per kind: the `absolute` candidates are the preserved requirement's own absolutes and the reflowed
`AGENTS.md` bullet, not new claims; the `path` candidates are the preserved text's existing paths (`docs/audits/`,
`docs/guidance-evidence/`, `showcase/quality/agent-skills`), not edited paths; the `count` candidates are the preserved
scenario and requirement counts; the `cross-ref` candidates are the preserved requirement's `code/workflows/specs` and
the proposal's and task's references to the source requirement, not new cross-references.

## The smoke-run

A seeded contradiction in `docs/contracts/shared-report.md` ("The verdict line MAY be omitted when the report is
short.") was detected by the widened auditor, and the provenance section — an explained control — was not reported. The
run also found a real divergence: the knowledge-authoring contract dropped "of development activity" from the spec's
sentence; the contract now restores it.
